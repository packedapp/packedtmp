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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.application.ApplicationDriver;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.bean.BeanExtensionPoint;
import app.packed.bean.hooks.BeanClass;
import app.packed.bean.hooks.BeanField;
import app.packed.bean.hooks.BeanField.AnnotatedWithHook;
import app.packed.bean.hooks.BeanInfo;
import app.packed.bean.hooks.BeanMethod;
import app.packed.bean.hooks.BeanVariable;
import app.packed.inject.Ancestral;
import app.packed.inject.service.ServiceExtension;
import app.packed.inject.service.ServiceExtensionMirror;
import app.packed.operation.dependency.DependencyProvider;
import packed.internal.container.ExtensionModel;
import packed.internal.container.ExtensionSetup;
import packed.internal.container.PackedExtensionTree;
import packed.internal.inject.invoke.InternalInfuser;
import packed.internal.util.StackWalkerUtil;
import packed.internal.util.ThrowableUtil;

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
 * Every extension implementations must provide either an empty (preferable non-public) constructor, or a constructor
 * taking a single parameter of type {@link Ancestral}. The constructor should have package private accessibility to
 * make sure users do not try an manually instantiate it, but instead use
 * {@link BaseContainerConfiguration#useExtension(Class)}. The extension subclass should not be declared final as it is
 * expected that future versions of Packed will supports some debug configuration that relies on extending extensions.
 * And capturing interactions with the extension.
 * 
 * @see ExtensionDescriptor
 * 
 * @param <E>
 *            The type of the extension subclass
 */
public abstract class Extension<E extends Extension<E>> {

    /**
     * The internal configuration of the extension that all methods on {@code Extension} delegate to.
     * <p>
     * This field is initialized in {@link ExtensionSetup#initialize()} via a var handle. The field is _not_ nulled out
     * after the configuration of the extension has completed. This allows for invoking methods such as
     * {@link #checkConfigurable()} at any time.
     * <p>
     * This field should only be accessed via {@link #setup()}.
     */
    @Nullable
    private ExtensionSetup setup;

    /** Creates a new extension. Subclasses should have a single package-private constructor. */
    protected Extension() {}

    /** {@return an extension point for the bean extension.} */
    protected final BeanExtensionPoint bean() {
        return use(BeanExtensionPoint.class);
    }

    /**
     * Checks that the extension is configurable, throwing {@link IllegalStateException} if it is not.
     * 
     * @throws IllegalStateException
     *             if the extension is no longer configurable. Or if invoked from the constructor of the extension
     */
    protected final void checkConfigurable() {
        setup().checkConfigurable();
    }

    /** {@return the path of the container that this extension belongs to.} */
    protected final NamespacePath containerPath() {
        return setup().container.path();
    }

    protected void hookOnBeanBegin(BeanInfo beanInfo) {}

    protected void hookOnBeanClass(BeanClass clazz) {}

    protected void hookOnBeanDependencyProvider(DependencyProvider providr) {}

    protected void hookOnBeanEnd(BeanInfo beanInfo) {}

    /**
     * A callback method that is invoked for any field on a newly added bean where the field:
     * 
     * is annotated with an annotation that itself is annotated with {@link BeanField.AnnotatedWithHook} and where
     * {@link AnnotatedWithHook#extension()} matches the type of this extension.
     * <p>
     * This method is never invoked more than once for a single bean field for any given extension. Even if there are
     * multiple matching hook annotations on the same field. This method will only be called once for the field.
     * 
     * @param field
     *            the bean field
     * @see BeanField.AnnotatedWithHook
     */
    protected void hookOnBeanField(BeanField field) {}

    protected void hookOnBeanMethod(BeanMethod method) {}

    protected void hookOnBeanVariable(BeanVariable variable) {}

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
        return setup().container.isExtensionUsed(extensionType);
    }

    /**
     * {@return whether or not this extension has a parent extension.} Only extensions that are used in the root container
     * of an application does not have a parent extension.
     */
    protected final boolean isRoot() {
        return setup().parent == null;
    }

    /**
     * This method can be overridden to provide a customized mirror for the extension. For example, {@link ServiceExtension}
     * overrides this method to provide an instance of {@link ServiceExtensionMirror}.
     * <p>
     * This method should never return null.
     * 
     * @return a mirror for the extension
     * @see ContainerMirror#extensions()
     */
    protected ExtensionMirror<E> newExtensionMirror() {
        return new ExtensionMirror<>();
    }

    protected ExtensionPoint<E> newExtensionPoint() {
        throw new UnsupportedOperationException(getClass() + " does not define an extension point.");
    }

    /**
     * Invoked by the runtime on the root extension to finalize configuration of the extension.
     * <p>
     * The default implementation of this method will call {@link #onApplicationClose()} on every child. Either pre-order or
     * post-order tree iteration.
     * <p>
     * Packed only calls this method on the root extension so if you want to iterate over all extensions in the tree you
     * should arrange to call <{@code super.onApplicationClose}.
     * <p>
     * <strong>NOTE:</strong> At this stage the set of extensions used by the container are fixed. It is not possible to add
     * extensions that have not already been used, for example, via calls to {@link #use(Class)}. Or indirectly, for
     * example, by installing a bean or linking a container that uses extensions that have not already been used in the
     * extension's container. Failing to follow this rule will result in an {@link InternalExtensionException} being thrown.
     */
    // Hmm InternalExtensionException hvis det er brugerens skyld??
    protected void onApplicationClose() {
        for (ExtensionSetup c = setup().childFirst; c != null; c = c.childSiebling) {
            c.instance().onApplicationClose();
        }
    }

    /**
     * Invoked (by the runtime) when.
     * <p>
     * This method should be used to fail fast.
     * 
     * <p>
     * This is the last opportunity to wire any components that requires extensions that have not already been added.
     * Attempting to wire extensions at a later time will fail with InternalExtensionException
     * <p>
     * If you need, for example, to install extensors that depends on a particular dependency being installed (by other) You
     * should installed via {@link #onApplicationClose()}.
     * 
     * @see #checkIsPreLinkage()
     */
    // When the realm in which the extension's container is located is closed
    protected void onAssemblyClose() {
        ExtensionSetup setup = setup();
        for (ExtensionSetup c = setup.childFirst; c != null; c = c.childSiebling) {
            if (c.container.assembly == setup.container.assembly) {
                c.instance().onAssemblyClose();
            }
        }
    }

    /**
     * Invoked (by the runtime) immediately after the extension has been instantiated (constructor returned successfully),
     * but before the new extension instance is made available to the user.
     * <p>
     * Since most methods on {@code Extension} cannot be invoked from the constructor. This method can be used to perform
     * post instantiation of the extension as needed.
     * 
     * @see #onAssemblyClose()
     * @see #onApplicationClose()
     */
    protected void onNew() {}

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
        return setup().selectWirelets(wireletClass);
    }

    /** {@return the internal configuration of the extension.} */
    private final ExtensionSetup setup() {
        ExtensionSetup s = setup;
        if (s == null) {
            throw new IllegalStateException("This operation cannot be invoked from the constructor of an extension. If you need to perform "
                    + "initialization before the extension is returned to the user, override Extension#onNew()");
        }
        return s;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final ExtensionTree<E> tree() {
        ExtensionSetup setup = setup();
        return new PackedExtensionTree(setup, setup.extensionType);
    }

    // Kunne vaere en mode paa traet?
    // filterOnSameLifetime();
    protected final ExtensionTree<E> treeOfLifetime() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an extension point of the specified type
     * <p>
     * Only extension points of extensions that have been explicitly registered as dependencies, for example, by using
     * {@link DependsOn} may be specified as arguments to this method.
     * <p>
     * This method cannot be called from the constructor of the extension.
     * 
     * @param <P>
     *            the type of extension point to return
     * @param type
     *            the type of extension point to return
     * @return the extension point instance
     * @throws IllegalStateException
     *             If the underlying container is no longer configurable and the extension which the extension point is a
     *             part of has not previously been used.
     * @throws IllegalArgumentException
     *             If the extension which the extension point is a part of has not explicitly been registered as a
     *             dependency of this extension
     */
    protected final <P extends ExtensionPoint<?>> P use(Class<P> type) {
        return setup().use(type);
    }

    // Vi kan sagtens lave den en normal metode taenker jeg maaske paa en utility klasse...
    // Og saa et Lookup object som parameter...
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

        // Create and return a single instance of the bootstrap class
        InternalInfuser.Builder builder = InternalInfuser.builder(MethodHandles.lookup(), c);
        MethodHandle mh = builder.findConstructor(c, e -> new InternalExtensionException(e));
        try {
            return (T) mh.invoke();
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    protected static <T> T $dependsOnIfAvailable2(Class<T> returnType, String testExistence, Lookup ifPresentLookup, String ifPresentClass,
            Supplier<T> ifUnavailable) {

        throw new UnsupportedOperationException();
    }

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
//Static initializers
////Dependencies
////Attributes
////Connections 
////LibraryInfo

//bootstrapConfig
////dependsOn(Codegen)

//////Problemet er den lazy extension thingy can enable andre extensions 
//Configurable -> Parent -> 

class Zarchive {

    /**
     * @param extensionType
     *            the extension type to test
     * @return {@code true} if the extension is disabled, otherwise {@code false}
     * 
     * @see ApplicationDriver.Builder#disableExtension(Class...)
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
//      return setup();
//  }

//    /**
//     * If you always knows that you need a runnable application. For example, schedule extension, concurrency extension,
//     * network extension
//     * <p>
//     * If only certain cirkus stances use checkRunnableApplication()
//     */
//    protected static void $requiresRunnableApplication() {}
//
//    protected static ClassComponentDriverBuilder classBinderFunctional(String functionPrefix, TypeToken<?> token) {
//        classBinderFunctional("fGet", new TypeToken<Consumer<String>>() {});
//        throw new UnsupportedOperationException();
//    }
//
//    protected static ClassComponentDriverBuilder newClassComponentBinderBuilder() {
//        throw new UnsupportedOperationException();
//    }

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
    protected static Optional<Class<? extends Extension<?>>> $dependsOnIfAvailable(String extensionName) {
        return ExtensionModel.bootstrap(StackWalkerUtil.SW.getCallerClass()).dependsOnOptionally(extensionName);
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
