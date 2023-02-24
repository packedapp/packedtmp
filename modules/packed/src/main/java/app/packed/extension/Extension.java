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
package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

import app.packed.application.ApplicationPath;
import app.packed.application.BuildGoal;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import app.packed.extension.container.ContainerHandle;
import app.packed.service.ServiceableBeanConfiguration;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.PackedContainerHandle;
import internal.app.packed.container.PackedWireletSelection;
import internal.app.packed.container.WireletWrapper;
import internal.app.packed.util.StringFormatter;
import internal.app.packed.util.types.ClassUtil;

/**
 * Extensions are main mechanism by which the framework can be extended with new features.
 *
 * Extensions are the primary way to extend the framework with new features. In fact most features provided by Packed
 * itself is using the same extension mechanism available to any user.
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
public abstract class Extension<E extends Extension<E>> {

    /** The internal configuration of the extension. */
    final ExtensionSetup extension = ExtensionSetup.initalizeExtension(this);

    /**
     * Creates a new extension. Subclasses should have a single package-private constructor.
     *
     * @throws IllegalStateException
     *             if attempting to construct the extension manually
     */
    protected Extension() {}

    /**
     * Returns an extension navigator with this extension instance as the current extension.
     *
     * @return an extension navigator
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    // ApplicationNavigator?
    protected final ExtensionNavigator<E> applicationNavigator() {
        return new ExtensionNavigator(extension, extension.extensionType);
    }

    /**
     * {@return an instance of this extension that is used in the application's root container. Will return this if this
     * extension is the root extension}
     */
    @SuppressWarnings("unchecked")
    protected final E applicationRoot() {
        ExtensionSetup s = extension;
        while (s.treeParent != null) {
            s = s.treeParent;
        }
        return (E) s.instance();
    }

    /** {@return the base extension point.} */
    protected final BaseExtensionPoint base() {
        return use(BaseExtensionPoint.class);
    }

    /** {@return the build goal of the application.} */
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
        if (!extension.isConfigurable()) {
            throw new IllegalStateException(extension.extensionType + " is no longer configurable");
        }
    }

    /** {@return the path of the container that this extension belongs to.} */
    protected final ApplicationPath containerPath() {
        return extension.container.path();
    }

    /**
     * Returns an extension instance from the container represented by the specified container handle. Or empty if the
     * extension is not used in the container.
     *
     * @param handle
     *            represent the container for which the extension should be returned
     * @return the extension or empty
     */
    @SuppressWarnings("unchecked")
    protected final Optional<E> fromHandle(ContainerHandle handle) {
        requireNonNull(handle, "handle is null");
        ExtensionSetup s = ((PackedContainerHandle) handle).container().extensions.get(extension.extensionType);
        return s == null ? Optional.empty() : Optional.ofNullable((E) s.instance());
    }

    /** {@return whether or not this extension's container is the root container in the application.} */
    protected final boolean isApplicationRoot() {
        return extension.container.isApplicationRoot();
    }

    /**
     * Returns whether or not the specified extension is currently used by this extension, other extensions or user code.
     *
     * @param extensionType
     *            the extension type to test
     * @return {@code true} if the extension is currently in use, otherwise {@code false}
     * @implNote Packed does not perform detailed tracking on what extensions use other extensions. So it cannot answer
     *           questions about what exact extension is using another extension
     */
    // Ved ikke om vi draeber den, eller bare saetter en stor warning
    // Problemet er at den ikke fungere skide godt paa fx JFR extension.
    // Her er det jo root container vi skal teste
    protected final boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
        return extension.container.isExtensionUsed(extensionType);
    }

    /** {@return whether or not this container is the root of its lifetime.} */
    protected final boolean isLifetimeRoot() {
        return extension.container.isLifetimeRoot();
    }

    /**
     * Returns the instance of this extension that is the root of this container's lifetime. If this container is the root
     * returns this.
     *
     * @return the instance of this extension that is root of this container's lifetime
     */
    // What about cross application???
    @SuppressWarnings("unchecked")
    protected final E lifetimeRoot() {
        ExtensionSetup s = extension;
        while (s.treeParent != null) {
            if (s.container.lifetime != s.treeParent.container.lifetime) {
                s = s.treeParent;
            }
        }
        return (E) s.instance();
    }

    /**
     * Returns a bean introspector.
     *
     * Whenever a Hook annotation is found
     * <p>
     * This method is called exactly once for a single bean if needed.
     *
     * @return a new bean introspector
     */
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {};
    }

    /**
     * This method can be overridden to provide a customized {@link ExtensionMirror mirror} for the extension. For example,
     * {@link BaseExtension} overrides this method to provide an instance of {@link BaseExtensionMirror}.
     * <p>
     * This method should never return null.
     *
     * @return a customized mirror for the extension
     * @throws InternalExtensionException
     *             if the extension defines an extension mirror but does not override this method.
     */
    protected ExtensionMirror<E> newExtensionMirror() {
        // This exception is only throw if the extension forgot to override the method
        throw new InternalExtensionException("A customized extension mirror was declared, but this method was not overridden by " + extension.extensionType);
    }

    /**
     * Returns a new extension point for the extension
     * <p>
     * This method should never return null.
     *
     * @return a new extension point
     *
     * @throws InternalExtensionException
     *             if the extension defines an extension point but does not override this method.
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
        // childCursor
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
    // TODO Hmm doesnt work properly any more... Why?
    // I think either we need to fail for example
    protected void onNew() {}

    /**
     * @return the parent of this extension if present. Or empty if the extension is in the root container of the
     *         application.
     */
    @SuppressWarnings("unchecked")
    protected final Optional<E> parent() {
        ExtensionSetup parent = extension.treeParent;
        return parent == null ? Optional.empty() : Optional.of((E) parent.instance());
    }

    protected final <T> ServiceableBeanConfiguration<T> provide(Class<T> implementation) {
        return base().install(implementation).provide();
    }

    /**
     * Registers a action to run doing the code generation phase of the application.
     * <p>
     * If the application has no code generation phase. For example, if building a {@link BuildGoal#MIRROR}. The specified
     * action will not be executed.
     *
     * @param action
     *            the action to run
     * @throws IllegalStateException
     *             if the extension is no longer configurable
     * @see BuildGoal#isCodeGenerating()
     */
    // add void runOnBuildCompleted, void runOnBuildFailed(Throwable cause); lad os lige faa nogle use cases foerst
    protected final void runOnCodegen(Runnable action) {
        checkIsConfigurable();
        extension.container.application.addCodeGenerator(action);
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
                    + /* simple extension name */ extension.model.name() + ", wireletClass.getModule() = " + wireletClass.getModule());
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
     * Only extension points of extensions that have been explicitly registered as dependencies using {@link DependsOn} may
     * be specified as arguments to this method.
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
     *             If the extension which the extension point is a part of has not explicitly been declared as a dependency
     *             of this extension
     */
    protected final <P extends ExtensionPoint<?>> P use(Class<P> extensionPointClass) {
        requireNonNull(extensionPointClass, "extensionPointClass is null");

        // Extract the extension class (<E>) from ExtensionPoint<E>
        Class<? extends Extension<?>> otherExtensionClass = ExtensionPoint.TYPE_VARIABLE_EXTRACTOR.get(extensionPointClass);

        // Check that the extension of requested extension point's is a direct dependency of this extension
        if (!extension.model.dependsOn(otherExtensionClass)) {
            // An extension cannot use its own extension point
            if (otherExtensionClass == getClass()) {
                throw new InternalExtensionException(otherExtensionClass.getSimpleName() + " cannot use its own extension point " + extensionPointClass);
            }
            throw new InternalExtensionException(getClass().getSimpleName() + " must declare " + StringFormatter.format(otherExtensionClass)
                    + " as a dependency in order to use " + extensionPointClass);
        }

        ExtensionSetup otherExtension = extension.container.useExtension(otherExtensionClass, extension);

        // Create a new extension point
        ExtensionPoint<?> newExtensionPoint = otherExtension.instance().newExtensionPoint();

        if (newExtensionPoint == null) {
            throw new NullPointerException(
                    "Extension " + otherExtension.model.fullName() + " returned null from " + otherExtension.model.name() + ".newExtensionPoint()");
        }

        // Make sure it is a proper type of the requested extension point
        if (!extensionPointClass.isInstance(newExtensionPoint)) {
            throw new InternalExtensionException(otherExtension.extensionType.getSimpleName() + ".newExtensionPoint() was expected to return an instance of "
                    + extensionPointClass + ", but returned an instance of " + newExtensionPoint.getClass());
        }

        // Initializes the extension point
        newExtensionPoint.initialize(otherExtension, extension);

        return extensionPointClass.cast(newExtensionPoint);
    }

    /**
     * Uses an extension that explicitly depends on this extension.
     * <p>
     *
     * <p>
     * The dependent extension must have declared this extension as a dependency using {@link DependsOn}. Otherwise this
     * method will throw an {@link InternalExtensionException}.
     *
     * @param <D>
     *            the type of dependent extension
     * @param dependentExtension
     * @return the dependent extension
     * @throws InternalExtensionException
     *             if the dependent extension does not have a direct dependency on this extension. Or if the dependent
     *             extension is not in the same module as this extension
     */
    protected final <D extends Extension<D>> D useDependent(Class<D> dependentExtension) {
        // Ideen er at vi kan slaa nogen der er dependant paa os op.
        // Same module
        // Tror alligevel det er extension point vi skal returnere... IDK
        // fx BaseExtension.useDependant(ThreadExtension.class);
        throw new UnsupportedOperationException();
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

        /**
         * {@return other extensions the annotated extension depends on.}
         * <p>
         * {@link BaseExtension} is a mandated extension, and cannot be specified.
         */
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
//
///** {@return instance of this extension that is used in the lifetimes assembly container.} */
//@SuppressWarnings("unchecked")
//// I don't know if we need this for anything...
//final E assemblyRoot() {
//    ExtensionSetup s = extension;
//    while (s.treeParent != null) {
//        if (s.container.assembly != s.treeParent.container.assembly) {
//            s = s.treeParent;
//        }
//    }
//    return (E) s.instance();
//}
//
///**
// * Returns a container handle for the extension's container. If this extension installed the container.
// * <p>
// * When creating a new container the assembly. The handle returned is always closed
// *
// * @return
// * @throws UnsupportedOperationException
// *             if this extension this not install this extension's container
// */
//// I'm not sure this is needed
//protected final ContainerHandle containerHandle() {
//    throw new UnsupportedOperationException();
//}