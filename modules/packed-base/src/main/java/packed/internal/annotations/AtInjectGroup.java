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
package packed.internal.annotations;

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
import packed.internal.util.descriptor.InternalFieldDescriptor;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/**
 * A group of injectable fields and methods.
 */
public final class AtInjectGroup {

    /** An empty inject group. */
    private static final AtInjectGroup EMPTY = new AtInjectGroup(new Builder());

    /** All fields annotated with {@link Inject}. */
    public final List<AtDependable> fields;

    /** All non-static methods annotated with {@link Inject}. */
    public final List<AtDependable> methods;

    /**
     * Creates a new group from a builder.
     * 
     * @param builder
     *            the builder used to create the group
     */
    private AtInjectGroup(Builder builder) {
        this.methods = builder.methods == null ? List.of() : List.copyOf(builder.methods);
        this.fields = builder.fields == null ? List.of() : List.copyOf(builder.fields);
    }

    /** A builder object for {@link AtInjectGroup}. */
    public static final class Builder {

        /** All fields annotated with {@link Inject}. */
        private ArrayList<AtDependable> fields;

        /** All non-static methods annotated with {@link Inject}. */
        private ArrayList<AtDependable> methods;

        /**
         * Creates a new group from this builder.
         * 
         * @return the new group
         */
        public AtInjectGroup build() {
            if (fields == null && methods == null) {
                return EMPTY;
            }
            return new AtInjectGroup(this);
        }

        @Nullable
        public AtDependable createIfInjectable(Lookup lookup, Field field, Annotation[] annotations) {
            AtDependable result = null;
            if (JavaXInjectSupport.isInjectAnnotationPresent(annotations)) {
                InternalFieldDescriptor descriptor = InternalFieldDescriptor.of(field);
                Checks.checkAnnotatedFieldIsNotStatic(descriptor, Inject.class);
                Checks.checkAnnotatedFieldIsNotFinal(descriptor, Inject.class);

                if (fields == null) {
                    fields = new ArrayList<>();
                }
                fields.add(result = new AtDependable(descriptor.newInvoker(lookup), List.of(InternalDependency.of(descriptor))));
            }
            return result;
        }

        @Nullable
        public AtDependable createIfInjectable(Lookup lookup, Method method, Annotation[] annotations) {
            AtDependable result = null;
            if (JavaXInjectSupport.isInjectAnnotationPresent(annotations)) {
                InternalMethodDescriptor descriptor = InternalMethodDescriptor.of(method);
                // static @Inject methods are treated like factory methods, and captured elsewhere

                if (methods == null) {
                    methods = new ArrayList<>();
                }
                methods.add(result = new AtDependable(descriptor.newInvoker(lookup), InternalDependency.fromExecutable(descriptor)));
            }
            return result;
        }
    }
}
