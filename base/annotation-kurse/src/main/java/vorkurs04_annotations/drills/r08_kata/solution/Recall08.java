package vorkurs04_annotations.drills.r08_kata.solution;

import projectkit.Javac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Corrige du kata 8 : tout le chapitre 0.4 melange, sur un petit modele.
 */
public class Recall08 {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Repeatable(Roles.class)
    @interface Role {
        String value();
    }

    // Le conteneur doit avoir une retention au moins aussi longue et des cibles incluses.
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @interface Roles {
        Role[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Limit {
        int max();

        String unit() default "x";
    }

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @interface Audited {
    }

    // Retention CLASS (le defaut) : dans le .class, mais invisible a la reflection.
    @interface Internal {
    }

    record Pair<A, B>(A left, B right) {
    }

    @Audited
    @Internal
    @Role("admin")
    @Role("ops")
    static class Service {
        @Limit(max = 3)
        private int retries = 1;
        @Limit(max = 100, unit = "ms")
        int timeout = 50;
        Pair<String, List<Integer>> last;

        @Role("ops")
        public String restart(String why) {
            return "restart:" + why;
        }

        public String status() {
            return "up";
        }
    }

    static class Child extends Service {
        @Override
        public String restart(String why) {
            return "child:" + why;
        }
    }

    static String codes(String source) {
        return Javac.compile(Map.of("S", source)).diags().stream()
                .filter(d -> d.kind().equals("ERROR")).map(Javac.Diag::code).collect(Collectors.joining(" "));
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        // D01 : un membre d'annotation n'accepte que primitif, String, Class, enum, annotation ou tableau de ceux-ci.
        System.out.println("D01 : " + codes("@interface Bad { Object value(); }") + " | "
                + codes("import java.lang.annotation.*; @Target(ElementType.METHOD) @interface M {} @M class X {}"));

        // D02 : repetee, l'annotation est rangee dans son conteneur : getAnnotation(Role) rend null.
        Role[] roles = Service.class.getAnnotationsByType(Role.class);
        System.out.println("D02 : " + roles.length + " " + (Service.class.getAnnotation(Role.class) == null) + " "
                + Arrays.stream(roles).map(Role::value).collect(Collectors.joining(",")));

        // D03 : @Inherited ne vaut que pour les classes ; Roles n'est pas @Inherited.
        System.out.println("D03 : " + Child.class.isAnnotationPresent(Audited.class) + " "
                + Child.class.getDeclaredAnnotations().length + " " + Child.class.isAnnotationPresent(Roles.class));

        // D04 : getDeclaredFields n'a pas d'ordre garanti : trier par nom.
        Field[] fields = Service.class.getDeclaredFields();
        Arrays.sort(fields, Comparator.comparing(Field::getName));
        System.out.println("D04 : " + Arrays.stream(fields).filter(f -> f.isAnnotationPresent(Limit.class))
                .map(f -> f.getName() + "<=" + f.getAnnotation(Limit.class).max() + f.getAnnotation(Limit.class).unit())
                .collect(Collectors.joining(" ")));

        // D05 : retention CLASS invisible ; la valeur par defaut se lit sur la methode de l'annotation.
        System.out.println("D05 : " + Service.class.isAnnotationPresent(Internal.class) + " "
                + Limit.class.getDeclaredMethod("unit").getDefaultValue());

        // D06 : trouver puis invoquer les methodes marquees.
        Service service = new Service();
        StringBuilder d06 = new StringBuilder();
        for (Method m : Service.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Role.class)) {
                d06.append(m.getName()).append(" ").append(m.invoke(service, "panne"));
            }
        }
        System.out.println("D06 : " + d06);

        // D07 : ecrire puis valider : get/set travaillent sur le receveur ; un int revient en Integer.
        Service.class.getDeclaredField("retries").set(service, 5);
        System.out.println("D07 : " + Arrays.stream(fields).filter(f -> f.isAnnotationPresent(Limit.class))
                .map(f -> {
                    try {
                        int v = f.getInt(service);
                        return f.getName() + " " + v + " " + (v <= f.getAnnotation(Limit.class).max() ? "ok" : "trop");
                    } catch (IllegalAccessException e) {
                        return f.getName() + " inaccessible";
                    }
                }).collect(Collectors.joining(" | ")));

        // D08 : getGenericType garde les arguments ; un composant de record a un type generique aussi.
        ParameterizedType last = (ParameterizedType) Service.class.getDeclaredField("last").getGenericType();
        String args08 = Arrays.stream(last.getActualTypeArguments()).map(Type::getTypeName).collect(Collectors.joining(" "));
        String comps = Arrays.stream(Pair.class.getRecordComponents())
                .map(c -> c.getName() + ":" + c.getGenericType().getTypeName()).collect(Collectors.joining(" "));
        System.out.println("D08 : " + args08 + " | " + comps);

        // D09 : deux annotations sont egales si meme type et memes valeurs, quel que soit l'endroit.
        Role onMethod = Service.class.getMethod("restart", String.class).getAnnotation(Role.class);
        System.out.println("D09 : " + onMethod.equals(roles[1]) + " " + onMethod.annotationType().getSimpleName());

        // D10 : invoke suit la liaison dynamique ; les annotations de methode ne s'heritent jamais.
        Method restart = Service.class.getMethod("restart", String.class);
        System.out.println("D10 : " + restart.invoke(new Child(), "x") + " "
                + Child.class.getMethod("restart", String.class).isAnnotationPresent(Role.class));
    }
}
