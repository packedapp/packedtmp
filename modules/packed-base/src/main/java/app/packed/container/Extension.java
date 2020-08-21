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

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Modifier;
import java.util.Optional;

import app.packed.base.TypeLiteral;
import app.packed.component.BeanConfiguration;
import app.packed.component.Bundle;
import app.packed.config.ConfigSite;
import app.packed.inject.Factory;
import packed.internal.config.ConfigSiteSupport;

/**
 * All config Extensions are the main way that function primary way to add features to Packed.
 * 
 * For example, allows you to extend the basic functionality of containers.
 * <p>
 * Extensions form the basis, extensible model
 * <p>
 * constructor visibility is ignored. As long as user has class visibility. They can can use an extension via, for
 * example, {@link BaseBundle#use(Class)} or {@link ContainerConfiguration#use(Class)}.
 * 
 * <p>
 * Any packages where extension implementations, custom hooks or extension wirelet pipelines are located must be open to
 * 'app.packed.base'
 * <p>
 * Every extension implementations must provide either an empty constructor, or a constructor taking a single parameter
 * of type {@link ExtensionConfiguration}. The constructor should have package private accessibility to make sure users
 * do not try an manually instantiate it, but instead use {@link ContainerConfiguration#use(Class)}. It is also
 * recommended that the extension itself is declared final.
 */

// Step1
// final Extension
// package private constructor
// open to app.packed.base
// exported to other users to use

// ErrorHandle, Logging

// ErrorHandling / Notifications ???
/// Taenker det ligger paa Extension'en fordi vi har jo ogsaa en InstantiationContext
// hvor errors jo ogsaa kan ske..
// hasErrors()...
//// Maybe we want to log the actual extension as well.
// so extension.log("fooo") instead
/// Yes, why not use it to log errors...

// Den eneste ting jeg kunne forstille mig at kunne vaere public.
// Var en maade at se paa hvordan en extension blev aktiveret..
// Men er det ikke bare noget logning istedet for metoder...
// "InjectorExtension:" Activate
//// Her er der noget vi gerne vil have viral.
public abstract class Extension {

    static final TypeLiteral<Class<? extends Extension>> TL = new TypeLiteral<Class<? extends Extension>>() {};

    /** A stack walker used by {@link #captureStackFrame(String)}. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    /** The configuration of this extension. Should never be read directly, but accessed via {@link #configuration()}. */
    // I think we should have a value representing configured. In this way people can store the extension
    // or keep it at runtime or whatever they want to do....
    // Samme problem som Bundle vel...
    // Maaske er en Extension et bundle
    private ExtensionConfiguration configuration; // = PEC.CONFIGURED

    /**
     * Invoked by the runtime immediately after the extension's constructor has returned successfully. Since most methods on
     * this class cannot be invoked from within the constructor. This method can be used as an alternative to set up things.
     * <p>
     * The reason for prohibiting configuration from the constructor. Is that users might then link other components that in
     * turn requires access to the actual extension instance. Which is not possible since it is still being instantiated.
     * While this is rare in practice. Too be on the safe side we prohibit it.
     * <p>
     * Should we just use a ThreadLocal??? I mean we can initialize it when Assembling... And I don't think there is
     * anywhere where we can get a hold of the actual extension instance...
     * 
     * But let's say we use another extension from within the constructor. We can only use direct dependencies... But say it
     * installed a component that uses other extensions....????? IDK
     * 
     * most As most methods in this class is unavailable Unlike the constructor, {@link #configuration} can be invoked from
     * this method. Is typically used to add new runtime components.
     */
    protected void add() {}

    /**
     * Captures the configuration site by finding the first stack frame where the declaring class of the frame's method is
     * not located on any subclasses of {@link Extension} or any class that implements
     * <p>
     * Invoking this method typically takes in the order of 1-2 microseconds.
     * <p>
     * If capturing of stack-frame-based config sites has been disable via, for example, fooo. This method returns
     * {@link ConfigSite#UNKNOWN}.
     * 
     * @param operation
     *            the operation
     * @return a stack frame capturing config site, or {@link ConfigSite#UNKNOWN} if stack frame capturing has been disabled
     * @see StackWalker
     */
    // TODO add stuff about we also ignore non-concrete container sources...
    protected final ConfigSite captureStackFrame(String operation) {
        // API-NOTE This method is not available on ExtensionContext to encourage capturing of stack frames to be limited
        // to the extension class in order to simplify the filtering mechanism.

        // TODO!!!! I virkeligheden skal man vel bare fange den sidste brug i et bundle....
        // Kan ogsaa sammenligne med configure navnet...

        if (ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
            return ConfigSite.UNKNOWN;
        }
        Optional<StackFrame> sf = STACK_WALKER.walk(e -> e.filter(f -> !captureStackFrameIgnoreFilter(f)).findFirst());
        return sf.isPresent() ? configuration().containerConfigSite().thenStackFrame(operation, sf.get()) : ConfigSite.UNKNOWN;
    }

    /**
     * @param frame
     *            the frame to filter
     * @return whether or not to filter the frame
     */
    private final boolean captureStackFrameIgnoreFilter(StackFrame frame) {
        Class<?> c = frame.getDeclaringClass();
        // Det virker ikke skide godt, hvis man f.eks. er en metode on a abstract bundle der override configure()...
        // Syntes bare vi filtrer app.packed.base modulet fra...
        // Kan vi ikke checke om imod vores container source.

        // ((PackedExtensionContext) context()).container().source
        // Nah hvis man koere fra config er det jo fint....
        // Fra config() paa en bundle er det fint...
        // Fra alt andet ikke...

        // Dvs ourContainerSource
        return Extension.class.isAssignableFrom(c)
                || ((Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) && Bundle.class.isAssignableFrom(c));
    }

