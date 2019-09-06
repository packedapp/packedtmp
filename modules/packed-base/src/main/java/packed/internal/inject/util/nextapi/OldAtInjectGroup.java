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
package packed.internal.inject.util.nextapi;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import app.packed.inject.Inject;
import app.packed.inject.ServiceDependency;
import app.packed.reflect.FieldDescriptor;
import app.packed.reflect.MethodDescriptor;
import app.packed.util.Nullable;
import packed.internal.inject.factoryhandle.ExecutableFactoryHandle;
import packed.internal.inject.factoryhandle.FieldFactoryHandle;

/** A group of injectable fields and methods. */
public final class OldAtInjectGroup {

    /** An empty inject group. */
    private static final OldAtInjectGroup EMPTY = new OldAtInjectGroup(new Builder());

    /** All fields annotated with {@link Inject}. */
    public final List<OldAtInject> fields;

    /** All non-static methods annotated with {@link Inject}. */
    public final List<OldAtInject> methods;

    /**
     * Creates a new group from a builder.
     * 
     * @param builder
     *            the builder used to create the group
     */
    private OldAtInjectGroup(Builder builder) {
        this.methods = builder.methods == null ? List.of() : List.copyOf(builder.methods);
        this.fields = builder.fields == null ? List.of() : List.copyOf(builder.fields);
    }

    /** A builder object for {@link OldAtInjectGroup}. */
    public static final class Builder {

        /** All fields annotated with {@link Inject}. */
        private ArrayList<OldAtInject> fields;

        /** All non-static methods annotated with {@link Inject}. */
        private ArrayList<OldAtInject> methods;

        /**
         * Creates a new group from this builder.
         * 
         * @return the new group
         */
        public OldAtInjectGroup build() {
            if (fields == null && methods == null) {
                return EMPTY;
            }
            return new OldAtInjectGroup(this);
        }

        @Nullable
        public OldAtInject createIfInjectable(Lookup lookup, Field field, Annotation[] annotations) {
            OldAtInject result = null;
            if (isInjectAnnotationPresent(annotations)) {
                FieldDescriptor descriptor = FieldDescriptor.of(field);

                if (fields == null) {
                    fields = new ArrayList<>();
                }

                fields.add(result = new OldAtInject(new FieldFactoryHandle<>(descriptor).withLookup(lookup),
                        List.of(ServiceDependency.fromVariable(descriptor))));
            }
            return result;
        }

        @Nullable
        public OldAtInject createIfInjectable(Lookup lookup, Method method, Annotation[] annotations) {
            OldAtInject result = null;
            if (isInjectAnnotationPresent(annotations)) {
                MethodDescriptor descriptor = MethodDescriptor.of(method);
                // static @Inject methods are treated like factory methods, and captured elsewhere

                if (methods == null) {
                    methods = new ArrayList<>();
                }
                methods.add(result = new OldAtInject(new ExecutableFactoryHandle<>(descriptor).withLookup(lookup),
                        ServiceDependency.fromExecutable(descriptor)));
            }
            return result;
        }

        public static boolean isInjectAnnotationPresent(Annotation[] annotations) {
            for (Annotation an : annotations) {
                if (an.annotationType() == Inject.class) {
                    return true;
                }
            }
            return false;
        }
    }

}
