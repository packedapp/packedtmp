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
package packed.internal.container.extension.hook;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.UnaryOperator;

import packed.internal.util.security.SecurityChecks;

/**
 *
 */
public final class FunctionalInterfaceModel {

    /** A cache of functional interface descriptors. */
    static final ClassValue<FunctionalInterfaceModel> MODEL_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected FunctionalInterfaceModel computeValue(Class<?> functionalInterface) {
            return new FunctionalInterfaceModel(functionalInterface);
        }
    };

    public final Class<?> clazz;

    /** Whether or not the class is restricted. If the class is restricted */
    public final boolean isRestricted;

    public final Method method;

    public MethodHandle methodHandle;

    private FunctionalInterfaceModel(Class<?> clazz) {
        this.clazz = requireNonNull(clazz);
        if (!clazz.isInterface()) {
            throw new IllegalArgumentException(clazz + " is not an interface");
        }

        this.isRestricted = SecurityChecks.isRestrictedClass(clazz);
        this.method = findSingleMethod(clazz);

        // Ahh.. for helvede...
        // Vi bliver noedt til at supportere gemme dem per extension model....
        // eftersom en extension maaske har adgang til et hemmeligt interface...
        // Det er ivirkeligheden nok den der laver FieldOperatoren...

        // check interface

        // Maybe FieldOperator needs to take a MethodHandle.lookup object if none public SAM type...
        // I think it is very rare for the users of said interface not to be the same as the extension
        // Which should have provided read access to
    }

    public static FunctionalInterfaceModel get(Class<?> interfaze) {
        return MODEL_CACHE.get(interfaze);
    }

    public static void main(String[] args) {
        System.out.println(get(UnaryOperator.class).method);
    }

    private static Method findSingleMethod(Class<?> clazz) {
        Method ms = null;
        for (Method m : clazz.getMethods()) {
            if (!m.isDefault() && !Modifier.isStatic(m.getModifiers())) {
                if (ms != null) {
                    throw new IllegalArgumentException(clazz + " contains more than 1 non-default non-static methods");
                }
                ms = m;
            }
        }
        if (ms == null) {
            throw new IllegalArgumentException(clazz + " does not contain any non-default non-static methods");
        }
        return ms;
    }
}
