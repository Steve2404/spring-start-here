package vorkurs04_annotations.projects.p06_schema.solution;

import projectkit.Javac;
import vorkurs04_annotations.projects.p06_schema.Data;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Le controle de schema : avant d'importer quoi que ce soit, on lit le type GENERIQUE complet de chaque
 * champ (getGenericType / getAnnotatedType), car getType() ne voit que le type EFFACE (List, Map...).
 */
public class SchemaCheck {

    // ---------- Partie 1 : decrire un Type generique ----------

    /** Le Type en texte, comme on l'ecrit en Java, sans toString (dont le format varie). */
    static String render(Type t, Map<String, AnnotatedType> resolved) {
        if (t instanceof Class<?> c) {
            return c.isArray() ? render(c.getComponentType(), resolved) + "[]" : c.getSimpleName();
        }
        if (t instanceof ParameterizedType p) {
            // Map.Entry<K, V> a un type PROPRIETAIRE (Map) : getOwnerType() le donne.
            String raw = ((Class<?>) p.getRawType()).getSimpleName();
            String owner = p.getOwnerType() == null ? "" : render(p.getOwnerType(), resolved) + ".";
            return owner + raw + "<" + Arrays.stream(p.getActualTypeArguments()).map(a -> render(a, resolved))
                    .collect(Collectors.joining(", ")) + ">";
        }
        if (t instanceof TypeVariable<?> v) {
            AnnotatedType bound = resolved.get(v.getName());
            return bound == null ? v.getName() : renderAnnotated(bound, Map.of());
        }
        if (t instanceof WildcardType w) {
            if (w.getLowerBounds().length > 0) {
                return "? super " + render(w.getLowerBounds()[0], resolved);
            }
            Type upper = w.getUpperBounds()[0];
            return upper == Object.class ? "?" : "? extends " + render(upper, resolved);
        }
        // Il n'existe que ces 5 sortes de Type : un GenericArrayType est la derniere.
        GenericArrayType g = (GenericArrayType) t;
        return render(g.getGenericComponentType(), resolved) + "[]";
    }

    static String annotations(AnnotatedType at) {
        return Arrays.stream(at.getAnnotations()).map(a -> "@" + a.annotationType().getSimpleName() + " ").collect(Collectors.joining());
    }

    /** Le meme rendu, mais sur l'arbre PARALLELE des AnnotatedType, qui porte en plus les annotations de type. */
    static String renderAnnotated(AnnotatedType at, Map<String, AnnotatedType> resolved) {
        if (at instanceof AnnotatedParameterizedType p) {
            ParameterizedType pt = (ParameterizedType) p.getType();
            String raw = ((Class<?>) pt.getRawType()).getSimpleName();
            String owner = pt.getOwnerType() == null ? "" : render(pt.getOwnerType(), resolved) + ".";
            return annotations(at) + owner + raw + "<" + Arrays.stream(p.getAnnotatedActualTypeArguments())
                    .map(a -> renderAnnotated(a, resolved)).collect(Collectors.joining(", ")) + ">";
        }
        if (at instanceof AnnotatedArrayType a) {
            return renderAnnotated(a.getAnnotatedGenericComponentType(), resolved) + "[]";
        }
        if (at instanceof AnnotatedTypeVariable v && resolved.containsKey(((TypeVariable<?>) v.getType()).getName())) {
            return renderAnnotated(resolved.get(((TypeVariable<?>) v.getType()).getName()), Map.of());
        }
        if (at instanceof AnnotatedWildcardType) {
            return annotations(at) + render(at.getType(), resolved);
        }
        return annotations(at) + render(at.getType(), resolved);
    }

    // ---------- Partie 2 : ce que l'import sait remplir ----------

