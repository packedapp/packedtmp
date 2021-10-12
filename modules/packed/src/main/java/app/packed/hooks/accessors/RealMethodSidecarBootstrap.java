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
package app.packed.hooks.accessors;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.Optional;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.hooks.BeanMethod;
import app.packed.inject.Factory;
import app.packed.inject.sandbox.FactoryType;
import app.packed.inject.variable.Variable;

/**
 * Packed creates a single instance of a subclass per method and runs the {@link #bootstrap()} method.
 */
// Vi har ikke laengere 

// implements AnnotatedElement

// Skal metoderne vaere protected????
// Ikke hvis man skal kunne specificere den til extensions...
public abstract class RealMethodSidecarBootstrap extends BeanMethod {
    final boolean needsInstance() {
        return !Modifier.isStatic(method().getModifiers());
    }

   
    // addFunction(Factory + @Schedule(123)
    // runWith?
    public void replaceRuntime(Object runtime) {
        // runtime must be fixed before we start binding...
    }

    // *******Customize receiver ******* /
    // Enten via en ny runtime klasse
    // Eller vi custom factories...

    // 1 per static method or 1 per instance method
    public void replaceRuntimeProtoype(Class<?> runtime) {

    }
    // We need meta class
    /// Only functions??
    /// Only interesting functions???
    // ...

    public final Optional<Class<?>> runtime() {
        // Class<?> --> MetaClass
        // disable -> empty
        // no interesting methods -> empty
        // default = sidecar
        // replaceRuntime(Class...);
        // For eksempel hvis man returnere noget...
        throw new UnsupportedOperationException();
    }
    // Tror de her doer...
    // Hvis vi kan lave en ny instans...
    // provideLocal...
    public final <T> void provide(Class<T> key, T instance) {
        provide(Key.of(key), instance);
    }

    public final <T> void provide(Key<T> key, T instance) {}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final void provide(Object instance) {
        requireNonNull(instance, "instance is null");
        provide((Class) instance.getClass(), instance);
    }

    // Ellers har vi en speciel Invoker factory

    // Parameters of method handle must match Invoker

    // Maaske man skal have sat en invoker inden... Ja det skal man

    // Hvordan mixer vi med services???
//    public final void bindParameterToArgument(int parameterIndex, int argumentIndex) {}
//
//    public final void bindParameterToArguement(int parameterIndex, MethodHandle invokerCompatible) {}

    // protected final void bindParameterMh(int index, MethodHandle mh) {}

//    public final void forEachUnbound(Consumer<? super VariableBinder> action) {
//
//    }

    /**
     * 
     * @throws IllegalStateException
     *             if called from outside of {@link #bootstrap()}
     */
    public final void provideInvoker() {
        throw new UnsupportedOperationException();
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

    // Hoere til invokeren... Eller IDK
    public final void serviceInjectionDisable() {
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
    // Vi vil ogsaa gerne have muligheden for at tilfoeje attributer...

    public final void serviceRegister(boolean isConstant) {
        builder().serviceRegister(isConstant);
    }

    public final void serviceRegister(boolean isConstant, Class<?> key) {
        serviceRegister(isConstant, Key.of(key));
    }

    public final void serviceRegister(boolean isConstant, Key<?> key) {
        builder().serviceRegister(isConstant, key);
    }

    public final MethodType type() {
        // Taenker den er opdateret
        return null;
    }
}
class Oldies {
    // this type of factory we will try and resolve...
    // bind parameters must be done before hand
    public final FactoryType targetType() {
        // this will change when binding parameter
        throw new UnsupportedOperationException();
    }
    public final void parameterBindArgument(int parameterIndex, int argumentIndex) {}

    /**
     * <p>
     * If the parameter type is a primitive, the argument object must be a wrapper, and will be unboxed to produce the
     * primitive value.
     * 
     * @param parameterIndex
     *            the index of the parameter
     * @param constant
     *            the constant to bind
     * @throws IndexOutOfBoundsException
     *             if the specified index is not valid
     * @throws ClassCastException
     *             if an argument does not match the corresponding parameter type.
     * @see MethodHandles#insertArguments(MethodHandle, int, Object...)
     */
    public final void parameterBindConstant(int parameterIndex, @Nullable Object constant) {}

    /**
     * @param parameterIndex
     *            the index of the parameter to bind
     * @param mh
     *            a method handle whose type must match. Do we insert automatic casts???
     */
    public final void parameterBindFromArguments(int parameterIndex, MethodHandle mh) {}

    public final void parameterBindFromArguments(int parameterIndex, Factory<?> factory) {}
    
    // insert instead??? bind = constants, insert = factory
    public final void parameterInsert(int index, Factory<?> factory, Object... resolveFrom) {
        // ResolveFrom must be int -> Arguments, or Class|Key -> @Provide
        // ideen er lidt at vi indsaetter parameter fra factory i factory type
    }

    // Must be done as the 
    // Must be direct castable compatible...
    public final void parameterTransform(int parameterIndex, Class<?> newType) {}

    // Must be compatible...
    public final void parameterTransform(int parameterIndex, Class<?> newType, MethodHandle transformer) {}

    public final void parameterTransform(int parameterIndex, Variable newType) {}
    public final void parameterTransform(int parameterIndex, Variable newType, MethodHandle transform) {}

}