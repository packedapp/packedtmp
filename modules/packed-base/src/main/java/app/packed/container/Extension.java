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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import app.packed.attribute.Attribute;
import app.packed.attribute.AttributeMaker;
import app.packed.base.Nullable;
import app.packed.component.ApplicationImage;
import app.packed.component.Assembly;
import app.packed.component.BaseComponentConfiguration;
import app.packed.component.BuildInfo;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Realm;
import app.packed.component.Wirelet;
import app.packed.component.WireletHandle;
import app.packed.inject.Factory;
import packed.internal.container.ExtensionModel;
import packed.internal.container.ExtensionSetup;
import packed.internal.inject.classscan.Infuser;
import packed.internal.util.StackWalkerUtil;
import packed.internal.util.ThrowableUtil;

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
 * Every extension implementations must provide either an empty (preferable non-public) constructor, or a constructor
 * taking a single parameter of type {@link ExtensionConfiguration}. The constructor should have package private
 * accessibility to make sure users do not try an manually instantiate it, but instead use
 * {@link ContainerConfiguration#use(Class)}. The extension subclass should not be declared final as it is expected that
 * future versions of Packed will supports some debug configuration that relies on extending extensions. And capturing
 * interactions with the extension.
 * 
 * @see ExtensionDescriptor
 */
// Maaske har vi findDescendent(Class<? extends Extension>)

// bootstrapConfig
//// dependsOn(Codegen)

// Extension State
//// Instantiate
//// Link
//// onNew
////// Problemet er den lazy extension thingy can enable andre extensions 
// Configurable -> Parent -> 
public abstract class Extension extends Realm {

    /**
     * The configuration of this extension. This field should never be read directly, but only accessed via
     * {@link #configuration()}.
     * 
     * @apiNote This field is not nulled out after the extension has been configured. This allows for invoking methods such
     *          as {@link #checkConfigurable()} after the configuration is complete.
     */
    @Nullable
    private ExtensionConfiguration configuration;

    /** Create a new extension. Subclasses should have a single package-protected constructor. */
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
    protected final BuildInfo build() {
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

    //checkInNoSubContainers
    protected final void checkUnconnected() {
        // This method cannot be invoked after ServiceExtension has been installed in any sub containers

        // Giver den mening hvis vi ikke connecter???? Det vil jeg ikke mene Ideen er jo at man hiver en eller
        // anden setting op fra parent'en
    }
    
    // checkExtendable...
    /**
     * Checks that the new extensions can be added to the container in which this extension is registered.
     * 
     * @see #onExtensionsFixed()
     */
    // CheckNoLeafs()
    protected final void checkExtendable() {
        configuration().checkExtendable();
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
                    + "initialization before the extension is returned to the user, override " + Extension.class.getSimpleName() + "#onNew()");
        }
        return c;
    }

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

    /**
     * Returns whether or not the specified extension is currently used by this extension, other extensions or user code.
     * 
     * @param extensionClass
     *            the extension to test
     * @return true if the extension is currently in use, otherwise false
     * 
     * @see ExtensionConfiguration#isUsed(Class)
     */
    protected final boolean isInUse(Class<? extends Extension> extensionClass) {
        return configuration().isUsed(extensionClass);
    }

    // Invoked before the first child container
    // Invoke always, even if no child containers
    // If you have configuration that
    // extensionPreambleDone

    protected final void isLeafContainer() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns whether or not the container that this extension belongs is being built into an {@link ApplicationImage}.
     * 
     * @return true if the extension
     */
    // isInImage(), isImageParticipant
    protected final boolean isPartOfImage() {
        return configuration().isPartOfImage();
    }

    /**
     * <p>
     * If this assembly links a container this method must be called from {@link #onComplete()}.
     * 
     * @param assembly
     *            the assembly to link
     * @param wirelets
     *            optional wirelets
     * @throws InternalExtensionException
     *             if the assembly links a container and this method was called from outside of {@link #onComplete()}
     */
    protected final void link(Assembly<?> assembly, Wirelet... wirelets) {
        configuration().link(assembly, wirelets);
    }

    protected final void lookup(MethodHandles.Lookup lookup) {
        ((ExtensionSetup) configuration()).lookup(lookup);
    }

    /**
     * Invoked by the runtime when the configuration of the container is completed.
     * <p>
     * This place is the only place where an extension is allowed to wire new containers, for example, by calling
     * {@link #link(Assembly, Wirelet...)}.
     * <p>
     * T method must not add new extensions. Be careful with the components accepted from users
     */
    protected void onComplete() {
        // Time
        // ──────────────────────────►
        // ┌────────────┐
        // │ Extendable │
        // └────────────┘
        // ┌──────────────────────┐
        // │Configuration │
        // └──────────────────────┘

        // An extension cannot link a container as long as it (the container?) is extendable.
    }

    /**
     * Invoked (by the runtime) immediately after the extension has been instantiated. But before the newly created
     * extension instance is returned to the end-user. Since most methods on this class cannot be invoked from the
     * extension's constructor. This method can be used to perform any needed post instantiation.
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
     * most As most methods in this class is unavailable Unlike the constructor, {@link #configuration()} can be invoked
     * from this method. Is typically used to add new runtime components.
     */
    protected void onNew() {}

    // Hvad hvis den selv tilfoejer komponenter med en child container???
    // Problemet er hvis den bruger extensions som den ikke har defineret
    // Det tror jeg maaske bare ikke den kan

    // onPreUserContainerWiring???
    /**
     * Invoked (by the runtime) when. This is the last opportunity to wire any components that requires extensions that have
     * not already been added. Attempting to wire extensions at a later time will fail with InternalExtensionException
     * 
     * @see #checkExtendable()
     */
    protected void onExtensionsFixed() {
        // if you need information from users to determind what steps to do here.
        // You should guard setting this information with checkExtendable()

        // A lot of shit goes on before linking the first container

        // Must add missing extensions
        // Must not add additional containers

        // So

        // Container wiring must only be done from onComplete

        // lazy operations should be idempotent
    }

    /**
     * Used to lookup other extensions.
     * <p>
     * Only subtensions of extensions that have been explicitly registered as dependencies, for example, by calling using
     * {@link #$dependsOn(Class...)} may be specified as arguments to this method.
     * <p>
     * This method is not available from the constructor of an extension. If you need to call it from the constructor, you
     * can instead declare a dependency on {@link ExtensionConfiguration} and call
     * {@link ExtensionConfiguration#use(Class)}.
     * 
     * @param <E>
     *            the type of subtension to return
     * @param subtensionClass
     *            the type of subtension to return
     * @return the subtension
     * @throws InternalExtensionException
     *             If invoked from the constructor of the extension. Or if the underlying container is no longer
     *             configurable and an extension of the specified type has not already been installed. Or if the extension
     *             of the specified subtension class has not been registered as a dependency of this extension
     * @see ExtensionConfiguration#use(Class)
     * @see #$dependsOn(Class...)
     */
    protected final <E extends Subtension> E use(Class<E> subtensionClass) {
        return configuration().use(subtensionClass);
    }

    // cannot be called
    protected final <C extends ComponentConfiguration> C userWire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return configuration().userWire(driver, wirelets);
    }

    protected final <T extends Wirelet> WireletHandle<T> wirelets(Class<T> wireletClass) {
        return configuration().wirelets(wireletClass);
    }

    // Uhh hvad hvis der er andre dependencies der aktivere den last minute i onBuild()???
    // Vi har jo ligesom lukket for this extension... Og saa bliver den allivel aktiveret!!
    // F.eks. hvis nogle aktivere onBuild().. Igen det er jo en hel chain vi saetter i gang
    /// Maa maaske kigge lidt paa graal og have nogle loekker who keeps retrying

    // Kan have en finishLazy() <-- invoked repeatably every time a new extension is added
    // onFinish cannot add new extensions...

    protected static <T extends Extension, A> void $addAttribute(Class<T> thisExtension, Attribute<A> attribute, Function<T, A> mapper) {}

    protected static <T extends Extension> void $addDependencyLazyInit(Class<? extends Extension> dependency, Class<T> thisExtension,
            Consumer<? super T> action) {
        // Bliver kaldt hvis den specificeret
        // Registeres ogsaa som dependeenc
        // $ = Static Init (s + i = $)
    }

    protected static <T extends Extension, A> void $addOptionalAttribute(Class<T> thisExtension, Attribute<A> attribute, Predicate<T> isPresent) {}

    protected static <T extends Extension> AttributeMaker<T> $attribute(Class<T> thisExtension) {
        throw new Error();
    }

    protected static <T extends Extension> AttributeMaker<T> $attribute(Class<T> thisExtension, Consumer<AttributeMaker<T>> c) {
        throw new Error();
    }

    /**
     * Only parent extensions will be linked
     */
    // Maaske skal vi have det for begge to
    protected static void $connectParentOnly() {
        ExtensionModel.bootstrap(StackWalkerUtil.SW.getCallerClass()).connectParentOnly();
    }

    /**
     * Registers one or more dependencies of this extension.
     * <p>
     * Every extension that another extension uses directly must be explicitly registered. Even if the extension is only
     * used occasionally.
     * 
     * @param extensions
     *            dependencies of this extension
     * @throws InternalExtensionException
     *             if the dependency could not be registered for some reason. For example, if it would lead to a cycles in
     *             the extension graph. Or if this method was not called directly from an extension class initializer
     * @see #$dependsOnOptionally(String)
     * @see #$dependsOnOptionally(String, String, Supplier)
     */
    @SafeVarargs
    protected static void $dependsOn(Class<? extends Extension>... extensions) {
        ExtensionModel.bootstrap(StackWalkerUtil.SW.getCallerClass()).dependsOn(extensions);
    }

    /**
     * Registers an optional dependency of this extension. The extension
     * <p>
     * The class loader of the caller (extension) class will be used when attempting to locate the dependency.
     * 
     * @param extensionName
     *            fully qualified name of the extension class
     * @return the extension class if the extension could be loaded, otherwise empty
     * @throws InternalExtensionException
     *             if the dependency could not be registered for some reason. For example, if it would lead to a cycles in
     *             the extension graph. Or if the specified extension name does not represent a valid extension class. Or if
     *             this method was not called directly from an extension class initializer.
     * @see #$dependsOn(Class...)
     * @see Class#forName(String, boolean, ClassLoader)
     */
    protected static Optional<Class<? extends Extension>> $dependsOnOptionally(String extensionName) {
        return ExtensionModel.bootstrap(StackWalkerUtil.SW.getCallerClass()).dependsOnOptionally(extensionName);
    }

    /**
     * 
     * @param <T>
     *            sd
     * @param extensionName
     *            sd
     * @param bootstrap
     *            sd
     * @param alternative
     *            sd
     * @return stuff
     */
    protected static <T> T $dependsOnOptionally(String extensionName, String bootstrap, Supplier<T> alternative) {
        Optional<Class<? extends Extension>> dep = ExtensionModel.bootstrap(StackWalkerUtil.SW.getCallerClass()).dependsOnOptionally(extensionName);
        // The dependency does not exist, return an alternative value
        if (dep.isEmpty()) {
            return alternative.get();
        }

        // The dependency exists, load bootstrap class
        Class<?> c;
        String s = dep.get().getName() + "$" + bootstrap;
        try {
            c = Class.forName(s, true, dep.get().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new InternalExtensionException("Could not load class " + s, e);
        }

        // Create and return a single instance of it
        MethodHandle mh = Infuser.of(MethodHandles.lookup()).findConstructorFor(c);
        try {
            return (T) mh.invoke();
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    static void $libraryFor(Module module) {
        // Will fail if the module, class does not have version
        // protected static void $libraryVersion(Module|Class m);
        // protected static void $libraryWrapper(Module m);

        // libraryFor(
        // Er det mere et foreignLibray???
        // ConverterExtension er jo sin egen version
        // will extract verions
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

    /**
     * A Subtension is the main way one extension communicate with another extension. If you are an end-user you will most
     * likely never have to deal with these type of classes.
     * <p>
     * 
     * 
     * An extension There are no annotations that make sense for this class
     * <p>
     * Subtensions are how extensions
     * 
     * On the basis that is the end-user that determines.
     * 
     * <p>
     * A Subtension is typically defined as a non-final inner class. It must have an extension as a declaring class. It must
     * have a single (preferable non-public) constructor and should not be declared final. This constructor may have the
     * following two types of services injected:
     * 
     * 
     * 
     * And should not be declared final... Ideen er lidt at vi saa kan dekorere den... og returnere en subclasse... Og paa
     * den maade se hvem der kalder hvilke metoder paa den...
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
     * 
     * @see Extension#use(Class)
     * @see ExtensionConfiguration#use(Class)
     */
    public static abstract class Subtension {}
}
