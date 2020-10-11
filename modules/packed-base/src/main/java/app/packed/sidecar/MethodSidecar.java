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
import app.packed.container.Extension;
import packed.internal.component.source.SourceModelMethod;
import packed.internal.sidecar.SidecarModel;

/**
 * Packed creates a single instance of a subclass and runs the {@link #configure()} method.
 */
public abstract class MethodSidecar {

    /** The builder of this sidecar. Updated by {@link SidecarModel.Builder}. */
    @Nullable
    private SourceModelMethod.Builder configuration;

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
    protected void configure() {}

    protected final void disableServiceInjection() {
        // syntes den er enabled by default
    }

    /**
     * 
     * @throws IllegalStateException
     *             if called from outside of {@link #configure()}
     */
    protected final void provideInvoker() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns any extension the source is a member of of. Or empty if the source is not part of any extension.
     * 
     * @return any extension the source is a member of of
     */
    public final Optional<Class<? extends Extension>> extensionMember() {
        return configuration().extensionMember();
    }

    protected final <T> void attach(Class<T> key, T instance) {
        attach(Key.of(key), instance);
    }

    public final <T> void attach(Key<T> key, T instance) {}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final void attach(Object instance) {
        requireNonNull(instance, "instance is null");
        attach((Class) instance.getClass(), instance);
    }

    /** Disables the sidecar. No reference to it will be maintained at runtime. */
    public final void disable() {
        configuration.disable();
    }

    /**
     * Returns an annotated element from the method that is being bootstrapped.
     * 
     * @see AnnotatedElement#getAnnotation(Class)
     */
    public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return configuration.getAnnotation(annotationClass);
    }

    /**
     * Returns the method that is being bootstrapped.
     * 
     * @return the method that is being bootstrapped
     */
    public final Method method() {
        return configuration.method();
    }

    /**
     * Register the result of invoking the method as a service.
     * <p>
     * Methods that Cannot create invokers.
     * 
     * @param isConstant
     *            whether or not the service is constant. Constants are always eagerly computed at initialization time
     * @see #registerAsService(boolean, Class)
     * @see #registerAsService(boolean, Key)
     * @throws IllegalStateException
     *             if any invokers have already been registered for the sidecar.
     */
    // Multiple invocations???? Failure, multi services???
    // Multi services... I think you need to register multiple sidecars
    public final void registerAsService(boolean isConstant) {
        configuration().registerAsService(isConstant);
    }

    public final void registerAsService(boolean isConstant, Class<?> key) {
        registerAsService(isConstant, Key.of(key));
    }

    public final void registerAsService(boolean isConstant, Key<?> key) {
        configuration().registerAsService(isConstant, key);
    }

    public final MethodType type() {
        // Taenker den er opdateret
        return null;
    }

    // MethodHandle must take
    public final void returnTypeTransform(MethodHandle mh, Class<?>... injections) {
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

    public final void returnTypeTransform(MethodHandle mh, Key<?>... injections) {
        // Must take class value
    }

    public final void forEachUnbound(Consumer<? super VariableBinder> action) {

    }
}
