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

import static internal.app.packed.util.StringFormatter.format;
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.application.BuildException;
import app.packed.application.BuildGoal;
import app.packed.base.NamespacePath;
import app.packed.bean.BeanExtensionPoint;
import app.packed.bean.BeanIntrospector;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceExtensionMirror;
import internal.app.packed.container.ExtensionModel;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.ExtensionTreeSetup;
import internal.app.packed.container.PackedWireletSelection;
import internal.app.packed.container.WireletWrapper;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.StackWalkerUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 * Extensions are the primary way to extend Packed with new features. In fact most features provided by Packed itself is
 * using the same extension mechanism available to any user.
 * <p>
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
 * Every extension implementations must provide either an empty constructor. The constructor should have package private
 * accessibility to make sure users do not try an manually instantiate it, but instead use
 * {@link ContainerConfiguration#use(Class)}. The extension subclass should not be declared final as it is expected that
 * future versions of Packed will supports some debug configuration that relies on extending extensions. And capturing
 * interactions with the extension.
 * 
 * @see ExtensionDescriptor
 * 
 * @param <E>
 *            The type of the extension subclass
 */
// Platform extension, Base extension
// Platform extension kunne baade vaere any packed extension. Og en platform extension
public abstract class Extension<E extends Extension<E>> {

    /** The internal configuration of the extension. */
    final ExtensionSetup extension; //handle + handle()

    /**
     * Creates a new extension. Subclasses should have a single package-private constructor.
     * 
     * @throws IllegalStateException
     *             if attempting to construct the extension manually
     */
    protected Extension() {
        this.extension = ExtensionSetup.initalizeExtension(this);
    }

    /** {@return an extension point for the bean extension.} */
    protected final BeanExtensionPoint bean() {
        return use(BeanExtensionPoint.class);
    }

    /** {@return the build goal.} */
    protected final BuildGoal buildGoal() {
        return extension.container.application.goal;
    }

    /**
     * Checks that the extension is configurable, throwing {@link IllegalStateException} if it is not.
     * 
     * @throws IllegalStateException
     *             if the extension is no longer configurable.
     */
    protected final void checkIsConfigurable() {
        ExtensionTreeSetup realm = extension.extensionRealm;
        if (realm.isClosed()) {
            throw new IllegalStateException(realm.realmType() + " is no longer configurable");
        }
    }

    protected final ContainerHandle containerBuilder(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    /** {@return the path of the container that this extension belongs to.} */
    protected final NamespacePath containerPath() {
        return extension.container.path();
    }

    // Ved ikke om vi draeber den, eller bare saetter en stor warning
    // Problemet er at den ikke fungere skide godt paa fx JFR extension.
    // Her er det jo root container vi skal teste
    /**
     * Returns whether or not the specified extension is currently used by this extension, other extensions or user code.
     * 
     * @param extensionType
     *            the extension type to test
     * @return {@code true} if the extension is currently in use, otherwise {@code false}
     * @implNote Packed does not perform detailed tracking on what extensions use other extensions. So it cannot answer
     *           questions about what exact extension is using another extension
     */
    protected final boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
        return extension.container.isExtensionUsed(extensionType);
    }

    /**
     * @return
     */
    protected final boolean isRoot() {
        return extension.treeParent == null;
    }

    /**
     * Returns an extension navigator with this extension instance as current.
     * 
     * @return a new extension navigator
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final ExtensionNavigator<E> navigator() {
        return new ExtensionNavigator(extension, extension.extensionType);
    }

    /**
     * Whenever a Hook annotation is found
     * <p>
     * This method is never called more than once for a single bean.
     * 
     * @return a new bean introspector
     * 
     * @throws InternalExtensionException
     *             if the method is not overridden
     */
    protected BeanIntrospector newBeanIntrospector() {
        // TODO we should provide some context... 
        // Or maybe just return a default BeanIntrospector, where nothing is overridden
        throw new InternalExtensionException("This method must be overridden by " + extension.extensionType);
    }

    /**
     * This method can be overridden to provide a customized mirror for the extension. For example, {@link ServiceExtension}
     * overrides this method to provide an instance of {@link ServiceExtensionMirror}.
     * <p>
     * This method should never return null.
     * 
     * @return a mirror for the extension
     * @throws InternalExtensionException
     *             if the method is not overridden
     */
    protected ExtensionMirror<E> newExtensionMirror() {
        // This method is only called if an exception forgot to override the method
        throw new InternalExtensionException("This method must be overridden by " + extension.extensionType);
    }

    /**
     * 
     * <p>
     * This method should never return null.
     * 
     * @return a new extension point
     * 
     * @throws UnsupportedOperationException
     *             if the extension does not support extension points.
     */
    protected ExtensionPoint<E> newExtensionPoint() {
        // I think it is the same as newExtensionMirror an internal excetion
        throw new InternalExtensionException("This method must be overridden by " + extension.extensionType);
    }

    /**
     * Invoked by the runtime on the root extension to finalize configuration of the extension.
     * <p>
     * The default implementation of this method will call {@link #onApplicationClose()} on every child. Either pre-order or
     * post-order tree iteration.
     * <p>
     * Packed only calls this method on the root extension. so if you want to iterate over all extensions in the tree you
     * should arrange to call <{@code super.onApplicationClose}.
     * <p>
     * <strong>NOTE:</strong> At this stage the set of extensions used by the container are fixed. It is not possible to add
     * extensions that have not already been used, for example, via calls to {@link #use(Class)}. Or indirectly, for
     * example, by installing a bean or linking a container that uses extensions that have not already been used in the
     * extension's container. Failing to follow this rule will result in an {@link InternalExtensionException} being thrown.
     */
    // Hmm InternalExtensionException hvis det er brugerens skyld??
    protected void onApplicationClose() {
        for (ExtensionSetup e = extension.treeFirstChild; e != null; e = e.treeNextSiebling) {
            e.instance().onApplicationClose();
        }
    }

    /**
     * Invoked (by the runtime) after {@link Assembly#build()} has returned successfully from the container where this
     * extension is used.
     * <p>
     * This method is typically use to
     * 
     * <p>
     * Extensions that depends on this extension will always have their {@link #onAssemblyClose()} method executed before
     * this extension. Therefore you can assume that noone will
     * 
     * If there are other extension that depends on this extension.
     * 
     * This method is always invoked after
     * 
     * <p>
     * This is the last opportunity to wire any components that requires extensions that have not already been added.
     * Attempting to wire extensions at a later time will fail with InternalExtensionException
     * <p>
     * If you need, for example, to install bean that depends on a particular dependency being installed (by other) You
     * should installed via {@link #onApplicationClose()}.
     * 
     * @see #checkIsPreLinkage()
     */
    // When the realm in which the extension's container is located is closed
    protected void onAssemblyClose() {
        ExtensionSetup s = extension;
        for (ExtensionSetup c = s.treeFirstChild; c != null; c = c.treeNextSiebling) {
            if (c.container.assembly == s.container.assembly) {
                c.instance().onAssemblyClose();
            }
        }
    }

    /**
     * Invoked (by the runtime) immediately after the extension has been instantiated (constructor returned successfully),
     * but before the new extension instance is made available to the user.
     * <p>
     * Since most methods on {@code Extension} cannot be invoked from the constructor. This method can be used instead to
     * perform post instantiation of the extension as needed.
     * 
     * @see #onAssemblyClose()
     * @see #onApplicationClose()
     */
    protected void onNew() {}

    /** @return the parent of this extension if present. */
    @SuppressWarnings("unchecked")
    protected final Optional<E> parent() {
        ExtensionSetup parent = extension.treeParent;
        return parent == null ? Optional.empty() : Optional.of((E) parent.instance());
    }

    /** {@return the root extension in the application.} */
    @SuppressWarnings("unchecked")
    protected final E root() {
        ExtensionSetup s = extension;
        while (s.treeParent != null) {
            s = s.treeParent;
        }
        return (E) s.instance();
    }

    /**
     * Returns a selection of all wirelets of the specified type that have not already been processed.
     * <p>
     * If this extension defines any runtime wirelet. A check must also be made at runtime, you must remember to check if
     * there are any unprocessed wirelets at runtime. As this may happen when creating an image
     * 
     * @param <T>
     *            the type of wirelets to select
     * @param wireletClass
     *            the type of wirelets to select
     * @return a selection of all container wirelets of the specified type that have not already been processed
     * @throws IllegalArgumentException
     *             if the specified class is not located in the same module as the extension itself. Or if the specified
     *             wirelet class is not a proper subclass of ContainerWirelet.
     */
    protected final <T extends Wirelet> WireletSelection<T> selectWirelets(Class<T> wireletClass) {
        // Check that we are a proper subclass of ExtensionWirelet
        ClassUtil.checkProperSubclass(Wirelet.class, wireletClass, "wireletClass");

        // We only allow selection of wirelets in the same module as the extension itself
        // Otherwise people could do wirelets(ServiceWirelet.provide(..).getClass())...
        if (getClass().getModule() != wireletClass.getModule()) {
            throw new IllegalArgumentException("The specified wirelet class is not in the same module (" + getClass().getModule().getName() + ") as '"
                    + /* simple extension name */ extension.descriptor().name() + ", wireletClass.getModule() = " + wireletClass.getModule());
        }

        // Find the containers wirelet wrapper and return early if no wirelets have been specified, or all of them have already
        // been consumed
        WireletWrapper wirelets = extension.container.wirelets;
        if (wirelets == null || wirelets.unconsumed() == 0) {
            return WireletSelection.of();
        }

        return new PackedWireletSelection<>(wirelets, wireletClass);
    }

    /**
     * Returns an extension point of the specified type.
     * <p>
     * Only extension points of extensions that have been explicitly registered as dependencies, for example, by using
     * {@link DependsOn} may be specified as arguments to this method.
     * 
     * @param <P>
     *            the type of extension point to return
     * @param extensionPointClass
     *            the type of extension point to return
     * @return the extension point instance
     * @throws IllegalStateException
     *             If the underlying container is no longer configurable and the extension which the extension point is a
     *             part of has not previously been used.
     * @throws InternalExtensionException
     *             If the extension which the extension point is a part of has not explicitly been registered as a
     *             dependency of this extension
     */
    @SuppressWarnings("unchecked")
    protected final <P extends ExtensionPoint<?>> P use(Class<P> extensionPointClass) {
        requireNonNull(extensionPointClass, "extensionPointClass is null");

        // Extract the extension class from ExtensionPoint<E>
        Class<? extends Extension<?>> otherExtensionClass = ExtensionPoint.EXTENSION_POINT_TO_EXTENSION_CLASS_EXTRACTOR.get(extensionPointClass);

        // Check that the extension of requested extension point's is a direct dependency of the requesting extension
        if (!extension.descriptor().dependsOn(otherExtensionClass)) {
            // Special message if you try to use your own extension point
            if (otherExtensionClass == getClass()) {
                throw new InternalExtensionException(otherExtensionClass.getSimpleName() + " cannot use its own extension point " + extensionPointClass);
            }
            throw new InternalExtensionException(
                    getClass().getSimpleName() + " must declare " + format(otherExtensionClass) + " as a dependency in order to use " + extensionPointClass);
        }

        ExtensionSetup otherExtension = extension.container.safeUseExtensionSetup(otherExtensionClass, extension);

        // Create a new extension point
        ExtensionPoint<?> newExtensionPoint = otherExtension.instance().newExtensionPoint();

        // Make sure it is a proper type of the requested extension point
        if (!extensionPointClass.isInstance(newExtensionPoint)) {
            if (newExtensionPoint == null) {
                throw new NullPointerException(
                        "Extension " + otherExtension.descriptor().fullName() + " returned null from " + otherExtension.descriptor().name() + ".newExtensionPoint()");
            }
            throw new InternalExtensionException(otherExtension.extensionType.getSimpleName() + ".newExtensionPoint() was expected to return an instance of "
                    + extensionPointClass + ", but returned an instance of " + newExtensionPoint.getClass());
        }

        // Initializes the extension point
        newExtensionPoint.initialize(otherExtension, extension);

        return (P) newExtensionPoint;
    }

    // Vi kan sagtens lave den en normal metode fx pa ExtensionPoint...
    // Og saa et Lookup object som parameter...
    @SuppressWarnings("unchecked")
    protected static <T> T $dependsOnIfAvailable(String extensionName, String bootstrapClass, Supplier<T> alternative) {
        Class<?> callerClass = StackWalkerUtil.SW.getCallerClass();
        // Attempt to load an extension with the specified name
        Optional<Class<? extends Extension<?>>> dependency = ExtensionModel.bootstrap(callerClass).dependsOnOptionally(extensionName);
        // The extension does not exist, return an alternative value
        if (dependency.isEmpty()) {
            return alternative.get();
        }

        // The dependency exists, load the bootstrap class
        Class<?> c;
        String bootstrapClassName = dependency.get().getName() + "$" + bootstrapClass;
        try {
            c = Class.forName(bootstrapClassName, true, callerClass.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new InternalExtensionException("Could not load class " + bootstrapClassName, e);
        }

        // Must be a static class. As the value should be stored in a static field
        if (!Modifier.isStatic(c.getModifiers())) {
            throw new IllegalArgumentException();
        }

        MethodHandle constructor;
        // TODO fix visibility
        try {
            constructor = MethodHandles.lookup().findConstructor(c, MethodType.methodType(void.class));
        } catch (NoSuchMethodException e) {
            throw new BuildException("A container hook must provide an empty constructor, hook = " + c, e);
        } catch (IllegalAccessException e) {
            throw new BuildException("Can't see it sorry, hook = " + c, e);
        }
        Object result;

        try {
            result = constructor.invoke();
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
        return (T) result;
    }

    protected static <T> T $dependsOnIfAvailable2(Class<T> returnType, String testExistence, Lookup ifPresentLookup, String ifPresentClass,
            Supplier<T> ifUnavailable) {

        throw new UnsupportedOperationException();
    }

    // I think we need some more use cases
    public static abstract class Bootstrap {

        protected abstract void bootstrap();

        protected final void dependsOn(Class<? extends Extension<?>> extensionClass) {

        }

        protected final <T> T dependsOnIfAvailable(String extensionName, String bootstrapClass, Supplier<T> alternative) {
            return alternative.get();
        }
    }

    public @interface BootstrapWith {
        Class<? extends Extension.Bootstrap> value();
    }

    /**
     * If an extension depends on other extensions (most do). They must annotated with this annotation, indicating exactly
     * what extensions they depend upon. This should include dependencies that are only used in some cases.
     * <p>
     * Trying to use other that use other extensions that are not explicitly defined using this annotation. Will fail by
     * throwing an {@link InternalExtensionException}. This includes both explicit usage, for example, via
     * {@link Extension#use(Class)} or usage of hook annotations from other extensions.
     * <p>
     * All classes that are declared as dependencies will be loaded together with annotated extension. However, the
     * dependency (extension) class will not be initialized before it is usage for the first time.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface DependsOn {

        /** {@return other extensions the annotated extension depends on.} */
        Class<? extends Extension<?>>[] extensions() default {};

        // Den der $dependsOnOptionally(String, Class, Supplier) kan vi stadig have
        // Den kraever bare at den allerede har vaeret listet som optionally
        /**
         * Extensions that are optionally used will be attempted to be resolved using the annotated extension classes class
         * loader.
         */
        String[] optionally() default {};
    }
}

class Zarchive {

    /**
     * @param extensionType
     *            the extension type to test
     * @return {@code true} if the extension is disabled, otherwise {@code false}
     * 
     */
    // Kan disable den paa application driver...
    // Er det kombination af isExtensionDisabled og isUsed
    /// Maaske bare Set<Class<? extends Extension<?>>> disabledExtensions(); disabledExtension.contains
    /// Maaske vi skal have en selvstaedig classe.
    /// Disabled kan ogsaa vaere hvis vi koere med whitelist

    /// Hmm. Hvis nu en extension har en optional use af en extension.. Saa kan vi jo ikke svare paa det her
    /// Maaske det er vigtigt at have de 2 options.
    /// isExtensionUsable() , makeUnusable
    protected final boolean isExtensionBanned(Class<? extends Extension<?>> extensionType) {
        throw new UnsupportedOperationException();
    }

    protected static <T extends Extension<T>> void $addDependencyLazyInit(Class<? extends Extension<?>> dependency, Class<T> thisExtension,
            Consumer<? super T> action) {
        // Bliver kaldt hvis den specificeret
        // Registeres ogsaa som dependeenc
        // $ = Static Init (s + i = $)
    }

//  protected static <T extends Extension> AttributeMaker<T> $attribute(Class<T> thisExtension) {
//      throw new Error();
//  }
//
//  protected static <T extends Extension> AttributeMaker<T> $attribute(Class<T> thisExtension, Consumer<AttributeMaker<T>> c) {
//      throw new Error();
//  }

    // Uhh hvad hvis der er andre dependencies der aktivere den last minute i onBuild()???
    // Vi har jo ligesom lukket for this extension... Og saa bliver den allivel aktiveret!!
    // F.eks. hvis nogle aktivere onBuild().. Igen det er jo en hel chain vi saetter i gang
    /// Maa maaske kigge lidt paa graal og have nogle loekker who keeps retrying

    // Kan have en finishLazy() <-- invoked repeatably every time a new extension is added
    // onFinish cannot add new extensions...

//  protected static <T extends Extension, A> void $attributeAdd(Class<T> thisExtension, Attribute<A> attribute, Function<T, A> mapper) {}
//
//  protected static <T extends Extension, A> void $attributeAddOptional(Class<T> thisExtension, Attribute<A> attribute, Predicate<T> isPresent) {}

//  /**
//   * Only parent extensions will be linked
//   */
//  // Maaske skal vi have det for begge to
//  protected static void $connectParentOnly() {
//      ExtensionModel.bootstrap(StackWalkerUtil.SW.getCallerClass()).connectParentOnly();
//  }

    // An instance of extensorType will automatically be installed whenever the extensor is used
    // protected static <T extends Extension, A> void $autoInstallExtensor(Class<? extends ExtensionBeanOld<?>>
    // extensorType) {}

    // Hmm, er det overhoved interessant at faa en Subtension???
    // Vil vi ikke hellere have extensionen.
    // Og man kan vel ikke bruge hook annoteringer
//  
//  @SafeVarargs
//  protected static void $cycleBreaker(Class<? extends Extension<?>>... extensions) {
    // Man maa saette den via noget VarHandle vaerk

//      // A -DependsOn(B)
//      // B -cycleBreaker(A) // Man den scanner den ikke, den markere den bare
//
//      // Specified extension must have a dependency on this extension
//      // And must be in same module
//      throw new UnsupportedOperationException();
//  }

//  protected static void $lookup(MethodHandles.Lookup lookup) {
//      // Nej den giver sgu ikke saerlig god mening...
//      // Men har et requirement paa app.packed.base
//      // 
//  }
    //
//  /**
//   * Returns a configuration object for this extension. The configuration object can be used standalone in situations
//   * where the extension needs to delegate responsibility to classes that cannot invoke the protected methods on
//   * {@code Extension}, for example, due to class-member visibility rules.
//   * <p>
//   * This method will fail with {@link IllegalStateException} if invoked from the constructor of the extension. If you
//   * need to use an extension configuration in the constructor. You can declare {@code ExtensionConfiguration} as a
//   * parameter in the extension's constructor and the let the runtime dependency inject it into the extension instance.
//   * Another alternative is to override {@link #onNew()} to perform post initialization.
//   * 
//   * @throws IllegalStateException
//   *             if invoked from the constructor of the extension.
//   * @return a configuration object for this extension
//   */
//  protected final ExtensionConfiguration configuration() {
//      return setup;
//  }

    /**
     * Registers an optional dependency of this extension. The extension
     * <p>
     * The class loader of the caller (extension) class will be used when attempting to locate the dependency.
     * 
     * @param extensionName
     *            fully qualified name of the extension class
     * @return the extension class if the extension could be loaded, otherwise empty
     * @throws UnsupportedOperationException
     *             if the dependency could not be registered for some reason. For example, if it would lead to a cycles in
     *             the extension graph. Or if the specified extension name does not represent a valid extension class. Or if
     *             this method was not called directly from an extension class initializer.
     * @see #$dependsOn(Class...)
     * @see Class#forName(String, boolean, ClassLoader)
     */
    protected static Optional<Class<? extends Extension<?>>> $dependsOnIfAvailable(String extensionName) {
//        return ExtensionModel.bootstrap(StackWalkerUtil.SW.getCallerClass()).dependsOnOptionally(extensionName);
        return Optional.empty();
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

    protected static void $requiresClassGenFullAccessToModule() {
        // Ideen er lidt at man skal markere hvis man skal have adgang til Classgen

        // Det kan ogsaa bare vaere en dependency paa en extension...
        // Det er faktisk maaske det lettes
        // dependsOn(FullClassGenExtension.class);

        // Tror faktisk ikke vi supportere det udover annotation

    }

    static final void preFinalMethod() {
        // Lav versioner der tager 1,2,3 og vargs parametere...

        // Ideen er lidt at vi kan capture alle kald...
        // Ogsaa dem fra final metoder...
        // Hvor vi ikke kan dekore

        /// Are we complicating things to much???

        // Saa denne klasser bliver kun noedt til at blive kaldt af end-brugere hvis de har en abstract
        // extension klasse... Men har folk det?? I don't think so

        // Man kunne ogsaa bruge en final metode.. Hvis man vil increase sikkerheden...
        // fx setPassword(String password) {
        //// preFinalMethod("******");
        //// ....
        // }
        // Her vil man nok ikke vaelge at
    }

    // maybe dependsOn, dependsOnOptionally, dependsOnIfAvailable(always optionally=
}