    /** La 1re raison de refus en parcourant l'arbre du type, ou vide si tout est gere. */
    static Optional<String> problem(Type t, Map<String, AnnotatedType> resolved) {
        if (t instanceof WildcardType) {
            return Optional.of("joker");
        }
        if (t instanceof GenericArrayType) {
            return Optional.of("tableau generique");
        }
        if (t instanceof TypeVariable<?> v) {
            if (resolved.containsKey(v.getName())) {
                return Optional.empty();
            }
            // getGenericDeclaration dit QUI declare T ; getBounds donne sa borne (ici Comparable<T>).
            String owner = ((Class<?>) v.getGenericDeclaration()).getSimpleName();
            return Optional.of("variable " + v.getName() + " de " + owner + " non resolue (borne " + render(v.getBounds()[0], resolved) + ")");
        }
        if (t instanceof Class<?> c) {
            if (c.getTypeParameters().length > 0) {
                return Optional.of("type brut");
            }
            return c.isArray() ? problem(c.getComponentType(), resolved) : Optional.empty();
        }
        ParameterizedType p = (ParameterizedType) t;
        if (p.getOwnerType() != null) {
            return Optional.of("type imbrique");
        }
        for (Type arg : p.getActualTypeArguments()) {
            Optional<String> inner = problem(arg, resolved);
            if (inner.isPresent()) {
                return inner;
            }
        }
        return Optional.empty();
    }

    /**
     * Les variables de type resolues par la super-classe : IntBox extends Box<@Positive Integer> donne T -> @Positive Integer.
     * On associe chaque TypeVariable de la super-classe brute a l'argument annote correspondant.
     */
    static Map<String, AnnotatedType> resolutions(Class<?> c) {
        Map<String, AnnotatedType> result = new HashMap<>();
        if (c.getGenericSuperclass() instanceof ParameterizedType) {
            TypeVariable<?>[] vars = c.getSuperclass().getTypeParameters();
            AnnotatedType[] args = ((AnnotatedParameterizedType) c.getAnnotatedSuperclass()).getAnnotatedActualTypeArguments();
            for (int i = 0; i < vars.length; i++) {
                result.put(vars[i].getName(), args[i]);
            }
        }
        return result;
    }

    // ---------- Partie 3 : importer une valeur selon son type annote ----------

    /** Convertit le texte selon le type, et note chaque violation de @Positive / @NotBlank dans errors. */
    static Object parse(String text, AnnotatedType at, Map<String, AnnotatedType> resolved, String path, List<String> errors) {
        if (at instanceof AnnotatedTypeVariable v) {
            return parse(text, resolved.get(((TypeVariable<?>) v.getType()).getName()), Map.of(), path, errors);
        }
        Type type = at.getType();
        if (at instanceof AnnotatedParameterizedType p) {
            Class<?> raw = (Class<?>) ((ParameterizedType) p.getType()).getRawType();
            AnnotatedType[] args = p.getAnnotatedActualTypeArguments();
            if (raw == List.class) {
                List<Object> list = new ArrayList<>();
                if (!text.isEmpty()) {
                    // Une liste de listes se coupe sur "/", une liste simple sur ",".
                    boolean nested = args[0] instanceof AnnotatedParameterizedType;
                    String[] parts = text.split(nested ? "/" : ",");
                    for (int i = 0; i < parts.length; i++) {
                        list.add(parse(parts[i], args[0], resolved, path + "[" + i + "]", errors));
                    }
                }
                return list;
            }
            if (raw == Map.class) {
                Map<String, Object> map = new LinkedHashMap<>();
                if (!text.isEmpty()) {
                    for (String entry : text.split(",")) {
                        String[] kv = entry.split(":", 2);
                        map.put(kv[0], parse(kv[1], args[1], resolved, path + "." + kv[0], errors));
                    }
                }
                return map;
            }
            // Optional : texte vide -> vide ; sinon la valeur, validee comme son argument de type.
            return text.isEmpty() ? Optional.empty() : Optional.of(parse(text, args[0], resolved, path, errors));
        }
        if (type instanceof Class<?> c && c.isArray()) {
            String[] parts = text.isEmpty() ? new String[0] : text.split(",");
            return Arrays.copyOf(parts, parts.length);
        }
        Object value;
        if (type == Integer.class) {
            value = Integer.parseInt(text);
        } else if (type == Double.class) {
            value = Double.parseDouble(text);
        } else {
            value = text;
        }
        // Les annotations de TYPE se lisent sur ce noeud precis de l'arbre AnnotatedType.
        if (at.isAnnotationPresent(Positive.class) && ((Number) value).doubleValue() <= 0) {
            errors.add(path + " = " + value + " doit etre > 0");
        }
        if (at.isAnnotationPresent(NotBlank.class) && ((String) value).isBlank()) {
            errors.add(path + " ne doit pas etre vide");
        }
        return value;
    }

