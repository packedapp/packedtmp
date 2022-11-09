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
import java.util.Optional;

import app.packed.application.BuildGoal;
import app.packed.bean.BeanExtensionPoint;
import app.packed.bean.BeanIntrospector;
import app.packed.framework.NamespacePath;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceExtensionMirror;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.ExtensionTreeSetup;
import internal.app.packed.container.PackedWireletSelection;
import internal.app.packed.container.WireletWrapper;
import internal.app.packed.util.ClassUtil;

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
