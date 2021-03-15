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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.base.Nullable;
import app.packed.component.BaseComponentConfiguration;
import app.packed.component.BuildInfo;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Image;
import app.packed.component.Realm;
import app.packed.component.Wirelet;
import app.packed.inject.Factory;
import packed.internal.container.ExtensionModel;
import packed.internal.container.ExtensionSetup;

/**
 * Extensions are the primary way to extend Packed with new features. In fact most features provided by Packed itself is
 * using the same extension mechanism available to any user.
 * 
 * For example, allows you to extend the basic functionality of containers.
 * <p>
 * Extensions form the basis, extensible model
 * <p>
 * constructor visibility is ignored. As long as user has class visibility. They can can use an extension via, for
 * example, {@link BaseAssembly#use(Class)} or {@link ContainerConfiguration#use(Class)}.
 * 
 * <p>
 * Step1 // package private constructor // open to app.packed.base // exported to other users to use
 * 
 * <p>
 * Any packages where extension implementations, custom hooks or extension wirelet pipelines are located must be open to
 * 'app.packed.base'
 * <p>
 * Every extension implementations must provide either an empty constructor, or a constructor taking a single parameter
 * of type {@link ExtensionConfiguration}. The constructor should have package private accessibility to make sure users
 * do not try an manually instantiate it, but instead use {@link ContainerConfiguration#use(Class)}. The extension
 * subclass should not be declared final as Packed supports some debug configuration that relies on extending
 * extensions.
 */
public abstract class Extension extends Realm {

    /** A stack walker used by various methods. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    /**
     * The configuration of this extension. Should never be read directly, but accessed via {@link #configuration()}.
     * 
     * @apiNote This field is not nulled out after the extensions has been configured to allow for calling methods such as
     *          {@link #checkConfigurable()} after configuration is done.
     */
    @Nullable
    private ExtensionConfiguration configuration;

    /** Create a new extension. Subclasses should have a package-protected constructor. */
    protected Extension() {}

    /**
     * Returns the assembly context the extension is a part of.
     * 
     * @return the assembly context
     * @throws IllegalStateException
     *             if invoked from the constructor of the extension
     */
    // Det jeg ikke kan lide ved den er fx information om image... som jo kan vaereforskellige
    // for extension'en selv...
    protected final BuildInfo assembly() {
        return configuration().build();
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

    protected final void checkIsLeafBundle() {
        configuration().checkIsLeafBundle();
    }

    /**
     * Returns the underlying configuration object that this extension wraps. The configuration object returned by this
     * method, can be used if the extension delegates some responsibility to classes that are not define in the same package
     * as the extension itself.
     * <p>
     * An instance of {@code ExtensionConfiguration} can also be dependency injected into the constructor of an extension
     * subclass. This is useful, for example, if you want to setup some external classes in the constructor that needs
     * access to the configuration object.
     * <p>
     * This method will fail with {@link IllegalStateException} if invoked from the constructor of the extension.
     * 
     * @throws IllegalStateException
     *             if invoked from the constructor of the extension, as an alternative dependency inject the configuration
     *             object into the constructor
     * @return a configuration object for this extension
     */
    protected final ExtensionConfiguration configuration() {
        ExtensionConfiguration c = configuration;
        if (c == null) {
            throw new IllegalStateException("This operation cannot be invoked from the constructor of the extension. If you need to perform "
                    + "initialization before returning the extension to the user, override " + Extension.class.getSimpleName() + "#extensionAdded()");
        }
        return c;
    }

    /**
     * A method that is invoked (by the runtime) immediately after the extension's constructor has been successfully
     * invoked. Since most methods on this class cannot be invoked from within the extension's constructor. This method can
     * be used to perform post instantiation, before the extension instance is returned to the user.
     * <p>
     * The reason for prohibiting configuration from the constructor. Is to avoid situations.. that users might then link
     * other components that in turn requires access to the actual extension instance. Which is not possible since it is
     * still being instantiated. While this is rare in practice. Too be on the safe side we prohibit it.
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
    protected void extensionAdded() {}

    // Hvad hvis den selv tilfoejer komponenter med en child container???
    // Problemet er hvis den bruger extensions som den ikke har defineret
    // Det tror jeg maaske bare ikke den kan
    protected void extensionBeforeDescendents() {}

    // Invoked before the first child container
    // Invoke always, even if no child containers
    // If you have configuration that
    // extensionPreambleDone

    /**
     * Invoked by the runtime when the configuration of the bundle is completed.
     * <p>
     */
    protected void extensionConfigured() {}

    protected final BaseComponentConfiguration install(Class<?> implementation) {
        return configuration().install(implementation);
    }

    protected final BaseComponentConfiguration install(Factory<?> factory) {
        return configuration().install(factory);
    }

    /**
     * @param instance
     *            the instance to install
     * @return the configuration of the component
     * @see ContainerConfiguration#installInstance(Object)
     */
    protected final BaseComponentConfiguration installInstance(Object instance) {
        return configuration().installInstance(instance);
    }

    protected final void isLeafBundle() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns whether or not the container that this extension belongs is being built into an {@link Image}.
     * 
     * @return true if the extension
     */
    // isInImage(), isImageParticipant
    protected final boolean isPartOfImage() {
        return configuration().isPartOfImage();
    }

    protected final void lookup(MethodHandles.Lookup lookup) {
        ((ExtensionSetup) configuration()).lookup(lookup);
    }

    /**
     * Used to lookup other extensions.
     * <p>
     * Only extension types that have been explicitly registered using {@link #$addDependency(Class)}may be specified as
     * arguments to this method.
     * <p>
     * Invoking this method is similar to calling {@link ContainerConfiguration#use(Class)}. However, this method also keeps
     * track of which extensions uses other extensions. And forming any kind of circle in the dependency graph will fail
     * with a runtime exception.
     * 
     * @param <E>
     *            the type of extension to return
     * @param extensionClass
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             If invoked from the constructor of the extension. Or if the underlying container is no longer
     *             configurable and an extension of the specified type has not already been installed
     * @throws UnsupportedOperationException
     *             if the specified extension type has not been specified when bootstrapping the extension
     * @see #$addDependency(Class)
     */
    protected final <E extends Subtension> E use(Class<E> extensionClass) {
        return configuration().use(extensionClass);
    }

    public <C extends ComponentConfiguration> C userWire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return configuration().userWire(driver, wirelets);
    }

