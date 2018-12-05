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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionException;
import app.packed.inject.Key;
import app.packed.inject.Provides;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Nullable;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.JavaXInjectSupport;
import packed.internal.util.ErrorMessageBuilder;
import packed.internal.util.descriptor.InternalAnnotatedElement;
import packed.internal.util.descriptor.InternalFieldDescriptor;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/**
 * Information about fields and methods annotated with {@link Provides}. Used for both services, components, import and
 * export stages.
 */
public final class ProvidesSupport {

    /** All fields annotated with {@link Provides}. */
    public final Collection<AccessibleField<AtProvides>> fields;

    /** All methods annotated with {@link Provides}. */
    public final Collection<AccessibleExecutable<AtProvides>> methods;

    /** A set of all keys provided. */
    public final Map<Key<?>, AtProvides> keys;

    public final boolean hasInstanceMembers;

    ProvidesSupport(Builder builder) {
        this.methods = builder.methods == null ? List.of() : List.copyOf(builder.methods);
        this.fields = builder.fields == null ? List.of() : List.copyOf(builder.fields);
        this.keys = builder.keys == null ? Map.of() : Map.copyOf(builder.keys);
        this.hasInstanceMembers = builder.hasInstanceMembers;
    }

    public boolean hasProvidingMembers() {
        return keys.size() > 0;
    }

    public static class Builder {

        private static final ProvidesSupport EMPTY = new ProvidesSupport(new Builder());

        ArrayList<AccessibleField<AtProvides>> fields;

        ArrayList<AccessibleExecutable<AtProvides>> methods;

        HashMap<Key<?>, AtProvides> keys;

        boolean hasInstanceMembers;

        public ProvidesSupport build() {
            if (fields == null && methods == null) {
                return EMPTY;
            }
            // TODO check that we do not have multiple fields/methods with the same key....
            return new ProvidesSupport(this);
        }

        AccessibleExecutable<AtProvides> forMethod(Lookup lookup, Method method, Annotation[] annotations) {
            for (Annotation a : annotations) {
                if (a.annotationType() == Provides.class) {
                    InternalMethodDescriptor descriptor = InternalMethodDescriptor.of(method);
                    hasInstanceMembers |= !descriptor.isStatic();
                    if (methods == null) {
                        methods = new ArrayList<>(1);
                        if (keys == null) {
                            keys = new HashMap<>();
                        }
                    }
                    AtProvides ap = AtProvides.from(descriptor, (Provides) a);
                    if (Modifier.isPrivate(method.getModifiers())) {
                        lookup = lookup.in(method.getDeclaringClass());
                    }
                    AccessibleExecutable<AtProvides> fi = new AccessibleExecutable<>(descriptor, lookup, ap);

                    Key<?> key = fi.metadata().key;
                    if (keys.putIfAbsent(key, fi.metadata()) != null) {
                        throw new InvalidDeclarationException(ErrorMessageBuilder.of(method.getDeclaringClass())
                                .cannot("have multiple members providing services with the same key (" + key.toStringSimple() + ").")
                                .toResolve("either remove @Provides on one of the members, or use a unique qualifier for each of the members"));
                    }
                    methods.add(fi);
                    return fi;
                }
            }
            return null;
        }

        AccessibleField<AtProvides> forField(Lookup lookup, Field field, Annotation[] annotations) {
            for (Annotation a : annotations) {
                if (a.annotationType() == Provides.class) {
                    InternalFieldDescriptor descriptor = InternalFieldDescriptor.of(field);
                    hasInstanceMembers |= !descriptor.isStatic();
                    if (fields == null) {
                        fields = new ArrayList<>(1);
                        if (keys == null) {
                            keys = new HashMap<>();
                        }
                    }
                    AtProvides ap = AtProvides.from(descriptor, (Provides) a);
                    if (Modifier.isPrivate(field.getModifiers())) {
                        lookup = lookup.in(field.getDeclaringClass());
                    }
                    AccessibleField<AtProvides> fi = new AccessibleField<>(descriptor, lookup, ap);

                    Key<?> key = fi.metadata().key;
                    if (keys.putIfAbsent(key, fi.metadata()) != null) {
                        throw new InvalidDeclarationException(ErrorMessageBuilder.of(field.getDeclaringClass())
                                .cannot("have multiple members providing services with the same key (" + key.toStringSimple() + ").")
                                .toResolve("either remove @Provides on one of the members, or use a unique qualifier for each of the members"));
                    }
                    fields.add(fi);
                    return fi;
                }
            }
            return null;
        }
    }

