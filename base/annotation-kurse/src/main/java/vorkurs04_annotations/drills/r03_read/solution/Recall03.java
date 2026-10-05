package vorkurs04_annotations.drills.r03_read.solution;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Corrige du drill 3 : toute l'API de LECTURE des annotations (0.4.8). Les annotations et les classes
 * a inspecter sont imbriquees ici : une annotation imbriquee est implicitement static.
 */
public class Recall03 {

    @Inherited @Retention(RetentionPolicy.RUNTIME) @interface Audit {
    }

    @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
    @interface Col {
        String name();

        boolean nullable() default true;
    }

    @Retention(RetentionPolicy.RUNTIME) @Repeatable(Tags.class) @interface Tag {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME) @interface Tags {
        Tag[] value();
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER) @interface Param {
    }

    @interface Hidden {
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE_USE) @interface NN {
    }

    @Audit @Tag("a") @Tag("b") static class Base {
        @Col(name = "id") long id;
        @Col(name = "ref") long ref;
        @NN String @NN [] matrix;
        List<@NN String> names;

        @Col(name = "ctor") Base() {
        }

        @Tag("solo") @Hidden void run(@Param String a, int b) {
        }
    }

    static class Child extends Base {
    }

    @Audit interface Marked {
    }

    static class Impl implements Marked {
    }

    record Point(@Col(name = "x") int x) {
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        // D01 : @Inherited : visible sur la sous-classe, mais pas DECLAREE sur elle.
        System.out.println("D01 : " + Base.class.isAnnotationPresent(Audit.class) + " " + Child.class.isAnnotationPresent(Audit.class)
                + " " + Child.class.getDeclaredAnnotation(Audit.class));
        // D02 : jamais herite via une interface.
        System.out.println("D02 : " + Impl.class.isAnnotationPresent(Audit.class));
        // D03 : lire les elements, valeur par defaut comprise.
        Col id = Base.class.getDeclaredField("id").getAnnotation(Col.class);
        System.out.println("D03 : " + id.name() + " " + id.nullable());
        // D04 : annotationType() donne le vrai type ; getClass() est une classe proxy.
        System.out.println("D04 : " + id.annotationType().getSimpleName() + " " + Proxy.isProxyClass(id.getClass()));
        // D05 : deux @Tag -> stockes dans le conteneur.
        System.out.println("D05 : " + Base.class.getAnnotation(Tag.class) + " " + Base.class.getAnnotationsByType(Tag.class).length
                + " " + Base.class.getAnnotation(Tags.class).value().length);
        // D06 : un seul @Tag -> pas de conteneur.
        Method run = Base.class.getDeclaredMethod("run", String.class, int.class);
        System.out.println("D06 : " + run.getAnnotation(Tag.class).value() + " " + run.getAnnotation(Tags.class));
        // D07 : annotations des parametres : un tableau PAR parametre.
        System.out.println("D07 : " + run.getParameterAnnotations()[0].length + " " + run.getParameterAnnotations()[1].length
                + " " + run.getParameters()[0].isAnnotationPresent(Param.class));
        // D08 : constructeurs.
        System.out.println("D08 : " + Base.class.getDeclaredConstructor().getAnnotation(Col.class).name());
        // D09 : retention CLASS (par defaut) : invisible.
        System.out.println("D09 : " + run.isAnnotationPresent(Hidden.class) + " " + run.getAnnotations().length);
        // D10 : annotation de type dans un argument generique.
        Field names = Base.class.getDeclaredField("names");
        AnnotatedParameterizedType p = (AnnotatedParameterizedType) names.getAnnotatedType();
        System.out.println("D10 : " + names.getAnnotations().length + " " + p.getAnnotatedActualTypeArguments()[0].isAnnotationPresent(NN.class));
        // D11 : "@NN String @NN []" : une annotation sur le tableau, une sur son element.
        AnnotatedArrayType a = (AnnotatedArrayType) Base.class.getDeclaredField("matrix").getAnnotatedType();
        System.out.println("D11 : " + a.getAnnotations().length + " " + a.getAnnotatedGenericComponentType().getAnnotations().length);
        // D12 : deux annotations de memes valeurs sont egales (equals par valeurs).
        Col ref = Base.class.getDeclaredField("ref").getAnnotation(Col.class);
        Col id2 = Base.class.getDeclaredField("id").getAnnotation(Col.class);
        System.out.println("D12 : " + id.equals(id2) + " " + id.equals(ref));
        // D13 : getAnnotations (heritees comprises) contre getDeclaredAnnotations, sur Child.
        System.out.println("D13 : " + Child.class.getAnnotations().length + " " + Child.class.getDeclaredAnnotations().length);
        // D14 : composant de record : @Col (FIELD/METHOD/CONSTRUCTOR) se propage au champ et a l'accesseur, pas au composant.
        System.out.println("D14 : " + Point.class.getDeclaredField("x").isAnnotationPresent(Col.class)
                + " " + Point.class.getDeclaredMethod("x").isAnnotationPresent(Col.class)
                + " " + Point.class.getRecordComponents()[0].isAnnotationPresent(Col.class));
    }
}
