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
package app.packed.container;

import app.packed.artifact.ArtifactImage;
import app.packed.artifact.app.App;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.lifecycle.LifecycleExtension;
import app.packed.lifecycle.OnStart;
import app.packed.service.ComponentServiceConfiguration;
import app.packed.service.Factory;
import app.packed.service.Injector;
import app.packed.service.InjectorConfigurator;
import app.packed.service.Provide;
import app.packed.service.ServiceConfiguration;
import app.packed.service.ServiceExtension;
import app.packed.util.Key;
import app.packed.util.Qualifier;

/**
 * A BaseBundle contains shortcut access to common functionality defined by the various extension available in this
 * module.
 * <p>
 * For example, instead of doing use(ServiceExtension.class).provide(Foo.class) you can just use
 * service().provide(Foo.class) or even just provide(Foo.class).
 * <p>
 * 
 * With common functionality provide by app.packed.base
 * 
 * <p>
 * 
 * Bundles provide a simply way to package components and build modular application. This is useful, for example, for:
 * <ul>
 * <li>Sharing functionality across multiple injectors and/or containers.</li>
 * <li>Hiding implementation details from users.</li>
 * <li>Organizing a complex project into distinct sections, such that each section addresses a separate concern.</li>
 * </ul>
 * <p>
 * There are currently two types of bundles available:
 * <ul>
 * <li><b>{@link BaseBundle}</b> which bundles information about services, and creates {@link Injector} instances using
 * .</li>
 * <li><b>{@link BaseBundle}</b> which bundles information about both services and components, and creates container
 * instances using .</li>
 * </ul>
 * 
 * 
 * @apiNote We never return, for example, Bundle or BaseBundle to allow for method chaining. As this would make
 *          extending the class difficult unless we defined the methods as non-final.
 */
public abstract class BaseBundle extends Bundle {

    /**
     * Returns a component extension instance, installing it if it has not already been installed.
     * 
     * @return a component extension instance
     */
    protected final ComponentExtension component() {
        return use(ComponentExtension.class);
    }

    /**
     * Exposes an internal service outside of this bundle, equivalent to calling {@code expose(Key.of(key))}. A typical use
     * case if having a single
     * 
     * When you expose an internal service, the descriptions and tags it may have are copied to the exposed services.
     * Overridden them will not effect the internal service from which the exposed service was created.
     * 
     * <p>
     * Once an internal service has been exposed, the internal service is made immutable. For example,
     * {@code setDescription()} will fail in the following example with a runtime exception: <pre>{@code 
     * ServiceConfiguration<?> sc = bind(ServiceImpl.class);
     * expose(ServiceImpl.class).as(Service.class);
     * sc.setDescription("foo");}
     * </pre>
     * <p>
     * A single internal service can be exposed under multiple keys: <pre>{@code 
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class).as(Service1.class).setDescription("Service 1");
     * expose(ServiceImpl.class).as(Service2.class).setDescription("Service 2");}
     * </pre>
     * 
     * @param <T>
     *            the type of the exposed service
     * 
     * @param key
     *            the key of the internal service to expose
     * @return a service configuration for the exposed service
     * @see #export(Key)
     */
    protected final <T> ServiceConfiguration<T> export(Class<T> key) {
        return service().export(key);
    }

    protected final <T> ServiceConfiguration<T> export(ComponentServiceConfiguration<T> configuration) {
        return service().export(configuration);
    }

    /**
     * Exposes an internal service outside of this bundle.
     * 
     * 
     * <pre> {@code  
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class);}
     * </pre>
     * 
     * You can also choose to expose a service under a different key then what it is known as internally in the
     * <pre> {@code  
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class).as(Service.class);}
     * </pre>
     * 
     * @param <T>
     *            the type of the exposed service
     * @param key
     *            the key of the internal service to expose
     * @return a service configuration for the exposed service
     * @see #export(Key)
     */
    protected final <T> ServiceConfiguration<T> export(Key<T> key) {
        return service().export(key);
    }

    protected final void exportAll() {
        service().exportAll();
    }

    protected final <T> ComponentConfiguration<T> install(Class<T> implementation) {
        return component().install(implementation);
    }

    protected final <T> ComponentConfiguration<T> install(Factory<T> factory) {
        return component().install(factory);
    }

    protected final <T> ComponentConfiguration<T> installHelper(Class<T> implementation) {
        return component().installStatic(implementation);
    }

    /**
     * Install the specified component instance.
     * <p>
     * If this install operation is the first install operation of the container. The component will be installed as the
     * root component of the container. All subsequent install operations on this bundle will have have component as its
     * parent. If you wish to have a specific component as a parent, the various install methods on
     * {@link ComponentConfiguration} can be used to specify a specific parent.
     *
     * @param instance
     *            the component instance to install
     * @return this configuration
     */
    protected final <T> ComponentConfiguration<T> installInstance(T instance) {
        return component().installInstance(instance);
    }

    /**
     * Returns a lifecycle extension instance, installing it if it has not already been installed.
     * 
     * @return a lifecycle extension instance
     */
    protected final LifecycleExtension lifecycle() {
        return use(LifecycleExtension.class);
    }

