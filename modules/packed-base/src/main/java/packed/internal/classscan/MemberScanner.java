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
package packed.internal.classscan;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.inject.Inject;
import app.packed.inject.Provides;
import packed.internal.inject.support.AtInject;
import packed.internal.inject.support.AtInjectGroup;
import packed.internal.inject.support.AtProvidesGroup;

/**
 *
 */
// Taenker vi slaar Method + Fields sammen paa lang sigt, fint nok nu her at have 2
// https://github.com/classgraph/classgraph
class MemberScanner {

    final Class<?> clazz;

    /** A builder for members annotated with {@link Provides}. */
    AtProvidesGroup.Builder provides = new AtProvidesGroup.Builder();

    /** A builder for members annotated with {@link Inject}. */
    AtInjectGroup.Builder inject = new AtInjectGroup.Builder();

    /** The lookup object. */
    final Lookup lookup;

    MemberScanner(Lookup lookup, Class<?> clazz) {
        this.lookup = requireNonNull(lookup);
        this.clazz = requireNonNull(clazz);
    }

    void scanMethods() {
        // optimize for classes extending Object.
        // No overridden....well except default methods on interfaces....

        // also optimize for

        // @Provides method cannot also have @Inject annotation
        // if (JavaXInjectSupport.isInjectAnnotationPresent(method)) {
        // throw new InvalidDeclarationException(cannotHaveBothAnnotations(Inject.class, Provides.class));
        // }
        for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
            for (Method method : c.getDeclaredMethods()) {
                Annotation[] annotations = method.getAnnotations();
                if (annotations.length > 0) {
                    // Multiple annotations
                    AtInject fInject = inject.createIfInjectable(lookup, method, annotations);

                    provides.addIfAnnotated(lookup, method, annotations);

                    // We need to to some checks when we have multiple annotations...
                    if (annotations.length > 1) {
                        if (fInject != null) {
                            System.out.println("OOPS");
                        }
                    }
                }
            }
        }

    }

    void scanFields() {
        for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                Annotation[] annotations = field.getAnnotations();
                if (annotations.length > 0) {
                    // Multiple annotations
                    AtInject fInject = inject.createIfInjectable(lookup, field, annotations);

                    provides.addIfAnnotated(lookup, field, annotations);

                    // We need to to some checks when we have multiple annotations...
                    if (annotations.length > 1) {
                        if (fInject != null) {
                            System.out.println("OOPS");
                        }
                    }
                }
            }
        }
    }

    public static MemberScanner forComponent(Class<?> clazz, Lookup lookup) {
        throw new UnsupportedOperationException();
    }

    static MemberScanner forService(Class<?> clazz, Lookup lookup) {
        MemberScanner ms = new MemberScanner(lookup, clazz);
        ms.scanFields();
        ms.scanMethods();
        return ms;
    }
}
