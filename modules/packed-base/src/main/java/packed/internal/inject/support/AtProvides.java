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

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionException;
import app.packed.inject.Key;
import app.packed.inject.Provides;
import app.packed.util.FieldDescriptor;
import app.packed.util.MethodDescriptor;
import app.packed.util.Nullable;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.JavaXInjectSupport;
import packed.internal.util.descriptor.InternalAnnotatedElement;
import packed.internal.util.descriptor.InternalFieldDescriptor;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/** A descriptor of the {@link Provides} annotation. */
public final class AtProvides {

    /** The annotated member, either an {@link InternalFieldDescriptor} or an {@link InternalMethodDescriptor}. */
    public final InternalAnnotatedElement annotatedMember;

    /** The binding mode from {@link Provides#bindingMode()}. */
    public final BindingMode bindingMode;

    /** Any dependencies (parameters) the annotated member has, is always empty for fields. */
    public final List<InternalDependency> dependencies;

    /** An (optional) description from {@link Provides#description()}. */
    @Nullable
    public final String description;

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
     * Returns whether or not the field or method on which the annotation is present is a static field or method.
     * 
     * @return whether or not the field or method on which the annotation is present is a static field or method
     */
    public boolean isStaticMember() {
        return annotatedMember instanceof FieldDescriptor ? ((FieldDescriptor) annotatedMember).isStatic() : ((MethodDescriptor) annotatedMember).isStatic();
    }

    public static Optional<AtProvides> find(InternalMethodDescriptor method) {
        for (Annotation a : method.getAnnotationsUnsafe()) {
            if (a.annotationType() == Provides.class) {
                return Optional.of(read(method, (Provides) a));
            }
        }
        return Optional.empty();
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

    public static AtProvides from(InternalMethodDescriptor method, Provides p) {
        Annotation annotation = JavaXInjectSupport.findQualifier(method, method.getAnnotations());
        Key<?> key = Key.fromTypeLiteralNullableAnnotation(method, method.getReturnTypeLiteral(), annotation);

        return new AtProvides(method, key, p, List.of());
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
