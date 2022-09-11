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
package app.packed.bean;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.TypeToken;
import app.packed.container.Extension;
import app.packed.container.ExtensionPoint.UseSite;
import app.packed.inject.Factory;
import app.packed.operation.OperationCustomizer;
import internal.app.packed.bean.PackedBeanCustomizer;
import internal.app.packed.bean.PackedBeanHandleBuilder;

/**
 * A bean driver must be created via {@link BeanExtensionPoint}.
 */
@SuppressWarnings("rawtypes")
public sealed interface BeanCustomizer<T> permits PackedBeanCustomizer {

    // Kan man tilfoeje en function til alle beans?
    // funktioner er jo stateless...
    // Er ikke sikker paa jeg syntes staten skal ligge hos operationen.
    // Det skal den heller ikke.
    default <F> OperationCustomizer addFunctionalOperation(Class<F> tt, F function) {
        throw new UnsupportedOperationException();
    }

    default OperationCustomizer addFunctionalOperation(TypeToken<?> tt, Object functionInstance) {
        throw new UnsupportedOperationException();
    }

    // Problemet er her at det virker meget underligt lige pludselig at skulle tilfoeje lanes
    // Og hvordan HttpRequest, Response er 2 separate lanes... det er lidt sort magi
    // Skal vi bruge et hint???
    default OperationCustomizer addFunctionalOperation2(Object functionInstance, Class<?> functionType, Class<?>... typeVariables) {
        throw new UnsupportedOperationException();
    }

    default OperationCustomizer addSyntheticOperation(MethodHandle methodHandle /* , boolean firstParamIsBeanInstance */) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return
     * 
     * @see BeanConfiguration#beanClass()
     * @see BeanMirror#beanClass()
     */
    Class<?> beanClass(); // beanSource instead??

    /**
     * @param decorator
     */
    // Usacase?? Typically T is not accessible to the extension
    // Right now this method is only here for InstanceBeanConfiguration#decorate
    // Maybe
    void decorateInstance(Function<? super T, ? extends T> decorator);

    /**
     * @return
     * 
     * @throws UnsupportedOperationException
     *             if called on a bean with void.class beanKind
     */
    Key<?> defaultKey();

    boolean isConfigurable();

    default boolean isCurrent() {
        return false;
    }

    /**
     * If the bean is registered with its own lifetime. This method returns a list of the lifetime operations of the bean.
     * <p>
     * The operations in the returned list must be computed exactly once. For example, via
     * {@link OperationCustomizer#computeMethodHandleInvoker()}. Otherwise a build exception will be thrown. Maybe this goes
     * for all operation customizers.
     * 
     * @return
     */
    default List<OperationCustomizer> lifetimeOperations() {
        return List.of();
    }

    /**
     * Registers a wiring action to run when the bean becomes fully wired.
     * 
     * @param action
     *            a {@code Runnable} to invoke when the bean is wired
     */
    // ->onWire
    BeanCustomizer<T> onWireRun(Runnable action);

    /**
     * @param consumer
     */
    // giver den plus decorate mening?
    void peekInstance(Consumer<? super T> consumer);

    // Hvis vi aabner op for specialized bean mirrors
    // maybe just name it mirror?
    default void specializeMirror(Supplier<? extends BeanMirror> mirrorFactory) {}

    /**
     * A builder for {@link BeanCustomizer}. Is created using the various {@code beanBuilder} methods on
     * {@link BeanExtensionPoint}.
     * 
     * @see BeanExtensionPoint#beanBuilder(BeanKind)
     * @see BeanExtensionPoint#beanBuilderFromClass(BeanKind, Class)
     * @see BeanExtensionPoint#beanBuilderFromFactory(BeanKind, Factory)
     * @see BeanExtensionPoint#beanBuilderFromInstance(BeanKind, Object)
     */
    // Could also have, scan(), scan(BeanScanner), noScan() instead of build()
    sealed interface Builder<T> permits PackedBeanHandleBuilder {

        // Scan (disable, do scan) ???

        /**
         * Registers a bean introspector that will be used instead of the framework calling
         * {@link Extension#newBeanIntrospector}.
         * 
         * @param introspector
         * @return this builder
         * 
         * @throws UnsupportedOperationException
         *             if the bean has a void bean class
         * 
         * @see Extension#newBeanIntrospector
         */
        Builder<T> introspectWith(BeanIntrospector introspector);

        /**
         * Adds a new bean to the container and returns a handle for it.
         * 
         * @return the new handle
         * @throws IllegalStateException
         *             if build has previously been called on the builder
         */
        BeanCustomizer<T> build();

        /**
         * Sets a prefix that is used for naming the bean (This can always be overridden by the user).
         * <p>
         * If there are no other beans with the same name (for same parent container) when creating the bean. Packed will use
         * the specified prefix as the name of the bean. Otherwise, it will append a postfix to specified prefix in such a way
         * that the name of the bean is unique.
         * 
         * @param prefix
         *            the prefix used for naming the bean
         * @return this builder
         * @throws IllegalStateException
         *             if build has previously been called on the builder
         */
        default Builder<T> namePrefix(String prefix) {
            // Bean'en bliver foerst lavet naar vi tilkobler en configuration

            return this;
        }

        /**
         * Marks the bean as owned by the extension representing by specified extension point context
         * 
         * @param context
         *            an extension point context representing the extension that owns the bean
         * @return this builder
         * @throws IllegalStateException
         *             if build has previously been called on the builder
         */
        Builder<T> ownedBy(UseSite context);
    }
}
//INFO (type, kind)

//BeanCustomizer?? Syntes maaske handle er lidt mere runtime
//BeanMaker??

//Operations (add synthetic/functional)

//Sidecars (add)

//Lifecycle (Custom Factory, InvocationConfigurations)

//Services (bind, bindContext)

//callacbks, onBound, onBuild, ...

//Vigtigt at note... Vi scanner klassen naar vi kalder build()
//Saa hvis vi provider services/contexts saa skal det vaere paa
//builderen ellers er de ikke klar til BeanField og friends
//som jo bliver lavet naar man invoker build()

//BeanExtensor?? OperationExtensor, ContainerExtensor, InterceptorExtensor
//default OperationHandle addOperation(OperationDriver driver) {
//  throw new UnsupportedOperationException();
//}
//
//default void addOperation(OperationDriver2 driver) {
//  throw new UnsupportedOperationException();
//}