    /**
     * Add the specified extension as a dependency.
     * <p>
     * to use it
     * <p>
     * Installing components that make use of fx Provide you need ServiceExtension
     * 
     * 
     * @param dependency
     *            the dependency that this dependency depends on
     * @see #$addDependencyLazyInit(Class, Class, Consumer)
     */
    protected static void $addDependency(Class<? extends Extension> dependency) {
        requireNonNull(dependency, "dependency is null");
        ExtensionModel.bootstrapAddDependency(STACK_WALKER.getCallerClass(), dependency);
    }

    // Tilfoejer en dependency hvor callbacket. Bliver kaldt. Hvis dependency bliver tilfoejet
    // Hvis den dependency extension allerede existere naar man tilfoejere extension. Bliver den kaldt med det samme

    // Uhh hvad hvis der er andre dependencies der aktivere den last minute i onBuild()???
    // Vi har jo ligesom lukket for this extension... Og saa bliver den allivel aktiveret!!
    // F.eks. hvis nogle aktivere onBuild().. Igen det er jo en hel chain vi saetter i gang
    /// Maa maaske kigge lidt paa graal og have nogle loekker who keeps retrying
    protected static <T extends Extension> void $addDependencyLazyInit(Class<? extends Extension> dependency, Class<T> thisExtension,
            Consumer<? super T> action) {
        // Bliver kaldt hvis den specificeret
        // Registeres ogsaa som dependeenc
        // $ = Static Init (s + i = $)
    }

    public static <T extends Extension> Optional<T> privateLookupExtension(MethodHandles.Lookup lookup, Class<T> extensionClass, Component containerComponent) {
        throw new UnsupportedOperationException();
    }
    static final void preFinalMethod() {
        // Lav versioner der tager 1,2,3 og vargs parametere...

        // Ideen er lidt at vi kan capture alle kald...
        // Ogsaa dem fra final metoder...
        // Hvor vi ikke kan dekore

        // Saa denne klasser bliver kun noedt til at blive kaldt af end-brugere hvis de har en abstract
        // extension klasse... Men har folk det?? I don't think so

        // Man kunne ogsaa bruge en final metode.. Hvis man vil increase sikkerheden...
        // fx setPassword(String password) {
        //// preFinalMethod("******");
        //// ....
        // }
        // Her vil man nok ikke vaelge at
    }