    /**
     * Checks that the extension is configurable, throwing {@link IllegalStateException} if it is not.
     * <p>
     * This method delegate all calls to {@link ExtensionConfiguration#checkConfigurable()}.
     * 
     * @throws IllegalStateException
     *             if the extension is no longer configurable. Or if invoked from the constructor of the extension
     */
    protected final void checkConfigurable() {
        configuration().checkConfigurable();
    }

    /**
     * Returns a configuration for this extension. This is useful, for example, if the extension delegates some
     * responsibility to classes that are not define in the same package as the extension.
     * <p>
     * An instance of this class can also be dependency injected into any subclass. This is useful, for example, if you want
     * to setup some external classes in the constructor that needs access to the configuration object.
     * <p>
     * This method will fail with {@link IllegalStateException} if invoked from the constructor of the extension.
     * 
     * @throws IllegalStateException
     *             if invoked from the constructor of the extension
     * @return a configuration object for the extension
     */
    protected final ExtensionConfiguration configuration() {
        ExtensionConfiguration c = configuration;
        if (c == null) {
            throw new IllegalStateException("This operation cannot be invoked from the constructor of the extension. If you need to perform "
                    + "initialization before returning the extension to the user, override " + Extension.class.getSimpleName() + "#added()");
        }
        return c;
    }

    // TODO skal vi have andre metoder, hvis vi wrapper componenter fra brugeren???
    // Vil mene det, her bliver vi jo ogsaa noedt til at checke man ikke bruger
    // annoteringer fra andre extensions...
    // Saa bliver noedt til at holde
    protected final <T> BeanConfiguration<T> install(Class<T> implementation) {
        return install(Factory.find(implementation));
    }

    protected final <T> BeanConfiguration<T> install(Factory<T> factory) {
        return configuration().install(factory);
    }

    /**
     * @param <T>
     *            the type of the component
     * @param instance
     *            the instance to install
     * @return the configuration of the component
     * @see ContainerConfiguration#installInstance(Object)
     */
    protected final <T> BeanConfiguration<T> installInstance(T instance) {
        return configuration().installInstance(instance);
    }

    protected final void lookup(Lookup l) {
        // Den fungere ligesom Bundle.lookup()
        // Her har vi selve extension'en som
    }

    protected final <E extends Subtension> E use(Class<E> extensionType) {
        return configuration().use(extensionType);
    }

    /**
     * Returns an extension of the specified type.
     * <p>
     * Only extension types that have been explicitly registered using {@link ExtensionSettings#dependencies()} or
     * {@link ExtensionSettings#optionalDependencies()} may be specified as arguments to this method.
     * <p>
     * Invoking this method is similar to calling {@link ContainerConfiguration#use(Class)}. However, this method also keeps
     * track of which extensions uses other extensions. And forming any kind of circle in the dependency graph will fail
     * with a runtime exception.
     * 
     * @param <E>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             If invoked from the constructor of the extension. Or if the underlying container is no longer
     *             configurable and an extension of the specified type has not already been installed
     * @throws UnsupportedOperationException
     *             if the specified extension type has not been specified via {@link ExtensionSettings}
     * @see ExtensionConfiguration#useOld(Class)
     */
    // This will be removed..
    protected final <E extends Extension> E useOld(Class<E> extensionType) {
        return configuration().useOld(extensionType);
    }

    // Naah, taenker vi tillader at lave inline klasser her...
    // Saa vi gider ikke have user..
    // Problemet er den funcking constructor...
    // Er rimlig sikker paa at inline klasser altid er statiske...

    /**
     * There are no annotations that make sense for this class
     * 
     * <p>
     * Instances of this class is automatically created by the runtime as needed. The instances are never cached. A new one
     * is created every it is requested.
     */
    // Should we require that extensions that want to expose services
    // to other extensions must implement them via @Provide
    // Naah, a subtension is not a runtime concept...
    // I really think people need to store there own Class
    public static abstract class Subtension {

        protected void initialize() {}
//
//        // realm() <--zx- public final????
//        // Vi kan sagtens lave nogle ting final paa sub extensions...
//        // Det er jo bare andre extensions der kalde den.
    }

}
//* 
//* @apiNote Original this method was protected. But extension is really the only sidecar that works this way. So to
//*          streamline with other sidecars we only allow it to be dependency injected into subclasses.
// TODO fix with actual annotation type
//throw new IllegalStateException("This operation cannot be invoked from the constructor of the extension. As an alternative "
//      + Extension.class.getSimpleName() + "#onAdd(action) can used to perform initialization");
// Er lidt tilhaenger af initialize()... istedet for annoteringer
// Annoteringer er gode for ikke abstract basis klasser
//
// final void runWithLookup(Lookup lookup, Runnable runnable) {
// // Extensions bliver bare noedt til at vaere aabne for
//
// // Ideen er at vi kan installere component. o.s.v. med det specificeret lookup....
// // D.v.s. vi laver en push, pop af et evt. eksisterende lookup object
// // En install fra en extension skal jo naesten bruge denne..
// // Faktisk, er der lidt sikkerhedshullumhej her.... Hvordan sikre vi os at extensions.
// // Ikke goer noget sjovt her. Hmm, altsaa indvitere man en extension indenfor...
//
// // Men vi vel helst have at de giver adgang via module-info...
// // Eller via Factory.withLookup();
// }