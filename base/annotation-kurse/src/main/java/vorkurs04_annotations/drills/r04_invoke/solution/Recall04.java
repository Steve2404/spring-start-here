package vorkurs04_annotations.drills.r04_invoke.solution;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Corrige du drill 4 : Method.invoke dans tous ses etats (0.4.10).
 */
public class Recall04 {

    static class Animal {
        public String speak() {
            return "...";
        }

        public static int twice(int n) {
            return n * 2;
        }

        public long widen(long n) {
            return n + 1;
        }

        public String kind(int n) {
            return "int";
        }

        public String kind(Integer n) {
            return "Integer";
        }

        public void touch() {
        }

        public static int count(String... words) {
            return words.length;
        }

        public int fail() {
            throw new IllegalStateException("ko");
        }

        private String secret() {
            return "s";
        }
    }

    static class Dog extends Animal {
        @Override
        public String speak() {
            return "wouf";
        }
    }

    static String error(Method m, Object receiver, Object... args) {
        try {
            return String.valueOf(m.invoke(receiver, args));
        } catch (IllegalArgumentException e) {
            return "IllegalArgumentException";
        } catch (InvocationTargetException e) {
            return "cause " + e.getCause().getClass().getSimpleName();
        } catch (IllegalAccessException e) {
            return "IllegalAccessException";
        }
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        Class<Animal> a = Animal.class;
        // D01 : static -> receveur null.
        System.out.println("D01 : " + a.getMethod("twice", int.class).invoke(null, 21));
        // D02 : la Method vient d'Animal, mais le receveur est un Dog : liaison dynamique.
        System.out.println("D02 : " + a.getMethod("speak").invoke(new Dog()));
        // D03 : la surcharge se choisit au getMethod, par les types de parametres.
        System.out.println("D03 : " + a.getMethod("kind", int.class).invoke(new Animal(), 1)
                + " " + a.getMethod("kind", Integer.class).invoke(new Animal(), 1));
        // D04 : invoke accepte un elargissement (Integer -> long), pas un retrecissement.
        Method widen = a.getMethod("widen", long.class);
        System.out.println("D04 : " + widen.invoke(new Animal(), 5) + " " + error(a.getMethod("twice", int.class), null, 5L));
        // D05 : un retour primitif revient emballe.
        Object r = a.getMethod("twice", int.class).invoke(null, 4);
        System.out.println("D05 : " + a.getMethod("twice", int.class).getReturnType() + " " + r.getClass().getSimpleName());
        // D06 : void -> null.
        System.out.println("D06 : " + a.getMethod("touch").invoke(new Animal()));
        // D07 : varargs = UN argument tableau ; le cast en Object evite qu'invoke l'eclate.
        Method count = a.getMethod("count", String[].class);
        System.out.println("D07 : " + count.invoke(null, (Object) new String[]{"a", "b", "c"}) + " " + error(count, null, "a", "b"));
        // D08 : la methode cible a leve : InvocationTargetException emballe la cause.
        System.out.println("D08 : " + error(a.getMethod("fail"), new Animal()));
        // D09 : mauvais nombre d'arguments, receveur null pour une methode d'instance.
        System.out.println("D09 : " + error(a.getMethod("touch"), new Animal(), 1) + " " + nullReceiver(a.getMethod("touch")));
        // D10 : getMethod ne voit pas une methode privee.
        System.out.println("D10 : " + missing(a));
        // D11 : getMethods = publiques HERITEES comprises ; getDeclaredMethods = toutes celles de la classe, privees comprises.
        System.out.println("D11 : " + Dog.class.getDeclaredMethods().length + " "
                + Dog.class.getMethod("twice", int.class).getDeclaringClass().getSimpleName());
        // D12 : modificateurs, varargs, nombre de parametres.
        System.out.println("D12 : " + Modifier.isStatic(count.getModifiers()) + " " + count.isVarArgs() + " " + count.getParameterCount());
        // D13 : classes imbriquees = "nestmates" : le prive reste accessible par reflection entre elles.
        Method secret = a.getDeclaredMethod("secret");
        System.out.println("D13 : " + secret.canAccess(new Animal()) + " " + secret.invoke(new Animal()));
    }

    static String nullReceiver(Method m) throws IllegalAccessException, InvocationTargetException {
        try {
            m.invoke(null);
            return "ok";
        } catch (NullPointerException e) {
            return "NullPointerException";
        }
    }

    static String missing(Class<?> c) {
        try {
            c.getMethod("secret");
            return "trouvee";
        } catch (NoSuchMethodException e) {
            return "NoSuchMethodException";
        }
    }
}
