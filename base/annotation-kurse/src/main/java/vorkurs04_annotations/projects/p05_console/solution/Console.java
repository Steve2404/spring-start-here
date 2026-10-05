package vorkurs04_annotations.projects.p05_console.solution;

import projectkit.Javac;
import vorkurs04_annotations.projects.p05_console.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * La console : les metadonnees (@Command) ne font rien ; c'est cette classe qui les DECOUVRE, les
 * VALIDE, en fait un MODELE (le registre), puis DECIDE et EXECUTE (Method.invoke). Pipeline 0.4.9 :
 * Discovery -> Detection -> Read -> Validation -> Model -> Decision.
 */
public class Console {

    /** Une commande validee : tout ce qu'il faut pour l'executer sans relire l'annotation. */
    record Entry(String name, Method method, Object receiver, Command meta) {
    }

    static final Set<Class<?>> SUPPORTED = Set.of(String.class, int.class, long.class, double.class, boolean.class);

    static boolean supported(Method m) {
        // Un varargs est un tableau en DERNIER parametre : on regarde le type de ses elements.
        Class<?>[] types = m.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            Class<?> t = (m.isVarArgs() && i == types.length - 1) ? types[i].getComponentType() : types[i];
            if (!SUPPORTED.contains(t)) {
                return false;
            }
        }
        return true;
    }

    static String unsupportedType(Method m) {
        return Arrays.stream(m.getParameterTypes()).filter(t -> !SUPPORTED.contains(t) && !t.isArray())
                .map(Class::getSimpleName).findFirst().orElse("?");
    }

    static String signature(Method m) {
        // Pour un varargs, getParameterTypes() rend int[] : on l'affiche comme on l'ecrit, int...
        Class<?>[] types = m.getParameterTypes();
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            boolean varargs = m.isVarArgs() && i == types.length - 1;
            parts.add(varargs ? types[i].getComponentType().getSimpleName() + "..." : types[i].getSimpleName());
        }
        return "(" + String.join(",", parts) + ")";
    }

    static Object convert(String token, Class<?> type) {
        // Les conversions sont NOTRE travail : invoke n'a jamais transforme un String en int.
        if (type == int.class) {
            return Integer.parseInt(token);
        }
        if (type == long.class) {
            return Long.parseLong(token);
        }
        if (type == double.class) {
            return Double.parseDouble(token);
        }
        if (type == boolean.class) {
            return Boolean.parseBoolean(token);
        }
        return token;
    }

    static Object[] arguments(Method m, List<String> tokens) {
        Class<?>[] types = m.getParameterTypes();
        if (m.isVarArgs()) {
            // Tous les jetons vont dans UN tableau du type des elements : invoke attend un Object[] de 1 case.
            Class<?> component = types[types.length - 1].getComponentType();
            int[] values = new int[tokens.size()];
            for (int i = 0; i < tokens.size(); i++) {
                values[i] = (Integer) convert(tokens.get(i), component);
            }
            return new Object[]{values};
        }
        // Autant d'arguments que de jetons : si le nombre est faux, c'est invoke qui le dira.
        Object[] args = new Object[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            args[i] = convert(tokens.get(i), i < types.length ? types[i] : String.class);
        }
        return args;
    }

    static Map<String, Entry> register(Javac.Result compiled) throws ReflectiveOperationException {
        Map<String, List<Entry>> candidates = new LinkedHashMap<>();
        for (String className : Data.HANDLERS.keySet()) {
            Class<?> type = Javac.load(compiled, className);
            // Un receveur par classe, fabrique par sa methode static create() : receveur null pour une static.
            Object receiver = type.getDeclaredMethod("create").invoke(null);
            List<Method> methods = Arrays.stream(type.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Command.class))
                    .sorted(Comparator.comparing(Method::getName)).toList();
            for (Method m : methods) {
                Command meta = m.getAnnotation(Command.class);
                String where = className + "." + m.getName();
                if (meta.name().isBlank()) {
                    System.out.println("REJET " + where + " : nom vide");
                } else if (!supported(m)) {
                    System.out.println("REJET " + where + " : type non gere " + unsupportedType(m));
                } else {
                    candidates.computeIfAbsent(meta.name(), k -> new ArrayList<>()).add(new Entry(meta.name(), m, receiver, meta));
                }
            }
        }
        // Deux commandes du meme nom : on n'invente PAS de priorite, aucune des deux n'est enregistree.
        Map<String, Entry> registry = new TreeMap<>();
        new TreeMap<>(candidates).forEach((name, list) -> {
            if (list.size() > 1) {
                String who = list.stream().map(e -> e.method().getDeclaringClass().getSimpleName() + "." + e.method().getName())
                        .sorted().collect(Collectors.joining(", "));
                System.out.println("CONFLIT " + name + " : " + who);
            } else {
                registry.put(name, list.get(0));
            }
        });
        return registry;
    }

    static String run(Map<String, Entry> registry, String user, String name, List<String> tokens) {
        Entry entry = registry.get(name);
        if (entry == null) {
            return "INCONNU";
        }
        if (entry.meta().adminOnly() && !user.equals("admin")) {
            return "REFUS reserve aux admins";
        }
        Method m = entry.method();
        Object receiver = Modifier.isStatic(m.getModifiers()) ? null : entry.receiver();
        // Une methode private n'est pas accessible d'ici : on demande l'acces, sans exception si c'est refuse.
        if (!m.canAccess(receiver) && !m.trySetAccessible()) {
            return "ERREUR ACCES";
        }
        Object[] args;
        try {
            args = arguments(m, tokens);
        } catch (NumberFormatException e) {
            return "ERREUR ARGUMENT";
        }
        try {
            Object result = m.invoke(receiver, args);
            // void -> null ; les primitifs reviennent emballes (Long, Integer, Boolean...).
            return m.getReturnType() == void.class ? "OK" : String.valueOf(result);
        } catch (IllegalArgumentException e) {
            // Erreur de NOTRE appel (nombre ou types d'arguments) : la commande n'a meme pas demarre.
            return "ERREUR APPEL";
        } catch (InvocationTargetException e) {
            // La commande a demarre puis a echoue : la vraie cause est EMBALLEE.
            Throwable cause = e.getCause();
            return "ERREUR COMMANDE " + cause.getClass().getSimpleName() + " (" + cause.getMessage() + ")";
        } catch (IllegalAccessException e) {
            return "ERREUR ACCES";
        }
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        String pkg = Console.class.getPackageName();
        Map<String, String> sources = new LinkedHashMap<>();
        Data.HANDLERS.forEach((name, src) -> sources.put(name, src.replace("{{PKG}}", pkg)));
        Javac.Result compiled = Javac.compile(sources);

        Map<String, Entry> registry = register(compiled);
        System.out.println("COMMANDES : " + registry.values().stream()
                .map(e -> e.name() + signature(e.method()) + (e.meta().adminOnly() ? " [admin]" : "")
                        + (Modifier.isPrivate(e.method().getModifiers()) ? " [prive]" : ""))
                .collect(Collectors.joining(" ")));

        for (String line : Data.SCRIPT) {
            List<String> parts = List.of(line.split(" "));
            String user = parts.get(0);
            String name = parts.get(1);
            if (name.equals("help")) {
                // L'aide est une DECISION prise sur les metadonnees : priorite decroissante, puis nom.
                registry.values().stream()
                        .sorted(Comparator.comparingInt((Entry e) -> e.meta().priority()).reversed().thenComparing(Entry::name))
                        .forEach(e -> System.out.println("AIDE " + e.name() + " : "
                                + (e.meta().description().isEmpty() ? "(sans description)" : e.meta().description())));
                continue;
            }
            System.out.println(line + " : " + run(registry, user, name, parts.subList(2, parts.size())));
        }
    }
}
