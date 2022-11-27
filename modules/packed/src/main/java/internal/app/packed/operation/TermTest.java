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
package internal.app.packed.operation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import app.packed.operation.CapturingOp;
import app.packed.operation.Op0;

/**
 *
 */
public record TermTest(Method method, MethodHandle mh) {

    public static void main(String[] args) {
        analyze(new Op0<String>(() -> "") {});
    }

    static void analyze(Object o) {
        analyze(o.getClass());
    }

    static void analyze(Class<?> t) {
        // Altsaa jeg ved ikke om vi spiller tiden ved ikke at afvente og se hvad der kommer med generiks

        // Taenker vi bare supportere et niveau for nu...
        
        Class<?> baseClass = t.getSuperclass();
        while (baseClass.getSuperclass() != CapturingOp.class) {
            baseClass = baseClass.getSuperclass();
        }

        // Maaske er det fint at smide en error?
        Constructor<?>[] con = baseClass.getDeclaredConstructors();
        if (con.length != 1) {
            throw new Error(baseClass + " must declare a single constructor");
        }
        Constructor<?> c = con[0];
        if (c.getParameterCount() != 1) {
            throw new Error(baseClass + " must declare a single constructor taking a single parameter");
        }

        Parameter p = c.getParameters()[0];

        Class<?> samType = p.getType();
        System.out.println(samType);
        Method samMethod = null;
        for (Method m : samType.getMethods()) {
            if (!m.isDefault()) {
                if (samMethod != null) {
                    throw new Error();
                }
                samMethod = m;
            }
        }
        if (samMethod == null) {
            throw new Error();
        }
        // check SAM interface type

        // For now we require that the Single Abstract Method must be on a public available class
        MethodHandle mh;
        try {
            mh = MethodHandles.publicLookup().unreflect(samMethod);
        } catch (IllegalAccessException e) {
            throw new Error(samMethod + " must be accessible via MethodHandles.publicLookup()", e);
        }
        
       // Type type = TypeVariableExtractor.of(baseClass, baseClassTypeVariableIndex).extract(actualClass);
        System.out.println(samMethod.getDeclaringClass());
        System.out.println(mh);
    }
}
