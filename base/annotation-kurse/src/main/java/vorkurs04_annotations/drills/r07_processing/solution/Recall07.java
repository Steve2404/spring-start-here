package vorkurs04_annotations.drills.r07_processing.solution;

import projectkit.Javac;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Corrige du drill 7 : l'API de l'annotation processing (0.4.13), avec UN petit processor
 * configurable dont on observe le comportement a chaque defi.
 */
public class Recall07 {

    @Retention(RetentionPolicy.SOURCE)
    public @interface Mark {
    }

    /** Ce que le processor a observe. */
    static final List<String> SEEN = new ArrayList<>();

    static class Probe extends AbstractProcessor {
        final String mode;
        int rounds;
        String option;

        Probe(String mode) {
            this.mode = mode;
        }

        @Override
        public synchronized void init(ProcessingEnvironment env) {
            super.init(env);
            option = env.getOptions().get("greeting");
        }

        @Override
        public Set<String> getSupportedAnnotationTypes() {
            return Set.of(Mark.class.getCanonicalName());
        }

        @Override
        public SourceVersion getSupportedSourceVersion() {
            return mode.equals("old") ? SourceVersion.RELEASE_8 : SourceVersion.latestSupported();
        }

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
            rounds++;
            if (env.processingOver()) {
                SEEN.add("fin round " + rounds);
                return claims();
            }
            Set<? extends Element> marked = env.getElementsAnnotatedWith(Mark.class);
            SEEN.add("round " + rounds + " racines " + env.getRootElements().size() + " marques " + marked.size());
            for (Element e : marked) {
                switch (mode) {
                    case "kinds" -> SEEN.add(e.getKind() + " " + e.getSimpleName());
                    case "types" -> e.getEnclosedElements().stream()
                            .filter(x -> x instanceof VariableElement)
                            .forEach(x -> SEEN.add(x.getSimpleName() + " " + x.asType() + " " + x.asType().getKind()));
                    case "members" -> SEEN.add(e.getEnclosedElements().stream().map(x -> x.getKind().toString()).sorted()
                            .collect(Collectors.joining(" ")));
                    case "error" -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "refuse", e);
                    case "warn" -> processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "attention", e);
                    case "generate", "twice" -> generate(e, mode.equals("twice") ? 2 : 1);
                    default -> {
                    }
                }
            }
            return claims();
        }

        /** true = "ces annotations sont a moi" ; false les laisse aux processors suivants (et javac le signale). */
        private boolean claims() {
            return !mode.equals("unclaimed");
        }

        private void generate(Element e, int times) {
            for (int i = 0; i < times; i++) {
                try (Writer w = processingEnv.getFiler().createSourceFile(e.getSimpleName() + "Gen").openWriter()) {
                    w.write("public class " + e.getSimpleName() + "Gen {}\n");
                } catch (FilerException ex) {
                    SEEN.add("FilerException");
                } catch (IOException ex) {
                    SEEN.add("IOException");
                }
            }
        }
    }

    static String run(String mode, String source, List<String> options) {
        SEEN.clear();
        Probe probe = new Probe(mode);
        Javac.Result r = Javac.compile(Map.of("S", source), options, probe);
        String codes = r.diags().stream().map(Javac.Diag::code).distinct().collect(Collectors.joining(" "));
        return r.success() + " | " + (SEEN.isEmpty() ? "jamais appele" : String.join(" ; ", SEEN)) + (codes.isEmpty() ? "" : " | " + codes)
                + (probe.option == null ? "" : " | option " + probe.option);
    }

    public static void main(String[] args) {
        String imp = "import " + Mark.class.getCanonicalName() + ";\n";
        String one = imp + "@Mark class A { int n; java.util.List<String> names; void m() {} }";
        // D01 : un round de travail, puis le round final (processingOver) : 2 appels.
        System.out.println("D01 : " + run("count", one, List.of()));
        // D02 : une source generee relance un round ; elle y est la seule racine, sans @Mark.
        System.out.println("D02 : " + run("generate", one, List.of()));
        // D03 : getElementsAnnotatedWith rend TOUS les elements marques, pas que les classes.
        System.out.println("D03 : " + run("kinds", imp + "@Mark class A { @Mark int n; @Mark void m() {} }", List.of()));
        // D04 : le TypeMirror garde les generiques (pas d'effacement a la compilation).
        System.out.println("D04 : " + run("types", one, List.of()));
        // D05 : le constructeur par defaut, ajoute par javac, est deja un membre visible.
        System.out.println("D05 : " + run("members", one, List.of()));
        // D06 : une ERROR du Messager ne leve rien : javac finit les rounds, puis echoue.
        System.out.println("D06 : " + run("error", one, List.of()));
        // D07 : un WARNING laisse la compilation reussir.
        System.out.println("D07 : " + run("warn", one, List.of()));
        // D08 : piege, FilerException herite d'IOException : l'attraper en premier.
        System.out.println("D08 : " + run("twice", one, List.of()));
        // D09 : l'option est lue, mais non declaree (getSupportedOptions) : javac avertit.
        System.out.println("D09 : " + run("count", one, List.of("-Agreeting=salut")));
        // D10 : une version trop ancienne n'est qu'un avertissement.
        System.out.println("D10 : " + run("old", one, List.of()));
        // D11 : aucune annotation supportee dans les sources : process n'est jamais appele.
        System.out.println("D11 : " + run("count", "class B {}", List.of()));
        // D12 : return false = annotations non reclamees ; aucun autre processor : avertissement.
        System.out.println("D12 : " + run("unclaimed", one, List.of()));
    }
}
