package vorkurs04_annotations.drills.r05_generics.solution;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Corrige du drill 5 : la reflection sur les generiques (0.4.11). getTypeName() est permis ici
 * (c'est un drill d'API), mais on sait aussi descendre dans l'arbre a la main.
 */
public class Recall05 {

    static class Holder<T extends Number & Comparable<T>> {
        List<String> words;
        List<Integer> counts;
        Map<String, List<Integer>> index;
        List<? extends Number> numbers;
        List<? super Integer> sinks;
        List<String>[] pages;
        Map.Entry<String, Integer> entry;
        T value;

        @SafeVarargs
        private <E extends CharSequence> List<E> pick(Map<String, ? super E> source, E... extra) throws IllegalStateException {
            return null;
        }
    }

    abstract static class TypeRef<X> {
        Type captured() {
            return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }
    }

    static class IntSupplier implements Supplier<Integer> {
        public Integer get() {
            return 1;
        }
    }

    static Type generic(String field) throws NoSuchFieldException {
        return Holder.class.getDeclaredField(field).getGenericType();
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        // D01 : getType = type EFFACE ; getGenericType = type complet.
        Field words = Holder.class.getDeclaredField("words");
        System.out.println("D01 : " + words.getType().getSimpleName() + " " + words.getGenericType().getTypeName());
        // D02 : ParameterizedType : type brut et arguments (un argument peut etre lui-meme parametre).
        ParameterizedType index = (ParameterizedType) generic("index");
        System.out.println("D02 : " + ((Class<?>) index.getRawType()).getSimpleName() + " "
                + index.getActualTypeArguments().length + " " + (index.getActualTypeArguments()[1] instanceof ParameterizedType));
        // D03 : type proprietaire d'un type imbrique.
        ParameterizedType entry = (ParameterizedType) generic("entry");
        System.out.println("D03 : " + ((Class<?>) entry.getOwnerType()).getSimpleName() + " " + ((Class<?>) entry.getRawType()).getSimpleName());
        // D04 : jokers : bornes haute et basse.
        WildcardType ext = (WildcardType) ((ParameterizedType) generic("numbers")).getActualTypeArguments()[0];
        WildcardType sup = (WildcardType) ((ParameterizedType) generic("sinks")).getActualTypeArguments()[0];
        System.out.println("D04 : " + ext.getUpperBounds()[0].getTypeName() + " " + ext.getLowerBounds().length
                + " " + sup.getUpperBounds()[0].getTypeName() + " " + sup.getLowerBounds()[0].getTypeName());
        // D05 : un tableau de type parametre est un GenericArrayType.
        Type pages = generic("pages");
        System.out.println("D05 : " + (pages instanceof GenericArrayType) + " " + ((GenericArrayType) pages).getGenericComponentType().getTypeName());
        // D06 : une variable de type : nom, bornes, qui la declare.
        TypeVariable<?> t = (TypeVariable<?>) generic("value");
        System.out.println("D06 : " + t.getName() + " " + t.getBounds().length + " " + ((Class<?>) t.getGenericDeclaration()).getSimpleName()
                + " " + Holder.class.getDeclaredField("value").getType().getSimpleName());
        // D07 : effacement : meme Class a l'execution, types generiques differents.
        Field counts = Holder.class.getDeclaredField("counts");
        System.out.println("D07 : " + (words.getType() == counts.getType()) + " " + words.getGenericType().equals(counts.getGenericType()));
        // D08 : la signature generique d'une methode.
        Method pick = Arrays.stream(Holder.class.getDeclaredMethods()).filter(m -> m.getName().equals("pick")).findFirst().orElseThrow();
        System.out.println("D08 : " + pick.getTypeParameters()[0].getName() + " " + pick.getGenericReturnType().getTypeName() + " "
                + Arrays.stream(pick.getGenericParameterTypes()).map(Type::getTypeName).collect(Collectors.joining(" ")));
        // D09 : exceptions generiques et types effaces des parametres (E s'efface en sa borne).
        System.out.println("D09 : " + pick.getGenericExceptionTypes()[0].getTypeName() + " "
                + Arrays.stream(pick.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(" ")));
        // D10 : jeton de type : une sous-classe ANONYME garde l'argument de type de sa super-classe.
        Type captured = new TypeRef<Map<String, Integer>>() {
        }.captured();
        System.out.println("D10 : " + captured.getTypeName());
        // D11 : interfaces generiques implementees.
        System.out.println("D11 : " + IntSupplier.class.getGenericInterfaces()[0].getTypeName());
        // D12 : les parametres de type d'une classe.
        System.out.println("D12 : " + Holder.class.getTypeParameters().length + " " + List.class.getTypeParameters()[0].getName()
                + " " + Map.class.getTypeParameters()[1].getName());
    }
}
