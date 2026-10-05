package vorkurs04_annotations.projects.p07_access.solution;

import projectkit.Javac;
import vorkurs04_annotations.projects.p07_access.Data;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * L'auditeur d'acces : TROUVER un membre (getDeclared...) ne donne PAS le droit de l'utiliser.
 * Pour chaque operation : on la tente d'abord SANS forcer, puis on demande l'acces avec
 * trySetAccessible() (qui repond oui/non sans exception), puis on retente.
 */
public class AccessAuditor {

    /** Une operation qu'on peut retenter : elle peut echouer de facon "reflection". */
    interface Attempt {
        String run() throws ReflectiveOperationException;
    }

    static String describe(Throwable t) {
        return t.getClass().getSimpleName();
    }

    /** Sans forcer, puis forcer seulement si c'est un refus d'acces ; on raconte chaque etape. */
    static String attempt(AccessibleObject member, Attempt op) {
        String refused;
        try {
            return "direct -> " + op.run();
        } catch (IllegalAccessException e) {
            refused = "sans forcer " + describe(e) + " ; ";
        } catch (InvocationTargetException e) {
            // La reflection a reussi, c'est le CODE appele qui a echoue : la vraie cause est emballee.
            return "direct -> la cible a leve " + describe(e.getCause()) + " (" + e.getCause().getMessage() + ")";
        } catch (ReflectiveOperationException e) {
            return "direct -> " + describe(e);
        }
        if (!member.trySetAccessible()) {
            return refused + "trySetAccessible false (module ferme)";
        }
        try {
            return refused + "force -> " + op.run();
        } catch (ReflectiveOperationException e) {
            // Meme force : un static final, ou le champ d'un record, restent non modifiables.
            return refused + "force -> " + describe(e);
        }
    }

    static Object convert(String text, Class<?> type) {
        return type == int.class ? Integer.parseInt(text) : text;
    }

    static String probe(String line, Map<String, Class<?>> classes, Map<Class<?>, Object> receivers) {
        String[] parts = line.split(" ");
        String op = parts[0];
        String[] target = parts[1].split("\\.");
        Class<?> type = classes.get(target[0]);
        try {
            switch (op) {
                case "CREATE" -> {
                    Constructor<?> c = type.getDeclaredConstructor();
                    // Un constructeur n'a pas de receveur : canAccess(null).
                    String access = c.canAccess(null) ? "accessible" : "inaccessible";
                    return access + " ; " + attempt(c, () -> "objet " + c.newInstance().getClass().getSimpleName());
                }
                case "READ", "WRITE" -> {
                    Field f = type.getDeclaredField(target[1]);
                    Object receiver = Modifier.isStatic(f.getModifiers()) ? null : receivers.get(type);
                    String access = f.canAccess(receiver) ? "accessible" : "inaccessible";
                    if (op.equals("READ")) {
                        return access + " ; " + attempt(f, () -> String.valueOf(f.get(receiver)));
                    }
                    Object value = convert(parts[2], f.getType());
                    return access + " ; " + attempt(f, () -> {
                        f.set(receiver, value);
                        return "ecrit";
                    });
                }
                default -> {
                    Method m = Arrays.stream(type.getDeclaredMethods()).filter(x -> x.getName().equals(target[1])).findFirst()
                            .orElseThrow(() -> new NoSuchMethodException(target[1]));
                    Object receiver = Modifier.isStatic(m.getModifiers()) ? null : receivers.get(type);
                    Object[] args = Arrays.copyOfRange(parts, 2, parts.length, Object[].class);
                    String access = m.canAccess(receiver) ? "accessible" : "inaccessible";
                    return access + " ; " + attempt(m, () -> String.valueOf(m.invoke(receiver, args)));
                }
            }
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            // Echec de la DECOUVERTE : on n'est meme pas arrive a la question de l'acces.
            return "introuvable (" + describe(e) + ")";
        }
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        Javac.Result compiled = Javac.compile(Data.TARGETS);
        Map<String, Class<?>> classes = new HashMap<>();
        for (String name : Data.TARGETS.keySet()) {
            classes.put(name, Javac.load(compiled, name));
        }
        classes.put("String", String.class);

        // Les objets sur lesquels on lit/ecrit : construits par leur constructeur PUBLIC.
        Map<Class<?>, Object> receivers = new HashMap<>();
        receivers.put(classes.get("Vault"), classes.get("Vault").getConstructor(String.class).newInstance("abc"));
        receivers.put(classes.get("Point"), classes.get("Point").getConstructor(int.class, int.class).newInstance(1, 2));
        receivers.put(String.class, "hello");

        for (String line : Data.PROBES) {
            System.out.println(line + " : " + probe(line, classes, receivers));
        }

        // setAccessible(true) sur java.base : pas de "non" poli, une exception.
        try {
            String.class.getDeclaredField("value").setAccessible(true);
            System.out.println("FORCE String.value : accepte");
        } catch (InaccessibleObjectException e) {
            System.out.println("FORCE String.value : " + describe(e));
        } catch (NoSuchFieldException e) {
            System.out.println("FORCE String.value : " + describe(e));
        }

        // Les modules expliquent la difference : java.lang est EXPORTE (public utilisable) mais pas OUVERT (reflection profonde).
        Module ours = classes.get("Vault").getModule();
        Module base = String.class.getModule();
        System.out.println("MODULE Vault : nomme " + ours.isNamed() + " ; MODULE String : " + base.getName()
                + " ; java.lang exporte " + base.isExported("java.lang") + " ; ouvert a Vault " + base.isOpen("java.lang", ours));
        List<String> mods = List.of(Modifier.toString(classes.get("Vault").getDeclaredField("VERSION").getModifiers()),
                Modifier.toString(classes.get("Point").getDeclaredField("x").getModifiers()));
        System.out.println("MODIFICATEURS VERSION : " + mods.get(0) + " ; Point.x : " + mods.get(1));
    }
}
