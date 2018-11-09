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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.inject.BindingMode;
import app.packed.inject.Inject;
import app.packed.inject.InjectionException;
import app.packed.inject.Key;
import app.packed.inject.Provider;
import app.packed.inject.Provides;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Nullable;
import packed.internal.inject.JavaXInjectSupport;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/** This class represents a method annotated with the {@link Provider} annotation. */
public final class MethodInvokerAtProvides extends MethodInvoker {

    /** The binding mode from {@link Provides#bindingMode()}. */
    private final BindingMode bindingMode;

    /** An (optional) description from {@link Provides#description()}. */
    @Nullable
    private final String description;

    /** The key under which this method will deliver services. */
    private final Key<?> key;

    MethodInvokerAtProvides(InternalMethodDescriptor descriptor, MethodHandles.Lookup lookup, Method method, Provides provides) {
        super(descriptor, lookup);

        // @Provides method cannot also have @Inject annotation
        if (JavaXInjectSupport.isInjectAnnotationPresent(method)) {
            throw new InvalidDeclarationException(cannotHaveBothAnnotations(Inject.class, Provides.class));
        }
        this.key = descriptor.fromMethodReturnType();
        this.description = provides.description().length() > 0 ? provides.description() : null;
        this.bindingMode = provides.bindingMode();
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
     * Returns the (optional) description.
     * 
     * @return the (optional) description
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * Returns the key under which the provided service will be made available.
     * 
     * @return the key under which the provided service will be made available
     */
    public Key<?> getKey() {
        return key;
    }

    public static Optional<MethodInvokerAtProvides> find(InternalMethodDescriptor method) {
        for (Annotation a : method.annotations) {
            if (a.annotationType() == Provides.class) {
                return Optional.of(read(method, (Provides) a));
            }
        }
        return Optional.empty();
    }

    private static MethodInvokerAtProvides read(InternalMethodDescriptor method, Provides provides) {
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

// static String provideHasBothQualifierAnnotationAndQualifierAttribute(AnnotationProvidesReflectionData m, Annotation
// qualifierAnnotation,
// Class<? extends Annotation> qualifierAttribute) {
// return m + " cannot both specify a qualifying annotation (@" + qualifierAnnotation.annotationType() + ") and
// qualifying attribute @Provides(qualifier="
// + qualifierAttribute.getSimpleName() + ")";
// }
