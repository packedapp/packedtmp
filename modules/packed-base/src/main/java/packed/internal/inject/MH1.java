/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package packed.internal.inject;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import packed.internal.util.LookupUtil;

/**
 *
 */
public class MH1 {
    static final MethodHandle OPTIONAL_OF_NULLABLE = LookupUtil.mhStaticPublic(Optional.class, "ofNullable", Optional.class, Object.class);

    static final MethodHandle OPTIONAL_OF = LookupUtil.mhStaticPublic(Optional.class, "of", Optional.class, Object.class);

    public static final MethodHandle UPPER_CASE = mhOf(MH1.class, "uppercase", String.class);

    public static final MethodHandle DUP = mhOf(MH1.class, "dup", String.class);

    public static final MethodHandle CONCAT = mhOf(MH1.class, "concat", String.class, String.class);

    public static final MethodHandle LENGTH = mhOf(MH1.class, "lenght", String.class);

    public static final MethodHandle RND = mhOf(MH1.class, "rnd");
    public static final MethodHandle FROMINT = mhOf(MH1.class, "fromInt", int.class);

    public static final MethodHandle FROMINT2 = mhOf(MH1.class, "from2Int", int.class, int.class);

    public static final MethodHandle PRINTLN = mhOf(MH1.class, "println", String.class);
    public static final MethodHandle PRINTLN_OPTIONAL = mhOf(MH1.class, "println", Optional.class);

    public static final MethodHandle PRINTLN2 = mhOf(MH1.class, "println2Str", String.class, String.class);

    public static final MethodHandle PRINTLN3 = mhOf(MH1.class, "println3Str", String.class, String.class, String.class);

    static MethodHandle mhOf(Class<?> c, String name, Class<?>... arguments) {
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name) && Arrays.equals(arguments, m.getParameterTypes())) {
                try {
                    return MethodHandles.lookup().unreflect(m);
                } catch (IllegalAccessException e) {
                    throw new Error(e);
                }
            }
        }
        throw new IllegalArgumentException();
    }

    public static String uppercase(String str) {
        return str.toUpperCase();
    }

    public static String dup(String str) {
        return str + str;
    }

    public static Integer lenght(String str) {
        return str.length();
    }

    public static String rnd() {
        return UUID.randomUUID().toString();
    }

    public static String fromInt(int i) {
        return "" + i;
    }

    public static String from2Int(int i1, int i2) {
        return "" + i1 + i2;
    }

    public static void println(String str) {
        System.out.println(str);
    }

    public static void println2Str(String str1, String str2) {
        System.out.println(str1 + " - " + str2);
    }

    public static void println3Str(String str1, String str2, String str3) {
        System.out.println(str1 + " - " + str2 + " - " + str3);
    }

    public static void println(Optional<String> str) {
        System.out.println(str);
    }

    public static String concat(String s1, String s2) {
        return s1 + s2;
    }

    public static void main(String[] args) throws Throwable {
        System.out.println("------------- collectArguments ----------- ");
        collectArguments();
        System.out.println();
        System.out.println();

        System.out.println("------------- filterArguments ----------- ");
        filterArguments();
        System.out.println();
        System.out.println();
        // collectArguments();

        System.out.println("------------- foldArguments ----------- ");
        foldArguments();

        System.out.println();
        System.out.println();
        MethodHandle filtered = MethodHandles.filterArguments(PRINTLN_OPTIONAL, 0, OPTIONAL_OF_NULLABLE);
        System.out.println(filtered.type());
        filtered.invokeExact((Object) "Hej");
    }

    public static void collectArguments() throws Throwable {
        // The thing to notice is that the supplied filter(s). Are always unary functions.
        // Similar to FilterArguments. But the function can take as many arguments as it wants to
        // Jeg tror forskellen er at filterArguments tager en varargs. Saa man kan specificere flere filtre
        // Dvs filterArguments matches one parameter per methodHandle
        // collectArguments matches one or more parameters per method handle.
        System.out.println(PRINTLN.type());
        System.out.println(RND.type());
        MethodHandle p = MethodHandles.collectArguments(PRINTLN, 0, RND);
        System.out.println(p.type());
        p.invokeExact();
        System.out.println();

        p = MethodHandles.collectArguments(PRINTLN, 0, DUP);
        System.out.println(p.type());
        p.invokeExact("HEJ");
        System.out.println();

        System.out.println("Target = " + PRINTLN.type());
        System.out.println("Filter = " + CONCAT.type());
        p = MethodHandles.collectArguments(PRINTLN, 0, CONCAT);
        System.out.println("Result = " + p.type());
        p.invokeExact("HEJ", "MEDDIG");
        // p.invokeExact(123);
    }

    public static void filterArguments() throws Throwable {
        // The thing to notice is that the supplied filter(s) must always be an unary functions.
        // one parameter and one return type (which cannot be void)
        // filterType.parameterCount() == 1 && filterType.returnType() == targetType.parameterType(pos)
        // result.arity == input.arity always

        // I virkeligheden er der mere tale om en slags mappere
        // filter tager en mapper per argument
        System.out.println("Target = " + PRINTLN.type());
        System.out.println("Filter = " + FROMINT.type());
        MethodHandle p = MethodHandles.filterArguments(PRINTLN, 0, FROMINT);
        System.out.println("Result = " + p.type());

        System.out.println("");

        System.out.println("Target = " + PRINTLN2.type());
        System.out.println("Filter = 2*" + FROMINT.type());
        MethodHandle p2 = MethodHandles.filterArguments(PRINTLN2, 0, FROMINT, FROMINT);
        System.out.println("Result = " + p2.type());

        // p.invokeExact();
        p.invokeExact(123);
    }

    public static void foldArguments() throws Throwable {
        System.out.println(PRINTLN3.type());
        System.out.println(CONCAT.type());
        MethodHandle p = MethodHandles.foldArguments(PRINTLN3, CONCAT);
        System.out.println(p.type());
        p.invoke("foo", "BLA");

        p = MethodHandles.insertArguments(PRINTLN2, 1, "BLA");
        System.out.println(p.type());
        p.invoke("foo");
    }

    // FilterArguments -> ApplyUnaryFunction(s) takes an unary function and saves it on the MethodHandle
    // DropArguments -> InsertDummyArgument(s) in the resulting MH, arguments that are ignored
    // InsertArguments -> BindArgument(s) to supplied Objects

    // CollectArguments -> ApplyFunction(s) takes an function and replaces the parameter with the function
    // (function.returnType = parameter.type)
    // ----------------- Similar to FilterArguments. But the function can take as many arguments as it wants to

    // FoldArguments Fjerner argument foerste argument af target. Den kan tage en funktion imodsaetning til
    // insertArguments som tager en constant
    // F.eks. at indsaette, Optional.empty kunne vaere en fold operation. Men nu er empty en constant...

    // PermuteArguments
}
