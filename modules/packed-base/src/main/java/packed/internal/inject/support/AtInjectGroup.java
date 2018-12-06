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
package packed.internal.inject.support;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import app.packed.inject.Inject;
import app.packed.util.Nullable;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.JavaXInjectSupport;
import packed.internal.inject.function.ExecutableInvoker;
import packed.internal.inject.function.FieldInvoker;
import packed.internal.util.descriptor.InternalFieldDescriptor;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/**
 *
 */
public final class AtInjectGroup {

    /** All fields annotated with {@link Inject}. */
    public final List<AtInject> fields;

    /** All non-static methods annotated with {@link Inject}. */
    public final List<AtInject> methods;

    /** An empty group. */
    private static final AtInjectGroup EMPTY = new AtInjectGroup(new Builder());

    private AtInjectGroup(Builder builder) {
        this.methods = builder.methods == null ? List.of() : List.copyOf(builder.methods);
        this.fields = builder.fields == null ? List.of() : List.copyOf(builder.fields);
    }

    public static class Builder {

        /** All fields annotated with {@link Inject}. */
        ArrayList<AtInject> fields;

        /** All non-static methods annotated with {@link Inject}. */
        ArrayList<AtInject> methods;

        @Nullable
        public AtInject createIfInjectable(Lookup lookup, Field field, Annotation[] annotations) {
            if (JavaXInjectSupport.isInjectAnnotationPresent(annotations)) {
                InternalFieldDescriptor descriptor = InternalFieldDescriptor.of(field);
                Checks.checkAnnotatedFieldIsNotStatic(descriptor, Inject.class);
                Checks.checkAnnotatedFieldIsNotFinal(descriptor, Inject.class);

                FieldInvoker<?> fii = new FieldInvoker<>(descriptor).withLookup(lookup);

                AtInject ai = new AtInject(fii, InternalDependency.of(descriptor));

                if (fields == null) {
                    fields = new ArrayList<>(2);
                }
                fields.add(ai);
                return ai;
            }
            return null;
        }

        @Nullable
        public AtInject createIfInjectable(Lookup lookup, Method method, Annotation[] annotations) {
            if (JavaXInjectSupport.isInjectAnnotationPresent(annotations)) {
                InternalMethodDescriptor descriptor = InternalMethodDescriptor.of(method);

                ExecutableInvoker<?> fii = new ExecutableInvoker<>(descriptor).withLookup(lookup);

                AtInject ai = new AtInject(fii, InternalDependency.fromExecutable(descriptor));
                if (methods == null) {
                    methods = new ArrayList<>(2);
                }
                methods.add(ai);
                return ai;
            }
            return null;
        }

        /**
         * @return
         */
        public AtInjectGroup build() {
            if (fields == null && methods == null) {
                return EMPTY;
            }
            return new AtInjectGroup(this);
        }
    }
}
