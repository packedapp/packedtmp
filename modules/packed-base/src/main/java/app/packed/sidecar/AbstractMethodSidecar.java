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
package app.packed.sidecar;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.bundle.Extension;
import packed.internal.component.source.SourceModelMethod;
import packed.internal.sidecar.SidecarModel;

/**
 *
 * <p>
 * Something about throwing {@link IllegalStateException}.
 */
// configure vs builder -> configuration (but for someone it is a builder)

// public methods vs private... This class is never exposed to users.
// So why not public
public abstract class AbstractMethodSidecar {

    /** The configuration (builder) of this sidecar. Updated by {@link SidecarModel.Builder}. */
    @Nullable
    private SourceModelMethod.Builder configuration;

    /**
     * Returns this sidecar's builder object.
     * 
     * @return this sidecar's builder object
     */
    SourceModelMethod.Builder configuration() {
        SourceModelMethod.Builder c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        }
        return c;
    }

    /** Configures this sidecar. */
    protected abstract void configure();

    /**
     * Returns a direct method handle to the method (without any intervening argument bindings or transformations that may
     * been called elsewhere on this sidecar instance).
     * 
     * @return a direct method handle to the method
     */
    public final MethodHandle directMethodHandle() {
        return configuration().methodHandle();
    }

    /** Disables the sidecar. No reference to it will be maintained at runtime. */
    public final void disable() {
        configuration().disable();
    }

    /**
     * Returns any extension the source is a member of of. Or empty if the source is not part of any extension.
     * 
     * @return any extension the source is a member of of
     */
    // It is basically whether or not the declaring class is annotated with AnnotatedMember
    public final Optional<Class<? extends Extension>> extensionMember() {
        return configuration().extensionMember();
    }

    /**
     * Returns an annotated element from the method that is being bootstrapped.
     * 
     * @see AnnotatedElement#getAnnotation(Class)
     */
    public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return configuration().methodUnsafe().getAnnotation(annotationClass);
    }

    /**
     * Returns the method that is being processed by this sidecar.
     * 
     * @return the method that is being processed by this sidecar
     */
    public final Method method() {
        return configuration().methodSafe();
    }
}
