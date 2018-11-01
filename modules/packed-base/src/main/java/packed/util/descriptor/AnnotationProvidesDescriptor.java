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
package packed.util.descriptor;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionException;
import app.packed.inject.Key;
import app.packed.inject.Provides;
import packed.inject.JavaXInjectSupport;

/**
 *
 */
public final class AnnotationProvidesDescriptor {

    /** The bind mode from {@link Provides#bindMode()}. */
    private final BindingMode bindMode;

    /** An (optional) description from {@link Provides#description()}. */
    private final String description;

    private final Key<?> key;

    AnnotationProvidesDescriptor(Key<?> key, BindingMode bindMode, String description) {
        this.key = requireNonNull(key);
        this.bindMode = requireNonNull(bindMode);
        this.description = description;
    }

    /**
     * Return the bind mode as defined on {@link Provides#bindMode()}.
     *
     * @return the bind mode
     */
    public BindingMode getBindMode() {
        return bindMode;
    }

    public String getDescription() {
        return description;
    }

    public Key<?> getKey() {
        return key;
    }

    static Optional<AnnotationProvidesDescriptor> find(InternalMethodDescriptor method) {
        for (Annotation a : method.annotations) {
            if (a.annotationType() == Provides.class) {
                return Optional.of(read(method, (Provides) a));
            }
        }
        return Optional.empty();
    }

    private static AnnotationProvidesDescriptor read(InternalMethodDescriptor method, Provides provides) {
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

        Key<?> key;
        Optional<Annotation> qualifier = method.findQualifiedAnnotation();
        if (qualifier.isPresent()) {
            key = Key.of(method.getGenericReturnType(), qualifier.get());
        } else {
            key = Key.of(method.getGenericReturnType());
        }

        return new AnnotationProvidesDescriptor(key, provides.bindMode(), provides.description().length() == 0 ? null : provides.description());
    }
}
