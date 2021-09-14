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
package packed.internal.service.sandbox;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationImage;
import app.packed.application.programs.Program;
import app.packed.container.Bundle;
import app.packed.container.ComposerAction;
import app.packed.container.Wirelet;
import app.packed.service.ServiceLocator;
import packed.internal.application.ApplicationLaunchContext;
import packed.internal.util.LookupUtil;

/**
 * An injector is an immutable holder of services that can be dependency injected or looked up by their type at runtime.
 * An injector is typically created by populating an injector builder with the various services that needs to be
 * available.
 *
 * These
 *
 * _ xxxxx Injector controls the services that are available from every container at runtime and is typically used for
 * and for injection.
 *
 * Typically a number of injectors exist. The container injector is.
 *
 *
 *
 * Container injector can be.
 *
 * For example, the injector for a component will also include a Component service. Because an instance of the component
 * interface can always be injected into any method.
 *
 *
 * <p>
 * An Injector instance is usually acquired in one of the following three ways:
 * <h3>Directly from a Container instance</h3> By calling container.getService(ServiceManager.class)}:
 *
 * <pre>
 * Container c = ...;
 * Injector injector = c.getService(Injector.class);
 * System.out.println(&quot;Available services: &quot; + Injector.services());
 * </pre>
 *
 * <h3>Annotated method, such as OnStart or OnStop</h3> When using annotations such as OnStart or OnStop. An injected
 * component manager can be used to determine which parameters are available for injection into the annotated method.
 *
 * <pre>
 * &#064;RunOnStart()
 * public void onStart(ServiceManager ServiceManager) {
 *     System.out.println(&quot;The following services can be injected: &quot; + ServiceManager.getAvailableServices());
 * }
 * </pre>
 *
 * <h3>Injecting it into a Constructor</h3> Or, by declaring it as a parameter in the constructor of a service or agent
 * registered using container builder or container builder
 *
 * <pre>
 * public class MyService {
 *     public MyService(ServiceManager ServiceManager) {
 *         System.out.println(&quot;The following services can be injected: &quot; + ServiceManager.getAvailableServices());
 *     }
 * }
 * </pre>
 *
 * <p>
 * The map returned by this method may vary doing the life cycle of a container. For example, if this method is invoked
 * in the constructor of a service registered with container builder. An instance of container builder is present in the
 * map returned. However, after the container has been initialized, the container will no longer keep a reference to the
 * configuration instance. So instances of Injector will never be available from any service manager after the container
 * has fully started.
 * <p>
 * Injectors are always immutable, however, extensions of this interface might provide mutable operations for methods
 * unrelated to injection.
 */
// Description... hmm its just super helpful...
// Injector does not have a name. In many cases there are a container behind an Injector.
// But if, for example, a component has its own injector. That injector does not have a container behind it.

// Do we have an internal injector and an external injector?????
// Or maybe an Injector and an InternalInjector (which if exportAll is the same???)

// Altsaa den hoerer vel ikke til her...
// Vi kan jo injecte andre ting en services

// Injector taenker jeg er component versionen...
// ServiceRegistry er service versionen...

// Aahhhh vi mangler nu end 4. version... ind imellem Injector og ServiceRegistry...

// Noget der kan injecte ting... Men ikke har en system component... 

public interface Injector extends ServiceLocator {

//    /**
//     * Returns the configuration site of this injector.
//     * 
//     * @return the configuration site of this injector
//     */
//    ConfigSite configSite();

    // /**
    // * Injects services into the fields and methods of the specified instance.
    // * <p>
    // * This method is typically only needed if you need to construct objects yourself.
    // *
    // * @param <T>
    // * the type of object to inject into
    // * @param instance
    // * the instance to inject members (fields and methods) into
    // * @param lookup
    // * A lookup object used to access the various members on the specified instance
    // * @return the specified instance
    // * @throws InjectionException
    // * if any of the injectable members of the specified instance could not be injected
    // */

    // <T> T injectMembers(MethodHandles.Lookup caller, T instance);
    // <T> T injectMembers(T instance, MethodHandles.Lookup lookup);

    static ApplicationImage<Injector> imageOf(Bundle<?> assembly, Wirelet... wirelets) {
        return driver().imageOf(assembly, wirelets);
    }

    // Is this useful outside of hosts???????
    static ApplicationDriver<Injector> driver() {
        return InjectorApplicationHelper.DRIVER;
    }

    /**
     * Creates a new injector using a configurator object.
     *
     * @param configurator
     *            a consumer used for configuring the injector
     * @param wirelets
     *            wirelets
     * @return the new injector
     */
    static Injector configure(ComposerAction<? super InjectorComposer> configurator, Wirelet... wirelets) {
        return InjectorComposer.configure(configurator, wirelets);
    }

    /**
     * Creates a new injector from the specified assembly.
     *
     * @param assembly
     *            the assembly to create the injector from
     * @param wirelets
     *            optional wirelets
     * @return the new injector
     * @throws RuntimeException
     *             if the injector could not be created for some reason. For example, if the source defines any components
     *             that requires a lifecycle
     */
    // Of er maaske fin. Saa understreger vi ligesom
    static Injector of(Bundle<?> assembly, Wirelet... wirelets) {
        return driver().launch(assembly, wirelets);
    }
}

/** An artifact driver for creating {@link Program} instances. */
final class InjectorApplicationHelper {

    static final MethodHandle CONV = LookupUtil.lookupStatic(MethodHandles.lookup(), "convert", Injector.class, ApplicationLaunchContext.class);

    static final ApplicationDriver<Injector> DRIVER = ApplicationDriver.builder().build(MethodHandles.lookup(),
            Injector.class, CONV);

    static Injector convert(ApplicationLaunchContext container) {
        return (Injector) container.services();
    }
}
