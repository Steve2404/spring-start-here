package vorkurs04_annotations.projects.p08_codegen.solution;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

/**
 * Le processor : javac l'appelle a CHAQUE round avec les elements annotes de ce round. Il valide
 * (Messager -> vraies erreurs de compilation), puis genere une source (Filer). Une source generee
 * declenche un nouveau round ; le dernier round (processingOver) n'apporte plus rien.
 */
@SupportedOptions("builder.suffix")
public class BuilderProcessor extends AbstractProcessor {

    private int rounds;
    private String suffix;

    public int rounds() {
        return rounds;
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        // init est appele UNE fois : on y lit les options -A une bonne fois pour toutes.
        super.init(env);
        suffix = env.getOptions().getOrDefault("builder.suffix", "Builder");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        // Le nom QUALIFIE de l'annotation : le paquet change entre ton code et la solution.
        return Set.of(Builder.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // Sans cela, javac avertit que le processor ne gere que Java 6.
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        rounds++;
        for (Element element : round.getElementsAnnotatedWith(Builder.class)) {
            if (element.getKind() != ElementKind.RECORD) {
                error(element, "@Builder exige un record, pas " + element.getKind().toString().toLowerCase()
                        + (element.getModifiers().contains(Modifier.ABSTRACT) ? " abstract" : ""));
                continue;
            }
            TypeElement record = (TypeElement) element;
            List<? extends RecordComponentElement> components = record.getRecordComponents();
            if (components.isEmpty()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "record sans composant : aucun builder", record);
                continue;
            }
            boolean clash = false;
            for (RecordComponentElement c : components) {
                if (c.getSimpleName().contentEquals("build")) {
                    // Sur ce JDK, un message attache a un RecordComponentElement n'a PAS de position (ligne 0) :
                    // on l'attache au record, qui en a une.
                    error(record, "le composant " + c.getSimpleName() + " cache la methode build()");
                    clash = true;
                }
            }
            if (!clash) {
                generate(record, components);
            }
        }
        // false : on ne "consomme" pas l'annotation, d'autres processors pourraient la traiter aussi.
        return false;
    }

    private void error(Element element, String message) {
        // Une ERREUR du Messager fait echouer la compilation, et pointe la ligne de l'element.
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void generate(TypeElement record, List<? extends RecordComponentElement> components) {
        String name = record.getSimpleName() + suffix;
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(name, record);
            try (PrintWriter out = new PrintWriter(file.openWriter())) {
                out.println("public class " + name + " {");
                for (RecordComponentElement c : components) {
                    // asType() donne le TypeMirror du composant ; son texte est le type complet (java.util.List<java.lang.String>).
                    out.println("    private " + c.asType() + " " + c.getSimpleName() + ";");
                }
                for (RecordComponentElement c : components) {
                    out.println("    public " + name + " " + c.getSimpleName() + "(" + c.asType() + " value) { this."
                            + c.getSimpleName() + " = value; return this; }");
                }
                String args = String.join(", ", components.stream().map(c -> c.getSimpleName().toString()).toList());
                out.println("    public " + record.getSimpleName() + " build() { return new " + record.getSimpleName() + "(" + args + "); }");
                out.println("}");
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "builder genere : " + name, record);
        } catch (FilerException e) {
            // Creer deux fois le meme fichier est refuse par le Filer.
            error(record, "fichier deja genere : " + name);
        } catch (IOException e) {
            error(record, "ecriture impossible : " + e);
        }
    }
}
