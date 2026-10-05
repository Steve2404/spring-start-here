package vorkurs04_annotations.drills.r06_access.solution;

import projectkit.Javac;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Corrige du drill 6 : le controle d'acces de la reflection (0.4.12). Les classes cibles sont
 * compilees a part (paquet par defaut) : depuis notre paquet, leur prive est vraiment prive.
 */
public class Recall06 {

    static final String SAFE = """
            public class Safe {
                private int pin = 1;
                private final String owner = new String("lea");
                private final int code = 7;
                private static final int MAX = 3;
                public static String motto = "ok";
                private Safe() {}
                public Safe(int pin) { this.pin = pin; }
                private String open() { return "ouvert"; }
                public int code() { return code; }
            }
            """;

    static String tryRun(ThrowingSupplier op) {
        try {
            return String.valueOf(op.get());
        } catch (ReflectiveOperationException | RuntimeException e) {
            return e.getClass().getSimpleName();
        }
    }

    interface ThrowingSupplier {
        Object get() throws ReflectiveOperationException;
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        Javac.Result r = Javac.compile(Map.of("Safe", SAFE));
        Class<?> safe = Javac.load(r, "Safe");
        Object s = safe.getConstructor(int.class).newInstance(42);

        // D01 : trouver un membre prive marche ; l'utiliser sans forcer, non.
        Field pin = safe.getDeclaredField("pin");
        System.out.println("D01 : " + pin.getName() + " " + pin.canAccess(s) + " " + tryRun(() -> pin.get(s)));
        // D02 : trySetAccessible repond oui/non ; ensuite get marche, et canAccess change.
        System.out.println("D02 : " + pin.trySetAccessible() + " " + pin.get(s) + " " + pin.canAccess(s));
        // D03 : un champ public static : canAccess(null).
        Field motto = safe.getField("motto");
        System.out.println("D03 : " + motto.canAccess(null) + " " + motto.get(null));
        // D04 : final d'instance : modifiable une fois force ; static final : jamais.
        Field owner = safe.getDeclaredField("owner");
        owner.trySetAccessible();
        owner.set(s, "tom");
        Field max = safe.getDeclaredField("MAX");
        max.trySetAccessible();
        System.out.println("D04 : " + owner.get(s) + " " + tryRun(() -> {
            max.set(null, 9);
            return "ecrit";
        }));
        // D05 : constante de compilation : le champ change, le code compile garde 7.
        Field code = safe.getDeclaredField("code");
        code.trySetAccessible();
        code.set(s, 99);
        System.out.println("D05 : " + code.get(s) + " " + safe.getMethod("code").invoke(s));
        // D06 : constructeur et methode prives.
        Constructor<?> hidden = safe.getDeclaredConstructor();
        Method open = safe.getDeclaredMethod("open");
        System.out.println("D06 : " + tryRun(hidden::newInstance) + " " + hidden.trySetAccessible() + " "
                + hidden.newInstance().getClass().getSimpleName() + " " + open.trySetAccessible() + " " + open.invoke(s));
        // D07 : un record : ses champs ne se modifient jamais par reflection.
        record Point(int x) {
        }
        Field x = Point.class.getDeclaredField("x");
        x.trySetAccessible();
        System.out.println("D07 : " + tryRun(() -> {
            x.set(new Point(1), 2);
            return "ecrit";
        }));
        // D08 : java.base n'ouvre pas java.lang : trySetAccessible dit false, setAccessible(true) leve.
        Field value = String.class.getDeclaredField("value");
        String forced;
        try {
            value.setAccessible(true);
            forced = "accepte";
        } catch (InaccessibleObjectException e) {
            forced = e.getClass().getSimpleName();
        }
        System.out.println("D08 : " + value.trySetAccessible() + " " + forced);
        // D09 : exporte (API publique) n'est pas ouvert (reflection profonde).
        Module base = String.class.getModule();
        System.out.println("D09 : " + base.getName() + " " + base.isExported("java.lang") + " " + base.isOpen("java.lang")
                + " " + safe.getModule().isNamed());
        // D10 : lire les modificateurs.
        System.out.println("D10 : " + Modifier.toString(max.getModifiers()) + " | " + Modifier.isPrivate(hidden.getModifiers()));
    }
}
