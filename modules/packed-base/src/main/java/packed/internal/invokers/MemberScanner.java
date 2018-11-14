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
package packed.internal.invokers;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import packed.internal.inject.InternalDependency;
import packed.internal.util.descriptor.AtProvides;

/**
 *
 */
// Taenker vi slaar Method + Fields sammen paa lang sigt, fint nok nu her at have 2
class MemberScanner {

    final Class<?> clazz;

    ArrayList<AccessibleField<InternalDependency>> fieldsAtInject;

    ArrayList<AccessibleField<AtProvides>> fieldsAtProvides;

    /** The lookup object. */
    final Lookup lookup;

    ArrayList<AccessibleExecutable<List<InternalDependency>>> methodsAtInject;

    ArrayList<AccessibleExecutable<AtProvides>> methodsAtProvides;

    MemberScanner(Lookup lookup, Class<?> clazz) {
        this.lookup = requireNonNull(lookup);
        this.clazz = requireNonNull(clazz);
    }

    public Collection<AccessibleField<InternalDependency>> injectableFields() {
        return fieldsAtInject == null ? List.of() : List.copyOf(fieldsAtInject);
    }

    public Collection<AccessibleExecutable<List<InternalDependency>>> injectableMethods() {
        return methodsAtInject == null ? List.of() : List.copyOf(methodsAtInject);
    }

    void scanMethods() {

        // @Provides method cannot also have @Inject annotation
        // if (JavaXInjectSupport.isInjectAnnotationPresent(method)) {
        // throw new InvalidDeclarationException(cannotHaveBothAnnotations(Inject.class, Provides.class));
        // }
    }

    void scanFields() {
        for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                Annotation[] annotations = field.getAnnotations();
                if (annotations.length > 0) {
                    // Multiple annotations
                    AccessibleField<InternalDependency> inject = MemberScanners.createIfInjectable(this, field, annotations);

                    // We need to to some checks when we have multiple annotations...
                    if (annotations.length > 1) {
                        if (inject != null) {
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
        return ms;
    }
}
