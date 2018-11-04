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
package packed.internal.inject.reflect;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import app.packed.inject.BindingMode;
import app.packed.inject.Inject;
import app.packed.inject.Key;
import app.packed.inject.Provider;
import app.packed.inject.Provides;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Nullable;
import packed.internal.inject.JavaXInjectSupport;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/** This class represents a method annotated with the {@link Provider} annotation. */
public final class AnnotatedMethodProvides extends MethodInvoker {

    /** The caching mode of this node. */
    private final BindingMode cachingMode;

    /** An optional description of the annotated method. */
    private final String description;

    /** The key. */
    private final Key<?> key;

    AnnotatedMethodProvides(InternalMethodDescriptor descriptor, MethodHandles.Lookup lookup, Method method, Provides provides) {
        super(descriptor, lookup);

        // @Provides method cannot also have @Inject annotation
        if (JavaXInjectSupport.isInjectAnnotationPresent(method)) {
            throw new InvalidDeclarationException(cannotHaveBothAnnotations(Inject.class, Provides.class));
        }
        this.key = descriptor.fromMethodReturnType();
        this.description = provides.description().length() > 0 ? provides.description() : null;
        this.cachingMode = provides.bindingMode();
    }

    /**
     * Returns the binding mode.
     * 
     * @return the binding mode
     */
    public BindingMode getBindingMode() {
        return cachingMode;
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
     * Returns the key
     * 
     * @return
     */
    public Key<?> getKey() {
        return key;
    }
}

// static String provideHasBothQualifierAnnotationAndQualifierAttribute(AnnotationProvidesReflectionData m, Annotation
// qualifierAnnotation,
// Class<? extends Annotation> qualifierAttribute) {
// return m + " cannot both specify a qualifying annotation (@" + qualifierAnnotation.annotationType() + ") and
// qualifying attribute @Provides(qualifier="
// + qualifierAttribute.getSimpleName() + ")";
// }
