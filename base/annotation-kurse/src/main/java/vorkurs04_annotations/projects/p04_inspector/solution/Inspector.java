package vorkurs04_annotations.projects.p04_inspector.solution;

import projectkit.Javac;
import vorkurs04_annotations.projects.p04_inspector.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * L'inspecteur : pour chaque classe du modele, il lit par reflection ce que les annotations RUNTIME
 * ont laisse. Regle d'or : l'ordre de getDeclaredFields / getDeclaredMethods / getAnnotations n'est
 * PAS garanti par la specification, on trie donc toujours avant d'afficher.
 */
public class Inspector {

    static String names(Annotation[] annotations) {
        // annotationType(), pas getClass() : getClass() rend la classe proxy generee a l'execution.
        return Arrays.stream(annotations).map(a -> a.annotationType().getSimpleName()).sorted()
                .collect(Collectors.joining(", ", "[", "]"));
    }

    /** Une annotation de type en texte : les valeurs de Range (defaut compris), le seul nom pour les autres. */
    static String text(Annotation a) {
        if (a instanceof Range r) {
            return "@Range(" + r.min() + ".." + r.max() + ")";
        }
        return "@" + a.annotationType().getSimpleName();
    }

    static String prefix(AnnotatedType t) {
        return Arrays.stream(t.getAnnotations()).map(Inspector::text).map(s -> s + " ").collect(Collectors.joining());
    }

    /**
     * Le type annote reconstruit RECURSIVEMENT : chaque sorte d'AnnotatedType a sa facon de descendre
     * dans ses parties (arguments, composant du tableau, bornes du joker).
     */
    static String render(AnnotatedType t) {
        if (t instanceof AnnotatedParameterizedType p) {
            Class<?> raw = (Class<?>) ((ParameterizedType) p.getType()).getRawType();
            String args = Arrays.stream(p.getAnnotatedActualTypeArguments()).map(Inspector::render).collect(Collectors.joining(", "));
            return prefix(t) + raw.getSimpleName() + "<" + args + ">";
        }
        if (t instanceof AnnotatedArrayType a) {
            // Pour un tableau, l'annotation du tableau LUI-MEME s'ecrit devant les crochets : String @A [].
            String own = prefix(t);
            return render(a.getAnnotatedGenericComponentType()) + (own.isEmpty() ? "" : " " + own) + "[]";
        }
        if (t instanceof AnnotatedWildcardType w) {
            AnnotatedType[] lower = w.getAnnotatedLowerBounds();
            if (lower.length > 0) {
                return prefix(t) + "? super " + render(lower[0]);
            }
            AnnotatedType upper = w.getAnnotatedUpperBounds()[0];
            boolean plain = upper.getType() == Object.class && upper.getAnnotations().length == 0;
            return prefix(t) + (plain ? "?" : "? extends " + render(upper));
        }
        if (t instanceof AnnotatedTypeVariable v) {
            return prefix(t) + ((TypeVariable<?>) v.getType()).getName();
        }
        return prefix(t) + ((Class<?>) t.getType()).getSimpleName();
    }

    static String declared(Field f) {
        // getAnnotation(Column.class) rend un objet TYPE : name() et nullable() se lisent comme des methodes,
        // et nullable() rend sa valeur par defaut quand personne ne l'a ecrite.
        StringBuilder out = new StringBuilder();
        Column column = f.getAnnotation(Column.class);
        if (column != null) {
            out.append("@Column(name=").append(column.name()).append(", nullable=").append(column.nullable()).append(") ");
        }
        if (f.isAnnotationPresent(Sensitive.class)) {
            out.append("@Sensitive ");
        }
        return out.isEmpty() ? "aucune" : out.toString().strip();
    }

    static void inspect(Class<?> c) {
        String name = c.getSimpleName();
        System.out.println("CLASSE " + name + " : declarees " + names(c.getDeclaredAnnotations()) + " ; visibles " + names(c.getAnnotations()));

        // getAnnotation(Tag.class) ne voit PAS les tags ranges dans le conteneur ; getAnnotationsByType les deballe.
        String byType = Arrays.stream(c.getAnnotationsByType(Tag.class)).map(Tag::value).collect(Collectors.joining(", ", "[", "]"));
        Tag single = c.getAnnotation(Tag.class);
        Tags container = c.getDeclaredAnnotation(Tags.class);
        System.out.println("TAGS " + name + " : par type " + byType
                + " ; getAnnotation(Tag) " + (single == null ? "absent" : single.value())
                + " ; conteneur " + (container == null ? "absent" : container.value().length + " tag(s)"));

        List<Field> fields = Arrays.stream(c.getDeclaredFields()).sorted(Comparator.comparing(Field::getName)).toList();
        for (Field f : fields) {
            System.out.println("CHAMP " + name + "." + f.getName() + " : " + render(f.getAnnotatedType()) + " ; " + declared(f));
        }

        List<Method> methods = Arrays.stream(c.getDeclaredMethods()).filter(m -> !m.isSynthetic())
                .sorted(Comparator.comparing(Method::getName)).toList();
        for (Method m : methods) {
            String tags = Arrays.stream(m.getAnnotationsByType(Tag.class)).map(Tag::value).collect(Collectors.joining(", ", "[", "]"));
            System.out.println("METHODE " + name + "." + m.getName() + " : retour " + render(m.getAnnotatedReturnType())
                    + " ; visibles " + names(m.getAnnotations()) + " ; tags " + tags);
            for (TypeVariable<Method> tv : m.getTypeParameters()) {
                String bounds = Arrays.stream(tv.getAnnotatedBounds()).map(Inspector::render).collect(Collectors.joining(" & "));
                System.out.println("TYPEPARAM " + name + "." + m.getName() + " : " + tv.getName() + " extends " + bounds);
            }
            Parameter[] params = m.getParameters();
            for (int i = 0; i < params.length; i++) {
                Parameter p = params[i];
                // Sans l'option -parameters de javac, le vrai nom n'est pas dans le .class : on lit arg0, arg1...
                System.out.println("PARAM " + name + "." + m.getName() + "#" + i + " " + p.getName()
                        + (p.isNamePresent() ? "" : " (nom absent)") + " : " + render(p.getAnnotatedType())
                        + " ; declarees " + names(p.getAnnotations()));
            }
        }
    }

    public static void main(String[] args) {
        String pkg = Inspector.class.getPackageName();
        Javac.Result model = Javac.compile(Map.of("Model", Data.MODEL.replace("{{PKG}}", pkg)));
        for (String className : Data.CLASSES) {
            inspect(Javac.load(model, className));
        }

        // Retention : Trace est dans le .class (CLASS) mais introuvable a l'execution.
        Method close = Arrays.stream(Javac.load(model, "Account").getDeclaredMethods())
                .filter(m -> m.getName().equals("close")).findFirst().orElseThrow();
        System.out.println("RETENTION : Trace sur close visible a l'execution ? " + close.isAnnotationPresent(Trace.class)
                + " ; dans le .class ? " + Javac.javap(model, "Account").contains("Trace"));
    }
}
