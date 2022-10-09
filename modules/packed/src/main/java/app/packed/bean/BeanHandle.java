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
import app.packed.container.Extension;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.container.ExtensionPoint.UseSite;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationType;
import internal.app.packed.bean.PackedBeanHandle;
import internal.app.packed.bean.PackedBeanHandleInstaller;

/**
 * A bean handle represents a private configuration installed bean.
 * <p>
 * Instances of {@code BeanHandle} are normally never exposed directly to end-users. Instead they are returned wrapped
 * in {@link BeanConfiguration} or a subclass hereof.
 * 
 * 
 */
//Vi har beans uden lifecycle men med instancer

//Fx en validerings bean <--

//LifetimeConfig
////Unmanaged Bean instantiated and initialized by packed  (Init lifetime does not take bean instance)
////Unmanaged Bean instantiated by the user and initialzied by packed (Init lifetime takes bean instance)

////Stateless (with instance) validation bean only supports fx validation annotations?

////
//Unmanaged

//Setup - Teardown

// Hvis

@SuppressWarnings("rawtypes")
public sealed interface BeanHandle<T> permits PackedBeanHandle {

    // We need a extension bean
    default OperationHandle addFunctionalOperation(ExtensionBeanConfiguration<?> operator, Class<?> functionalInterface, OperationType type,
            Object functionInstance) {
        // Function, OpType.of(void.class, HttpRequest.class, HttpResponse.class), someFunc)
        throw new UnsupportedOperationException();
    }

    default OperationHandle addOperation(ExtensionBeanConfiguration<?> operator, MethodHandle methodHandle) {
        return addOperation(operator, Op.ofMethodHandle(methodHandle));
    }

    default OperationHandle addOperation(ExtensionBeanConfiguration<?> operator, Op operation) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return
     * 
     * @see BeanConfiguration#beanClass()
     * @see BeanMirror#beanClass()
     */
    Class<?> beanClass();

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

//    // Kan man tilfoeje en function til alle beans?
//    // funktioner er jo stateless...
//    // Er ikke sikker paa jeg syntes staten skal ligge hos operationen.
//    // Det skal den heller ikke.
//    default <F> OperationCustomizer newFunctionalOperation(Class<F> tt, F function) {
//        throw new UnsupportedOperationException();
//    }
//    
//    // Problemet er her at det virker meget underligt lige pludselig at skulle tilfoeje lanes
//    // Og hvordan HttpRequest, Response er 2 separate lanes... det er lidt sort magi
//    // Skal vi bruge et hint???
//    default OperationCustomizer newFunctionalOperation2(Object functionInstance, Class<?> functionType, Class<?>... typeVariables) {
//        throw new UnsupportedOperationException();
//    }
//    default OperationCustomizer newFunctionalOperation(TypeToken<?> tt, Object functionInstance) {
//        throw new UnsupportedOperationException();
//    }

    boolean isConfigurable();

    default boolean isCurrent() {
        return false;
    }

    /**
     * If the bean is registered with its own lifetime. This method returns a list of the lifetime operations of the bean.
     * <p>
     * The operations in the returned list must be computed exactly once. For example, via
     * {@link OperationHandle#computeMethodHandleInvoker()}. Otherwise a build exception will be thrown. Maybe this goes for
     * all operation customizers.
     * 
     * @return
     */
    default List<OperationHandle> lifetimeOperations() {
        return List.of();
    }

    /**
     * Registers a wiring action to run when the bean becomes fully wired.
     * 
     * @param action
     *            a {@code Runnable} to invoke when the bean is wired
     */
    // ->onWire
    BeanHandle<T> onWireRun(Runnable action);

    /**
     * @param consumer
     */
    // giver den plus decorate mening?
    void peekInstance(Consumer<? super T> consumer);

    // Hvis vi aabner op for specialized bean mirrors
    // maybe just name it mirror?
    void specializeMirror(Supplier<? extends BeanMirror> mirrorFactory);

    /**
     * An installer used to create {@link BeanHandle}. Is created using the various {@code beanInstaller} methods on
     * {@link BeanExtensionPoint}.
     * <p>
     * The main purpose of this interface is to allow various configuration that is needed before the bean is introspected.
     * If the configuration is not needed before introspection the functionality such be present on {@code BeanHandle}
     * instead.
     * 
     * @see BeanExtensionPoint#beanInstaller()
     * @see BeanExtensionPoint#beanInstallerFromClass(Class)
     * @see BeanExtensionPoint#beanInstallerFromOp(Op)
     * @see BeanExtensionPoint#beanBuilderFromInstance(Object)
     */
    // Could have, introspectionDisable()/noIntrospection
    sealed interface Installer<T> permits PackedBeanHandleInstaller {

        /**
         * Marks the bean as owned by the extension representing by specified extension point context
         * 
         * @param context
         *            an extension point context representing the extension that owns the bean
         * @return this builder
         * @throws IllegalStateException
         *             if build has previously been called on the builder
         */
        Installer<T> forExtension(UseSite context);

        /**
         * Adds a new bean to the container and returns a handle for it.
         * 
         * @return the new handle
         * @throws IllegalStateException
         *             if install has previously been called
         */
        BeanHandle<T> install();

//        /**
//         * There will never be any bean instances.
//         * <p>
//         * This method can only be used together with {@link BeanExtensionPoint#beanInstallerFromClass(Class)}.
//         * 
//         * @return this installer
//         * @throws IllegalStateException
//         *             if used without source kind {@code class}
//         */
//        // I think we have an boolean instantiate on beanInstallerFromClass
//        Installer<T> instanceless();

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
        Installer<T> introspectWith(BeanIntrospector introspector);

        Installer<T> kindSingleton();

        default Installer<T> kindLazy() {
            throw new UnsupportedOperationException();
        }

        Installer<T> kindUnmanaged();

        default Installer<T> lifetime(LifetimeFoo lifetime) {
            throw new UnsupportedOperationException();
        }

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
        default Installer<T> namePrefix(String prefix) {
            // Bean'en bliver foerst lavet naar vi tilkobler en configuration

            return this;
        }

        /**
         * Allows for multiple beans of the same type in a single container.
         * <p>
         * By default, a container only allows a single bean of particular type if non-void.
         * 
         * @return this builder
         * 
         * @throws UnsupportedOperationException
         *             if {@code void} bean class
         */
        Installer<T> nonUnique();
        // instanceless-> can never set separate lifetime
        // instance specified -> can never set...

        // lifetimeUnmanaged();
        // lifetimeManaged(boolean seperateOperations);
    }

    // Tjahhh man kan vel maaske have flere end 2???
    // Lad os sige pause(), suspend(), open, close;
    // Umiddelbart har f.x @OnUpgrade jo ikke noget med lifetime at goere.
    // Men tilgaengaeld noget med life cycle at goere

    // Lad os sige vi koere suspend... saa skal vi ogsaa kunne koere resume?

    public class LifetimeFoo {

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