    /**
     * Binds the specified implementation as a new service. The runtime will use {@link Factory#findInjectable(Class)} to
     * find a valid constructor or method to instantiate the service instance once the injector is created.
     * <p>
     * The default key for the service will be the specified {@code implementation}. If the {@code Class} is annotated with
     * a {@link Qualifier qualifier annotation}, the default key will have the qualifier annotation added.
     *
     * @param <T>
     *            the type of service to bind
     * @param implementation
     *            the implementation to bind
     * @return a service configuration for the service
     * @see InjectorConfigurator#provide(Class)
     */
    protected final <T> ComponentServiceConfiguration<T> provide(Class<T> implementation) {
        return service().provide(implementation);
    }

    /**
     *
     * <p>
     * Factory raw type will be used for scanning for annotations such as {@link OnStart} and {@link Provide}.
     *
     * @param <T>
     *            the type of component to install
     * @param factory
     *            the factory used for creating the component instance
     * @return the configuration of the component that was installed
     */
    protected final <T> ComponentServiceConfiguration<T> provide(Factory<T> factory) {
        return service().provide(factory);
    }

    protected final void provideAll(Injector injector, Wirelet... wirelets) {
        service().provideAll(injector, wirelets);
    }

    protected final <T> ComponentServiceConfiguration<T> provideInstance(T instance) {
        return service().provideInstance(instance);
    }

    protected final void require(Class<?> key) {
        service().require(Key.of(key));
    }

    protected final void require(Key<?> key) {
        service().require(key);
    }

    protected final void requireOptionally(Class<?> key) {
        service().requireOptionally(Key.of(key));
    }

    protected final void requireOptionally(Key<?> key) {
        service().requireOptionally(key);
    }

    /**
     * Returns a service extension instance, installing it if it has not already been installed.
     * 
     * @return a service extension instance
     */
    protected final ServiceExtension service() {
        return use(ServiceExtension.class);
    }

    /**
     * Prints the contract of the specified bundle.
     * 
     * @param bundle
     *            the bundle to print the contract for
     */
    protected static void printContract(Bundle bundle) {
        // BaseBundleContract.of(bundle).print();
    }

    protected static void printDescriptor(Bundle bundle) {
        BundleDescriptor.of(bundle).print();
    }
}

class MyBundle2 extends BaseBundle {

    private static final ArtifactImage IMAGE = ArtifactImage.of(new MyBundle2());

    @Override
    protected void configure() {
        lifecycle().main(() -> System.out.println("HelloWorld"));
    }

    public static void main(String[] args) {
        App.run(IMAGE);
    }
}
/// **
// * @param builder
// * the injector configuration to delagate to
// * @param freeze
// * @apiNote we take an AbstractBundleConfigurator instead of a BundleConfigurator to make sure we never parse an
// * external configurator by accident. And we some let the bundle implementation invoke
// * {@link #lookup(java.lang.invoke.MethodHandles.Lookup)} on a random interface. Thereby letting the Lookup
// * object escape.
// */
// final void configure(InjectorBuilder builder, boolean freeze) {
//
// // Maybe we can do some access checkes on the Configurator. To allow for testing....
// //
// // if (this.injectorBuilder != null) {
// // throw new IllegalStateException();
// // } else if (isFrozen && freeze) {
// // // vi skal have love til f.eks. at koere en gang descriptor af, saa det er kun hvis vi skal freeze den ogsaa doer.
// // throw new IllegalStateException("Cannot configure this bundle, after it has been been frozen");
// // }
// // this.injectorBuilder = requireNonNull(builder);
// // try {
// // configure();
// // } finally {
// // this.injectorBuilder = null;
// // if (freeze) {
// // isFrozen = true;
// // }
// // }
// throw new UnsupportedOperationException();
// }
//// /**
//// * Returns the bundle support object which
//// *
//// * @return the bundle support object
//// */
//// protected final ContainerBuildContext context() {
//// // Vi laver en bundle nyt per configuration.....
//// ContainerBuildContext s = context;
//// if (s == null) {
//// throw new IllegalStateException("This method can only be called from within Bundle.configure(). Maybe you tried to
//// call Bundle.configure directly");
//// }
//// return s;
//// }
/**
 * A injector bundle provides a simple way to package services into a resuable container nice little thingy.
 * 
 * Bundles provide a simply way to package components and service. For example, so they can be used easily across
 * multiple containers. Or simply for organizing a complex project into distinct sections, such that each section
 * addresses a separate concern.
 * <p>
 * Bundle are useually
 *
 * <pre>
 * class WebServerBundle extends Bundle {
 *
 *     private port = 8080; //default port
 *
 *     protected void configure() {
 *        install(new WebServer(port));
 *     }
 *
 *     public WebServerBundle setPort(int port) {
 *         checkNotFrozen();
 *         this.port = port;
 *         return this;
 *     }
 * }
 * </pre>
 *
 * The bundle is used like this:
 *
 * <pre>
 * ContainerBuilder b = new ContainerBuilder();
 * b.use(WebServiceBundle.class).setPort(8080);
 *
 * Container c = cc.newContainer();
 * </pre>
 * <p>
 * Bundles must have a single public no argument constructor.
 * <p>
 * Bundles are strictly a configuration and initialization time concept. Bundles are not available
 */
//
/// **
// * Opens the bundle for modification later on
// */
// protected final void open() {
// // Nope....
// }
// ID256 BundleHash????? API wise. SpecHash..

// protected void lookup(Lookup lookup, LookupAccessController accessController) {}
// protected final void checkNotNativeRuntime() {
// if (GraalSupport.inImageRuntimeCode()) {
// StackFrame f = StackWalker.getInstance().walk(e -> e.skip(1).findFirst().get());
// throw new IllegalStateException("Cannot call " + f.getMethodName() + "() when running as a native-image");
// }
// }