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

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.cube.Extension;
import packed.internal.component.source.SourceModelMethod;
import packed.internal.sidecar.SidecarModel;

/**
 * Packed creates a single instance of a subclass and runs the {@link #configure()} method.
 */
// implements AnnotatedElement

// Skal metoderne vaere protected????
// Ikke hvis man skal kunne specificere den til extensions...
public abstract class MethodSidecar {

    /** The builder of this sidecar. Updated by {@link SidecarModel.Builder}. */
    @Nullable
    private SourceModelMethod.Builder configuration;

    protected final <T> void attach(Class<T> key, T instance) {
        attach(Key.of(key), instance);
    }

    protected final <T> void attach(Key<T> key, T instance) {}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final void attach(Object instance) {
        requireNonNull(instance, "instance is null");
        attach((Class) instance.getClass(), instance);
    }

    protected final void bindParameterMh(int index, MethodHandle mh) {

    }

    /**
     * Returns this sidecar's builder object.
     * 
     * @return this sidecar's builder object
     */
    private SourceModelMethod.Builder configuration() {
        SourceModelMethod.Builder c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        }
        return c;
    }

    /**
     * Configures the the sidecar
     */
    protected abstract void configure();

    /** Disables the sidecar. No reference to it will be maintained at runtime. */
    protected final void disable() {
        configuration.disable();
    }

    /**
     * Returns any extension the source is a member of of. Or empty if the source is not part of any extension.
     * 
     * @return any extension the source is a member of of
     */
    // It is basically whether or not the declaring class is annotated with AnnotatedMember
    protected final Optional<Class<? extends Extension>> extensionMember() {
        return configuration().extensionMember();
    }

    protected final void forEachUnbound(Consumer<? super VariableBinder> action) {

    }

    /**
     * Returns an annotated element from the method that is being bootstrapped.
     * 
     * @see AnnotatedElement#getAnnotation(Class)
     */
    protected final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return configuration.methodUnsafe().getAnnotation(annotationClass);
    }

    /**
     * Returns the method that is being bootstrapped.
     * 
     * @return the method that is being bootstrapped
     */
    protected final Method method() {
        return configuration.methodSafe();
    }

    /**
     * 
     * @throws IllegalStateException
     *             if called from outside of {@link #configure()}
     */
    protected final void provideInvoker() {
        throw new UnsupportedOperationException();
    }

    // MethodHandle must take
    protected final void returnTypeTransform(MethodHandle mh, Class<?>... injections) {
        // Kunne maaske godt taenke mig noget tekst???
        // Skal vi have en klasse??
        // Naar vi skal have et visuelt overblik engang?
        // Maaske er det nok at kunne se sidecaren...

        // Vi kan jo sende aben videre til andre sidecars. Saa tror ogsaa
        // vi skal kunne transformere variablen...

        // VarTransformer // add annotations, set type literal, remove annotations

        // Altsaa det er jo fuldstaendig som method handle... Vi har behov for de samme ting...
        // insert, drop, ...
        // Ved ikke om vi supporter multiple variable transformers...

        // Must take existing class value as single parameter
        // and injections as subsequent values
        // Har vi behov for at kunne aendre noget ved typen
    }

    protected final void returnTypeTransform(MethodHandle mh, Key<?>... injections) {
        // Must take class value
    }

    protected final void serviceInjectionDisable() {
        // syntes den er enabled by default
    }

    /**
     * Register the result of invoking the method as a service.
     * <p>
     * Methods that Cannot create invokers.
     * 
     * @param isConstant
     *            whether or not the service is constant. Constants are always eagerly computed at initialization time
     * @see #serviceRegister(boolean, Class)
     * @see #serviceRegister(boolean, Key)
     * @throws IllegalStateException
     *             if any invokers have already been registered for the sidecar.
     */
    // Multiple invocations???? Failure, multi services???
    // Multi services... I think you need to register multiple sidecars

    // Move to a special place on ServiceExtension... IDK
    protected final void serviceRegister(boolean isConstant) {
        configuration().serviceRegister(isConstant);
    }

    protected final void serviceRegister(boolean isConstant, Class<?> key) {
        serviceRegister(isConstant, Key.of(key));
    }

    protected final void serviceRegister(boolean isConstant, Key<?> key) {
        configuration().serviceRegister(isConstant, key);
    }

    protected final MethodType type() {
        // Taenker den er opdateret
        return null;
    }
}
