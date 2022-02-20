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
package app.packed.hooks;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.Variable;
import app.packed.bean.InvokerConfiguration;
import app.packed.bean.mirror.BeanOperationMirror;
import app.packed.extension.ExtensionConfiguration;
import app.packed.hooks.accessors.MethodAccessor;
import app.packed.hooks3.MetaAnnotationReader;
import app.packed.hooks3.VarInjector;
import app.packed.inject.Factory;
import packed.internal.bean.hooks.usesite.UseSiteFieldHookModel;
import packed.internal.bean.hooks.variable.FieldVariable;

// Ideen er vel man laver en operation?

// Mulige outcomes
//// Der bliver lavet en VarHandle
//// Der bliver lavet en MethodHandle evt 2 (.
//// Der bliver lavet en Invoker

// Disse foere alle til at der bliver lavet en operation
//// Ingen af delene
public abstract class BeanFieldProcessor {

    /** The builder used by this bootstrap. Updated by {@link UseSiteFieldHookModel}. */
    private UseSiteFieldHookModel.@Nullable Builder builder;

    // Taenker vi har lov til at smide reflection exception???
    protected void bootstrap() {}

    /**
     * Returns this sidecar's builder object.
     * 
     * @return this sidecar's builder object
     */
    private UseSiteFieldHookModel.Builder builder() {
        UseSiteFieldHookModel.Builder b = builder;
        if (b == null) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        }
        return b;
    }

    /** { @return the underlying field.} */
    public final Field field() {
        return builder().field();
    }

    public final InvokerConfiguration invoker(VarHandle.AccessMode accessMod) {
        throw new UnsupportedOperationException();
    }

    public final InvokerConfiguration invokerGetter() {
        throw new UnsupportedOperationException();
    }

    public final InvokerConfiguration invokerSetter() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param <T>
     *            the type of extension class bootstrap to return
     * @param type
     *            the type of extension class bootstrap to return
     * @return a extension class bootstrap of the specified type
     * @throws IllegalStateException
     *             if this method has previously been called on this instance
     * 
     */
    //// Tror vi replacer den med noget BeanConfigurationContext (som har .get/.set)
    // og bliver smidt ud efter build
    // extension selv fungere paa container niveau.
    // kunne have et Map<BeanInfo> i extensionen???
    protected final <T extends BeanClass> T manageBy(Class<T> type) {
        return builder().manageBy(type);
    }

    /**
     * @return a method handle getter
     */
    public final MethodHandle methodHandleGetter() {
        throw new UnsupportedOperationException();
    }

    public final MethodHandle methodHandleSetter() {
        throw new UnsupportedOperationException();
    }

    /**
     * Must have both get and set
     * 
     * @return the variable
     * @see Lookup#unreflectVarHandle(Field)
     * @see BeanFieldHook#allowGet()
     * @see BeanFieldHook#allowSet()
     * @throws UnsupportedOperationException
     *             if the extension field has not both get and set access
     */
    public final VarHandle varHandle() {
        return builder().varHandle();
    }

    /** {@return the underlying represented as a {@code Variable}.} */
    public final Variable variable() {
        return new FieldVariable(builder().field());
    }
}

class BeanFieldSandbox {
    
    public final <T extends BeanOperationMirror> void addOperationMirror(Class<T> mirrorType, Supplier<T> supplier) {

    }

    public BeanFieldSandbox newOperation(ExtensionConfiguration ec) {
        // fx hvis man vil provide den som en service og et eller andet samtidig
        // BeanSupport.
        /// Naah ikke en metode her...
        // Vi skal bruge extensionen der goer det...
        
        // BeanField.
        throw new UnsupportedOperationException();
    }

    public final VarInjector injector() {
        // Er ikke sikker paa vi supportere denne
        throw new UnsupportedOperationException();
    }

    public final MetaAnnotationReader metaAnnotations() {
        throw new UnsupportedOperationException();
    }

    public final void requireContext(Class<?> contextType) {
        // fx SchedulingContext ect... Don't know if we need it
    }
    
    public final <T> T variableCompute(Function<Variable, T> computer) {
        // Kan kun have en...
        throw new UnsupportedOperationException();
    }
}

class ZeanFieldProcessorOldCrap {

    Class<?> invoker;

    protected final <T> void attach(Class<T> key, T instance) {
        attach(Key.of(key), instance);
    }

    protected final <T> void attach(Key<T> key, T instance) {}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final void attach(Object instance) {
        requireNonNull(instance, "instance is null");
        attach((Class) instance.getClass(), instance);
    }

    /**
     * Check not final??
     */
    protected final void checkNonFinal() {}

    /** Disables any further processing of the field. */
    // IDK kan vi ikke bare sige
    public final void disable() {
        // builder().disable();
    }

    // Tror slet ikke vi supportere injection???
    // Brug et ServiceHook istedet for
    protected final void disableServiceInjection() {
        // syntes den er enabled by default
    }

    // A variable parser?
    protected final Optional<Key<?>> key() {
        // Maaske extract via Key.of(Field)
        return null;
    }

    /**
     * 
     * @throws IllegalStateException
     *             if called from outside of {@link #processor()}
     */
    protected final void provideInvoker() {
        if (invoker != null) {
            throw new IllegalStateException("Cannot provide more than 1 " + MethodAccessor.class.getSimpleName());
        }
        invoker = Object.class;
    }

    /**
     * Sets the field to the specified argument.
     * 
     * @param argument
     *            the argument to set
     * @throws UnsupportedOperationException
     *             if {@link BeanFieldHook#allowSet()} is false
     * @throws ClassCastException
     *             if the specified argument is not assignable to the field
     */
    // Get if static field...
    // Hvor tit bruger vi lige den????
    // Get a VarHandle and set it
    protected final void set(Object argument) {
//        builder().set(argument);
    }

    static Factory<?> findFactory(String name) {
        throw new UnsupportedOperationException();
    }
}