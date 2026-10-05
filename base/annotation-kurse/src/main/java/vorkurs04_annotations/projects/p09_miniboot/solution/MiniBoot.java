package vorkurs04_annotations.projects.p09_miniboot.solution;

import projectkit.Javac;
import vorkurs04_annotations.projects.p09_miniboot.Data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * MiniBoot : un mini-conteneur, phase par phase. Chaque phase s'arrete au premier probleme (ou les
 * rassemble tous quand c'est utile, comme la configuration), affiche ECHEC et ARRET, et l'application
 * suivante repart de zero. Les annotations ne font rien : tout le comportement est ICI.
 */
public class MiniBoot {

    /** Signale l'arret de l'application ; les lignes ECHEC sont deja affichees. */
    static final class Stop extends RuntimeException {
        Stop() {
            super(null, null, false, false);
        }
    }

    static void fail(List<String> errors) {
        errors.forEach(e -> System.out.println("  ECHEC " + e));
        System.out.println("  ARRET");
        throw new Stop();
    }

    static boolean isComponent(Class<?> c) {
        return c.isAnnotationPresent(MiniComponent.class) || c.isAnnotationPresent(MiniConfig.class);
    }

    static List<Field> sortedFields(Class<?> c) {
        return Arrays.stream(c.getDeclaredFields()).sorted(Comparator.comparing(Field::getName)).toList();
    }

    static List<Method> annotated(Class<?> c, Class<? extends java.lang.annotation.Annotation> a) {
        return Arrays.stream(c.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(a))
                .sorted(Comparator.comparing(Method::getName)).toList();
    }

    /** Phase 1 : structure. Une classe abstraite ne s'instancie pas ; un lifecycle doit etre void, sans parametre, non static. */
    static List<String> structure(List<Class<?>> components) {
        List<String> errors = new ArrayList<>();
        for (Class<?> c : components) {
            if (Modifier.isAbstract(c.getModifiers())) {
                errors.add("structure " + c.getSimpleName() + " : classe abstraite");
            }
            for (Class<? extends java.lang.annotation.Annotation> a : List.of(OnStart.class, AfterLoad.class)) {
                for (Method m : annotated(c, a)) {
                    if (m.getParameterCount() != 0 || m.getReturnType() != void.class || Modifier.isStatic(m.getModifiers())) {
                        errors.add("structure " + c.getSimpleName() + "." + m.getName() + " : @" + a.getSimpleName()
                                + " doit etre void, sans parametre, non static");
                    }
                }
            }
        }
        return errors;
    }

    /** Phase 2 : chaque @Inject doit designer exactement UN composant assignable a son type. */
    static Map<Field, Class<?>> wiring(List<Class<?>> components, List<String> errors) {
        Map<Field, Class<?>> wiring = new LinkedHashMap<>();
        for (Class<?> c : components) {
            for (Field f : sortedFields(c)) {
                if (!f.isAnnotationPresent(Inject.class)) {
                    continue;
                }
                List<Class<?>> candidates = components.stream().filter(f.getType()::isAssignableFrom).toList();
                String where = "injection " + c.getSimpleName() + "." + f.getName() + " : ";
                if (candidates.isEmpty()) {
                    errors.add(where + "aucun composant de type " + f.getType().getSimpleName());
                } else if (candidates.size() > 1) {
                    errors.add(where + "plusieurs composants de type " + f.getType().getSimpleName());
                } else {
                    wiring.put(f, candidates.get(0));
                }
            }
        }
        return wiring;
    }

    /** Phase 3 : ordre topologique (Kahn). Parmi les composants prets, on prend toujours le premier par nom. */
    static List<Class<?>> order(List<Class<?>> components, Map<Field, Class<?>> wiring) {
        Map<Class<?>, TreeSet<String>> missing = new HashMap<>();
        for (Class<?> c : components) {
            missing.put(c, new TreeSet<>());
        }
        wiring.forEach((field, dependency) -> missing.get(field.getDeclaringClass()).add(dependency.getSimpleName()));
        List<Class<?>> ordered = new ArrayList<>();
        TreeMap<String, Class<?>> ready = new TreeMap<>();
        missing.forEach((c, deps) -> {
            if (deps.isEmpty()) {
                ready.put(c.getSimpleName(), c);
            }
        });
        while (!ready.isEmpty()) {
            Class<?> next = ready.pollFirstEntry().getValue();
            ordered.add(next);
            missing.forEach((c, deps) -> {
                if (deps.remove(next.getSimpleName()) && deps.isEmpty()) {
                    ready.put(c.getSimpleName(), c);
                }
            });
        }
        if (ordered.size() < components.size()) {
            String cycle = components.stream().filter(c -> !ordered.contains(c)).map(Class::getSimpleName).sorted()
                    .collect(Collectors.joining(", "));
            fail(List.of("cycle : " + cycle));
        }
        return ordered;
    }

    static Object create(Class<?> c) throws ReflectiveOperationException {
        Constructor<?> ctor = c.getDeclaredConstructor();
        if (!ctor.canAccess(null)) {
            ctor.trySetAccessible();
        }
        try {
            return ctor.newInstance();
        } catch (InvocationTargetException e) {
            // Le CONSTRUCTEUR a echoue : on raconte la vraie cause, pas l'enveloppe.
            fail(List.of("creation " + c.getSimpleName() + " : " + e.getCause().getClass().getSimpleName() + " " + e.getCause().getMessage()));
            return null;
        }
    }