    static String show(Object value) {
        return value instanceof String[] array ? Arrays.toString(array) : String.valueOf(value);
    }

    public static void main(String[] args) {
        String pkg = SchemaCheck.class.getPackageName();
        Map<String, String> sources = new LinkedHashMap<>();
        Data.SCHEMAS.forEach((name, src) -> sources.put(name, src.replace("{{PKG}}", pkg)));
        Javac.Result compiled = Javac.compile(sources);

        Map<String, Class<?>> classes = new LinkedHashMap<>();
        Map<String, Boolean> valid = new HashMap<>();
        for (String name : Data.SCHEMAS.keySet()) {
            Class<?> c = Javac.load(compiled, name);
            classes.put(name, c);
            Map<String, AnnotatedType> resolved = resolutions(c);
            boolean ok = true;
            // getFields() : les champs publics, HERITES compris (IntBox voit value et history de Box).
            for (Field f : Arrays.stream(c.getFields()).sorted(Comparator.comparing(Field::getName)).toList()) {
                Optional<String> why = problem(f.getGenericType(), resolved);
                String text = renderAnnotated(f.getAnnotatedType(), resolved);
                if (why.isPresent()) {
                    ok = false;
                    System.out.println("REJET " + name + "." + f.getName() + " : " + why.get() + " dans " + render(f.getGenericType(), resolved));
                } else {
                    System.out.println("SCHEMA " + name + "." + f.getName() + " : " + text);
                }
            }
            valid.put(name, ok);
        }

        for (int i = 0; i < Data.ROWS.size(); i++) {
            String[] head = Data.ROWS.get(i).split(" \\| ", 2);
            String name = head[0];
            String prefix = "IMPORT ligne " + (i + 1) + " " + name + " : ";
            if (!valid.get(name)) {
                System.out.println(prefix + "REFUS schema invalide");
                continue;
            }
            Class<?> c = classes.get(name);
            Map<String, AnnotatedType> resolved = resolutions(c);
            Map<String, String> given = new HashMap<>();
            for (String pair : head[1].split(" ; ")) {
                String[] kv = pair.split("=", 2);
                given.put(kv[0], kv[1]);
            }
            List<String> errors = new ArrayList<>();
            List<String> shown = new ArrayList<>();
            for (Field f : Arrays.stream(c.getFields()).sorted(Comparator.comparing(Field::getName)).toList()) {
                String text = given.get(f.getName());
                shown.add(f.getName() + "=" + (text == null ? "absent" : show(parse(text, f.getAnnotatedType(), resolved, f.getName(), errors))));
            }
            System.out.println(prefix + (errors.isEmpty() ? String.join(" ", shown) : "REFUS " + String.join(" ; ", errors)));
        }

        // Signature generique d'une methode : parametres de type, retour, parametres (varargs), exceptions.
        Method top = Arrays.stream(classes.get("Ranking").getMethods()).filter(m -> m.getName().equals("top")).findFirst().orElseThrow();
        String typeParams = Arrays.stream(top.getTypeParameters())
                .map(v -> v.getName() + " extends " + render(v.getBounds()[0], Map.of())).collect(Collectors.joining(", "));
        Type[] params = top.getGenericParameterTypes();
        List<String> shownParams = new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            String p = render(params[i], Map.of());
            shownParams.add(top.isVarArgs() && i == params.length - 1 ? p.substring(0, p.length() - 2) + "..." : p);
        }
        String throwsPart = Arrays.stream(top.getGenericExceptionTypes()).map(t -> render(t, Map.of())).collect(Collectors.joining(", "));
        System.out.println("SIGNATURE <" + typeParams + "> " + render(top.getGenericReturnType(), Map.of()) + " top("
                + String.join(", ", shownParams) + ") throws " + throwsPart);

        // Effacement : a l'execution, List<String> et List<Integer> partagent la MEME Class.
        Class<?> ranking = classes.get("Ranking");
        try {
            Field words = ranking.getField("words");
            Field counts = ranking.getField("counts");
            System.out.println("EFFACEMENT words/counts : meme getType ? " + (words.getType() == counts.getType())
                    + " ; meme getGenericType ? " + words.getGenericType().equals(counts.getGenericType()));
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
        System.out.println("EFFACEMENT top : " + Arrays.stream(top.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", ", "[", "]")));
    }
}