    // protected static void $libraryVersion(Module m);
    // protected static void $libraryWrapper(Module m);

    /**
     * Subtensions are the main that extensions communicate with other extensions. If you are end-user you will most likely
     * never have to deal with these type of classes.
     * 
     * An extension There are no annotations that make sense for this class
     * <p>
     * Subtensions are how extensions
     * 
     * On the basis that is the end-user that determines.
     * 
     * <p>
     * Subtensions must have a non-public constructor. And should not be declared final... Ideen er lidt at vi saa kan
     * dekorere den... og returnere en subclasse... Og paa den maade se hvem der kalder hvilke metoder paa den...
     * 
     * Hvis mig hvad FooExtension laver. Installere Y Compoennt Kalder F metode paa Subtension...
     * 
     * <p>
     * Subclasses of this class supports 2 types of arguments. The extension instance that it belongs to. This is typically
     * easiest expressed by making the subclass an inner class.
     * 
     * {@code Class<? extends Extension>} which is the
     * 
     * <p>
     * New instances of this class is automatically created by the runtime when needed. The instances are never cached,
     * instead a fresh one is created every time it is requested.
     * <p>
     * Subtensions are only available through Extension#use(Class)
     * 
     * @see Extension#use(Class)
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

///**
//* Captures the configuration site by finding the first stack frame where the declaring class of the frame's method is
//* not located on any subclasses of {@link Extension} or any class that implements
//* <p>
//* Invoking this method typically takes in the order of 1-2 microseconds.
//* <p>
//* If capturing of stack-frame-based config sites has been disable via, for example, fooo. This method returns
//* {@link ConfigSite#UNKNOWN}.
//* 
//* @param operation
//*            the operation
//* @return a stack frame capturing config site, or {@link ConfigSite#UNKNOWN} if stack frame capturing has been disabled
//* @see StackWalker
//*/
//// TODO add stuff about we also ignore non-concrete container sources...
//protected final ConfigSite captureStackFrame(String operation) {
//  // API-NOTE This method is not available on ExtensionContext to encourage capturing of stack frames to be limited
//  // to the extension class in order to simplify the filtering mechanism.
//
//  // TODO!!!! I virkeligheden skal man vel bare fange den sidste brug i et bundle....
//  // Kan ogsaa sammenligne med configure navnet...
//
//  if (ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
//      return ConfigSite.UNKNOWN;
//  }
//  Optional<StackFrame> sf = STACK_WALKER.walk(e -> e.filter(f -> !captureStackFrameIgnoreFilter(f)).findFirst());
//  return sf.isPresent() ? configuration().containerConfigSite().thenStackFrame(operation, sf.get()) : ConfigSite.UNKNOWN;
//}

///**
//* @param frame
//*            the frame to filter
//* @return whether or not to filter the frame
//*/
//private final boolean captureStackFrameIgnoreFilter(StackFrame frame) {
//  Class<?> c = frame.getDeclaringClass();
//  // Det virker ikke skide godt, hvis man f.eks. er en metode on a abstract bundle der override configure()...
//  // Syntes bare vi filtrer app.packed.base modulet fra...
//  // Kan vi ikke checke om imod vores container source.
//
//  // ((PackedExtensionContext) context()).container().source
//  // Nah hvis man koere fra config er det jo fint....
//  // Fra config() paa en bundle er det fint...
//  // Fra alt andet ikke...
//
//  // Dvs ourContainerSource
//  return Extension.class.isAssignableFrom(c)
//          || ((Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) && Assembly.class.isAssignableFrom(c));
//}

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
//ErrorHandle, Logging

//ErrorHandling / Notifications ???
/// Taenker det ligger paa Extension'en fordi vi har jo ogsaa en InstantiationContext
//hvor errors jo ogsaa kan ske..
//hasErrors()...
////Maybe we want to log the actual extension as well.
//so extension.log("fooo") instead
/// Yes, why not use it to log errors...

//Den eneste ting jeg kunne forstille mig at kunne vaere public.
//Var en maade at se paa hvordan en extension blev aktiveret..
//Men er det ikke bare noget logning istedet for metoder...
//"InjectorExtension:" Activate
////Her er der noget vi gerne vil have viral.
