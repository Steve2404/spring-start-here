package vorkurs04_annotations.projects.p08_codegen;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du projet 8 (ne pas modifier). Il execute TON CodeGen.main, compare sa sortie a
 * EXPECTED, puis verifie la pratique de l'annotation processing (0.4.13).
 *
 * Enonce : TODO.md. Argument "solution" : verifie la solution. Dossier de travail : base/annotation-kurse.
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "LOT ok : succes true ; rounds 3 ; genere [PersonBuilder.java, PointBuilder.java]",
            "  WARNING Empty l.4 : record sans composant : aucun builder",
            "  NOTE Person l.5 : builder genere : PersonBuilder",
            "  NOTE Point l.4 : builder genere : PointBuilder",
            "  PersonBuilder : [age, build, name, tags]",
            "  build() -> Person[name=Lea, age=30, tags=[java, xml]]",
            "  UsePerson.demo() -> Person[name=Ines, age=41, tags=[ops]]",
            "LOT bad : succes false ; rounds 2 ; genere []",
            "  ERROR Shape l.4 : @Builder exige un record, pas class abstract",
            "  ERROR Task l.4 : le composant build cache la methode build()",
            "LOT maker : succes true ; rounds 3 ; genere [PointMaker.java]",
            "  NOTE Point l.4 : builder genere : PointMaker",
            "  PointMaker declare build : true");
            // EXPECTED-END

    static final List<String> API = List.of(
            "RetentionPolicy.SOURCE", "extends AbstractProcessor", "ProcessingEnvironment", "RoundEnvironment",
            "getSupportedAnnotationTypes", "getSupportedSourceVersion", "SourceVersion.latestSupported()",
            "@SupportedOptions(", ".getOptions()", ".getElementsAnnotatedWith(",
            ".getKind()", "ElementKind.RECORD", ".getModifiers()", "Modifier.ABSTRACT", ".getRecordComponents()",
            ".getSimpleName()", ".asType()",
            ".getMessager()", ".printMessage(", "Diagnostic.Kind.ERROR", "Diagnostic.Kind.WARNING", "Diagnostic.Kind.NOTE",
            ".getFiler()", ".createSourceFile(", ".openWriter()", "FilerException",
            "Javac.compile(", ".generated()", ".success()", "Data.BATCHES", "Javac.load(", ".invoke(", ".newInstance(");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "CodeGen", args, EXPECTED, API);
    }
}