    /** A descriptor of the {@link Provides} annotation. */
    public static final class AtProvides {

        /** The annotated member, either an {@link InternalFieldDescriptor} or ab {@link InternalMethodDescriptor}. */
        private final InternalAnnotatedElement annotatedMember;

        /** The binding mode from {@link Provides#bindingMode()}. */
        private final BindingMode bindingMode;

        /** If the annotation is present on a method, any dependencies (parameters) the method has. */
        private final List<InternalDependency> dependencies;

        /** An (optional) description from {@link Provides#description()}. */
        @Nullable
        private final String description;

        /** The key under which the provided service will be made available. */
        public final Key<?> key;

        AtProvides(InternalAnnotatedElement annotatedMember, Key<?> key, Provides provides, List<InternalDependency> dependencies) {
            this.annotatedMember = requireNonNull(annotatedMember);
            this.key = requireNonNull(key, "key is null");
            this.description = provides.description().length() > 0 ? provides.description() : null;
            this.bindingMode = provides.bindingMode();
            this.dependencies = requireNonNull(dependencies);
        }

        /**
         * Returns a {@link InternalFieldDescriptor} or {@link InternalMethodDescriptor} for the annotated field or method.
         * 
         * @return a descriptor of the annotated member
         */
        public InternalAnnotatedElement getAnnotatedMember() {
            return annotatedMember;
        }

        /**
         * Returns the binding mode as defined by {@link Provides#bindingMode()}.
         *
         * @return the binding mode
         */
        public BindingMode getBindingMode() {
            return bindingMode;
        }

        /**
         * Returns a list of any dependencies the annotated method might have. Is always empty for fields.
         * 
         * @return a list of any dependencies the annotated method might have
         */
        public List<InternalDependency> getDependencies() {
            return dependencies;
        }

        /**
         * Returns the (optional) description.
         * 
         * @return the (optional) description
         */
        @Nullable
        public String getDescription() {
            return description;
        }

        public static AtProvides from(InternalMethodDescriptor method, Provides p) {
            Annotation annotation = JavaXInjectSupport.findQualifier(method, method.getAnnotations());
            Key<?> key = Key.fromTypeLiteralNullableAnnotation(method, method.getReturnTypeLiteral(), annotation);

            return new AtProvides(method, key, p, List.of());
        }

        public static AtProvides from(InternalFieldDescriptor field, Provides p) {
            Annotation annotation = JavaXInjectSupport.findQualifier(field, field.getAnnotations());
            Key<?> key = Key.fromTypeLiteralNullableAnnotation(field, field.getTypeLiteral(), annotation);

            return new AtProvides(field, key, p, List.of());

            // Extract key
            // Men vi skal jo have informationer om hvorfor

            // Saa metoder ved hvorfor, the caller knows where/what
            // WHERE/What could not because of why...
            // Maybe have a isValidKey(Type) or <T> checkValidKey(T extends RuntimeException, String message) throws T;
            // Maybe have a string with "%s, %s".. Maybe A consumer with the message because XYZ
            // because it "xxxxxx"

        }

        public static Optional<AtProvides> find(InternalMethodDescriptor method) {
            for (Annotation a : method.getAnnotationsUnsafe()) {
                if (a.annotationType() == Provides.class) {
                    return Optional.of(read(method, (Provides) a));
                }
            }
            return Optional.empty();
        }

        private static AtProvides read(InternalMethodDescriptor method, Provides provides) {
            // Cannot have @Inject and @Provides on the same method
            if (JavaXInjectSupport.isInjectAnnotationPresent(method)) {
                throw new InjectionException("Cannot place both @Inject and @" + Provides.class.getSimpleName() + " on the same method, method = " + method);
            }

            Class<?> returnType = method.getReturnType();
            if (returnType == void.class || returnType == Void.class || returnType == Optional.class || returnType == OptionalInt.class
                    || returnType == OptionalLong.class || returnType == OptionalDouble.class) {
                throw new InjectionException("@Provides method " + method + " cannot have " + returnType.getSimpleName() + " as a return type");
            }

            // If factory, class, TypeLiteral, Provider, we need special handling

            // Optional<Annotation> qualifier = method.findQualifiedAnnotation();
            // if (qualifier.isPresent()) {
            // key = Key.of(method.getGenericReturnType(), qualifier.get());
            // } else {
            // key = Key.of(method.getGenericReturnType());
            // }

            throw new UnsupportedOperationException();
            // return new MethodInvokerAtProvidesDescriptor(key, provides.bindingMode(), provides.description().length() == 0 ? null
            // : provides.description());
        }
    }

}
