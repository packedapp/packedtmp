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
package packed.internal.inject.annotations;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.inject.Inject;

// Bliver stadig brugt til @Inject annoteringer
public class MemberScanner {

    final Class<?> clazz;

    /** A builder for members annotated with {@link Inject}. */
    public OldAtInjectGroup.Builder inject = new OldAtInjectGroup.Builder();

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

                    // Taenker vi maaske har et map istedet for?????

                    // Vi kan ogsaa have noget bitfitleri her, vi masker alle annoteringer.
                    // Og saa koeret noget or val && INJECT_MASK > INJECT (alle annoteringen som
                    // vi ikke vil kombinere
                    // Multiple annotations
                    inject.createIfInjectable(lookup, method, annotations);

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
                    inject.createIfInjectable(lookup, field, annotations);

                }
            }
        }
    }

    public static MemberScanner forService(Class<?> clazz, Lookup lookup) {
        MemberScanner ms = new MemberScanner(lookup, clazz);
        ms.scanFields();
        ms.scanMethods();
        return ms;
    }
}
