package vorkurs04_annotations.drills.r05_generics;

import projectkit.ProjectChecker;

import java.util.List;

/**
 * Le correcteur du drill de rappel 5 (ne pas modifier). Consigne : TODO.md.
 * Lance-le tel quel pour verifier TON Recall05, ou avec l'argument "solution".
 */
public class Check {

    static final List<String> EXPECTED = List.of(
            // EXPECTED-BEGIN
            "D01 : List java.util.List<java.lang.String>",
            "D02 : Map 2 true",
            "D03 : Map Entry",
            "D04 : java.lang.Number 0 java.lang.Object java.lang.Integer",
            "D05 : true java.util.List<java.lang.String>",
            "D06 : T 2 Holder Number",
            "D07 : true false",
            "D08 : E java.util.List<E> java.util.Map<java.lang.String, ? super E> E[]",
            "D09 : java.lang.IllegalStateException Map CharSequence[]",
            "D10 : java.util.Map<java.lang.String, java.lang.Integer>",
            "D11 : java.util.function.Supplier<java.lang.Integer>",
            "D12 : 1 E V");
            // EXPECTED-END

    static final List<String> API = List.of(
            ".getType()", ".getGenericType()", "ParameterizedType", ".getRawType()", ".getActualTypeArguments()",
            ".getOwnerType()", "WildcardType", ".getUpperBounds()", ".getLowerBounds()", "GenericArrayType",
            ".getGenericComponentType()", "TypeVariable", ".getBounds()", ".getGenericDeclaration()",
            ".getTypeParameters()", ".getGenericReturnType()", ".getGenericParameterTypes()",
            ".getGenericExceptionTypes()", ".getParameterTypes()", ".getGenericSuperclass()", ".getGenericInterfaces()");

    public static void main(String[] args) throws Exception {
        ProjectChecker.check(Check.class, "Recall05", args, EXPECTED, API);
    }
}
