package vorkurs04_annotations.projects.p08_codegen.solution;

import projectkit.Javac;
import vorkurs04_annotations.projects.p08_codegen.Data;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Le banc d'essai du processor : chaque lot est compile avec un BuilderProcessor NEUF ; on affiche ce
 * que javac et le processor ont dit, ce qui a ete genere, puis on UTILISE le code genere.
 */
public class CodeGen {

    public static void main(String[] args) throws ReflectiveOperationException {
        String pkg = CodeGen.class.getPackageName();
        for (Data.Batch batch : Data.BATCHES) {
            Map<String, String> sources = new LinkedHashMap<>();
            batch.sources().forEach((name, src) -> sources.put(name, src.replace("{{PKG}}", pkg)));
            BuilderProcessor processor = new BuilderProcessor();
            Javac.Result result = Javac.compile(sources, batch.options(), processor);

            System.out.println("LOT " + batch.name() + " : succes " + result.success() + " ; rounds " + processor.rounds()
                    + " ; genere " + result.generated().keySet());
            // Les messages du Messager sont des diagnostics comme les autres : code compiler.*.proc.messager.
            result.diags().stream()
                    .filter(d -> d.code().endsWith("proc.messager"))
                    .forEach(d -> System.out.println("  " + d.kind() + " " + d.file() + " l." + d.line() + " : " + d.message()));

            if (batch.name().equals("ok") && result.success()) {
                // Le code genere est du VRAI code : on le charge et on l'utilise par reflection.
                Class<?> builderType = Javac.load(result, "PersonBuilder");
                List<String> methods = Arrays.stream(builderType.getDeclaredMethods()).map(Method::getName).sorted().toList();
                System.out.println("  PersonBuilder : " + methods);
                Object builder = builderType.getConstructor().newInstance();
                builderType.getMethod("name", String.class).invoke(builder, "Lea");
                builderType.getMethod("age", int.class).invoke(builder, 30);
                builderType.getMethod("tags", List.class).invoke(builder, List.of("java", "xml"));
                System.out.println("  build() -> " + builderType.getMethod("build").invoke(builder));
                Object demo = Javac.load(result, "UsePerson").getDeclaredMethod("demo").invoke(null);
                System.out.println("  UsePerson.demo() -> " + demo);
            }
            if (batch.name().equals("maker")) {
                String code = result.generated().values().stream().collect(Collectors.joining());
                System.out.println("  PointMaker declare build : " + code.contains("public Point build()"));
            }
        }
    }
}