    /** Conversion STRICTE : "yes" n'est pas un boolean, "trois" n'est pas un int. */
    static Optional<Object> convert(String text, Class<?> type) {
        try {
            if (type == int.class) {
                return Optional.of(Integer.parseInt(text));
            }
            if (type == long.class) {
                return Optional.of(Long.parseLong(text));
            }
            if (type == boolean.class) {
                return text.equals("true") || text.equals("false") ? Optional.of(Boolean.parseBoolean(text)) : Optional.empty();
            }
            return Optional.of(text);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** Phase 4b : la configuration. On rassemble TOUTES les erreurs avant d'echouer : plus utile a l'utilisateur. */
    static void bind(Object target, Map<String, String> properties) throws IllegalAccessException {
        Class<?> c = target.getClass();
        String prefix = c.getAnnotation(MiniConfig.class).prefix();
        List<String> errors = new ArrayList<>();
        List<String> report = new ArrayList<>();
        for (Field f : sortedFields(c)) {
            ConfigValue cv = f.getAnnotation(ConfigValue.class);
            if (cv == null) {
                continue;
            }
            String where = "config " + c.getSimpleName() + "." + f.getName() + " : ";
            if (Modifier.isStatic(f.getModifiers())) {
                errors.add(where + "champ static interdit");
                continue;
            }
            if (Modifier.isFinal(f.getModifiers())) {
                errors.add(where + "champ final interdit");
                continue;
            }
            String key = prefix + "." + cv.key();
            String text = properties.get(key);
            boolean fromDefault = text == null;
            if (fromDefault && cv.required()) {
                errors.add(where + "cle " + key + " manquante");
                continue;
            }
            if (fromDefault) {
                text = cv.defaultValue();
            }
            Optional<Object> value = convert(text, f.getType());
            if (value.isEmpty()) {
                errors.add(where + "'" + text + "' n'est pas un " + f.getType().getSimpleName());
                continue;
            }
            f.trySetAccessible();
            f.set(target, value.get());
            report.add(f.getName() + "=" + value.get() + (fromDefault ? " (defaut)" : ""));
        }
        if (!errors.isEmpty()) {
            fail(errors);
        }
        System.out.println("  config " + c.getSimpleName() + " : " + String.join(" ", report));
    }

    static void call(Object target, Method m, String phase) throws IllegalAccessException {
        String where = phase + " " + target.getClass().getSimpleName() + "." + m.getName();
        m.trySetAccessible();
        try {
            m.invoke(target);
            System.out.println("  " + where + " : ok");
        } catch (InvocationTargetException e) {
            fail(List.of(where + " : " + e.getCause().getClass().getSimpleName() + " " + e.getCause().getMessage()));
        }
    }

    static void boot(Javac.Result compiled, Data.App app) throws ReflectiveOperationException {
        System.out.println("APP " + app.name());
        // Decouverte : seules les classes annotees deviennent des composants (Mailer, une interface, est ignoree).
        List<Class<?>> components = new ArrayList<>();
        for (String name : app.scan()) {
            Class<?> c = Javac.load(compiled, name);
            if (isComponent(c)) {
                components.add(c);
            } else {
                System.out.println("  ignore " + name);
            }
        }
        System.out.println("  composants : " + components.stream().map(Class::getSimpleName).sorted().toList());

        List<String> errors = structure(components);
        if (!errors.isEmpty()) {
            fail(errors);
        }
        Map<Field, Class<?>> wiring = wiring(components, errors);
        if (!errors.isEmpty()) {
            fail(errors);
        }
        List<Class<?>> ordered = order(components, wiring);
        System.out.println("  ordre : " + ordered.stream().map(Class::getSimpleName).collect(Collectors.joining(" -> ")));

        Map<Class<?>, Object> registry = new LinkedHashMap<>();
        for (Class<?> c : ordered) {
            Object instance = create(c);
            registry.put(c, instance);
            if (c.isAnnotationPresent(MiniConfig.class)) {
                bind(instance, app.properties());
                for (Method m : annotated(c, AfterLoad.class)) {
                    call(instance, m, "afterLoad");
                }
            }
        }
        for (Map.Entry<Field, Class<?>> w : wiring.entrySet()) {
            Field f = w.getKey();
            f.trySetAccessible();
            f.set(registry.get(f.getDeclaringClass()), registry.get(w.getValue()));
        }
        System.out.println("  injections : " + wiring.entrySet().stream()
                .map(w -> w.getKey().getDeclaringClass().getSimpleName() + "." + w.getKey().getName() + "<-" + w.getValue().getSimpleName())
                .sorted().collect(Collectors.joining(" ")));
        for (Class<?> c : ordered) {
            for (Method m : annotated(c, OnStart.class)) {
                call(registry.get(c), m, "onStart");
            }
        }
        System.out.println("  DEMARRE : " + registry.size() + " composant(s)");
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        String pkg = MiniBoot.class.getPackageName();
        Map<String, String> sources = new LinkedHashMap<>();
        Data.SOURCES.forEach((name, src) -> sources.put(name, src.replace("{{PKG}}", pkg)));
        Javac.Result compiled = Javac.compile(sources);
        for (Data.App app : Data.APPS) {
            try {
                boot(compiled, app);
            } catch (Stop stopped) {
                // l'application est arretee, on passe a la suivante
            }
        }
    }
}
