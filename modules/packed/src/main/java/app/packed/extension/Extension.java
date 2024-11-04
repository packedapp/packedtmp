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
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

import app.packed.BaseModuleNames;
import app.packed.build.BuildCodeSource;
import app.packed.build.BuildGoal;
import app.packed.component.ComponentPath;
import app.packed.container.ContainerHandle;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import app.packed.extension.Extension.ExtensionProperty;
import app.packed.extension.ExtensionPoint.ExtensionUseSite;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceTemplate;
import app.packed.service.ProvidableBeanConfiguration;
import app.packed.util.TreeView;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.extension.PackedExtensionHandle;
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

//Nooooo, if fx Logging is a custom BindingHook on Assembly.
// We also need to have this fucker on Extension. Or maybe allow no customization on extension beans
// ContainerLocalSource?
public non-sealed abstract class Extension<E extends Extension<E>> implements BuildCodeSource {

    /** The internal configuration of the extension. */
    final ExtensionSetup extension;

    final PackedExtensionHandle<E> handle;

    /**
     * Creates a new extension. Subclasses should have a single package-private constructor taking {@link ExtensionHandle}
     * as the single parameter.
     *
     * @param handle
     *            the extension's handle
     *
     * @throws IllegalStateException
     *             if attempting to construct the extension manually
     */
    protected Extension(ExtensionHandle<E> handle) {
        this.handle = (PackedExtensionHandle<E>) requireNonNull(handle);
        // Will fail if the extension is not initialized by the framework
        this.extension = ((PackedExtensionHandle<?>) handle).extension();
    }

    /**
     * Returns a node representing this extension in the application's extension tree.
     *
     * @return an extension navigator
     */
    protected final TreeView.Node<E> applicationNode() {
        return handle.applicationNode();
    }

    /**
     * {@return an instance of this extension that is used in the application's root container. Will return this if this
     * extension is the root extension}
     */
    @SuppressWarnings("unchecked")
    protected final E applicationRoot() {
        return (E) extension.root().instance();
    }

    /** {@return the base extension point.} */
    protected final BaseExtensionPoint base() {
        return use(BaseExtensionPoint.class);
    }

    /** {@return the build goal.} */
    protected final BuildGoal buildGoal() {
        return extension.container.application.deployment.goal;
    }

    /**
     * Checks that the extension is configurable, throwing {@link IllegalStateException} if it is not.
     *
     * @throws IllegalStateException
     *             if the extension is no longer configurable.
     */
    protected final void checkIsConfigurable() {
        handle.checkIsConfigurable();
    }

    /** {@return the path of the container that this extension belongs to.} */
    protected final ComponentPath containerPath() {
        return handle.containerPath();
    }

    /**
     * {@return The short name of the extension}
     * <p>
     * If there are multiple extensions with the same short name in a single application the framework will name them
     * uniquely.
     *
     * @implNote Typically, by postfixing
     */
    protected final String extensionName() {
        return extension.tree.name;
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
    protected final Optional<E> fromContainerHandle(ContainerHandle<?> handle) {
        requireNonNull(handle, "handle is null");
        ExtensionSetup s = ContainerSetup.crack(handle).extensions.get(extension.extensionType);
        return s == null ? Optional.empty() : Optional.ofNullable((E) s.instance());
    }

    protected final ExtensionHandle<E> handle() {
        return handle;
    }

    /** {@return whether or not this extension's container is the root container in the application.} */
    protected final boolean isApplicationRoot() {
        return extension.treeParent == null;
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
        return handle.isExtensionUsed(extensionType);
    }

    protected final boolean isInApplicationLifetime() {
        return lifetimeRoot() == applicationRoot();
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

    protected final <H extends NamespaceHandle<E, ?>> H namespaceLazy(NamespaceTemplate<H> template, String name) {
        return handle.namespaceLazy(template, name);
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
        throw new InternalExtensionException("This method must be overridden by " + extension.extensionType);
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
    protected ExtensionPoint<E> newExtensionPoint(ExtensionUseSite usesite) {
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
    // Configured????
    protected void onClose() {
        // childCursor
        for (ExtensionSetup e = extension.treeFirstChild; e != null; e = e.treeNextSibling) {
            e.instance().onClose();
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
    // Maybe we also have an onAssemblyClosed() <--- where you cannot install any more extensions
    protected void onConfigured() {
        ExtensionSetup s = extension;
        for (ExtensionSetup c = s.treeFirstChild; c != null; c = c.treeNextSibling) {
            if (c.container.assembly == s.container.assembly) {
                c.instance().onConfigured();
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

    /**
     * {@return the parent of this extension if present. Or empty if the extension is in the root container of the
     * application.}
     */
    protected final Optional<E> parent() {
        return handle.parent();
    }

    protected final <T> ProvidableBeanConfiguration<T> provide(Class<T> implementation) {
        ProvidableBeanConfiguration<T> sbc = base().install(implementation);
        return sbc.provide();
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
    protected final void runOnCodegen(Runnable action) {
        handle.runOnCodegen(action);
    }

    /**
     * Returns a selection of all wirelets of the specified type.
     * <p>
     * If this extension defines any runtime wirelet. A check must also be made at runtime, you must remember to check if
     * there are any unprocessed wirelets at runtime. As this may happen when creating an image
     *
     * @param <T>
     *            the type of wirelets to select
     * @param wireletClass
     *            the type of wirelets to select
     * @return a selection of all wirelets of the specified type
     * @throws IllegalArgumentException
     *             if the specified class is not located in the same module as the extension itself. Or if the specified
     *             wirelet class is not a proper subclass of ExtensionWirelet.
     */
    // Think ditch processed. We can call this on repeat
    // Add it must have Wireletphase = Build
    // A wirelet must be selected at least once.

    // Maybe have a forEach method as well? forEachWirelet(Class, Consumer);

    protected final <T extends Wirelet> Optional<T> selectWirelet(Class<T> wireletClass) {
        return selectWirelets(wireletClass).last();
    }

    protected final <T extends Wirelet> WireletSelection<T> selectWirelets(Class<T> wireletClass) {
        // Check that we are a proper subclass of ExtensionWirelet
        ClassUtil.checkProperSubclass(ExtensionWirelet.class, wireletClass, "wireletClass");

        // We only allow selection of wirelets in the same module as the extension itself
        // Otherwise people could do wirelets(ServiceWirelet.provide(..).getClass())...
        // Would probably be test the wirelet that defines <E> Then people could have an abstract shared wirelet.
        if (getClass().getModule() != wireletClass.getModule()) {
            throw new IllegalArgumentException("The specified wirelet class is not in the same module (" + getClass().getModule().getName() + ") as '"
                    + /* simple extension name */ extension.model.name() + ", wireletClass.getModule() = " + wireletClass.getModule());
        }
        // At runtime we have already checked that T is in the same module as the extension when building the application

        return extension.container.selectWireletsUnsafe(wireletClass);
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
     * @see ExtensionHandle#use(Class)
     */
    protected final <P extends ExtensionPoint<?>> P use(Class<P> extensionPointClass) {
        return handle.use(extensionPointClass);
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

    // For extension properties, they are not validated before the extension is used together with extension defining the
    // property (potentially never)
    @Repeatable(ExtensionProperty.All.class)
    public @interface ExtensionProperty {

        // FFF#dsdf <- ExtensionName#PropertyName

        // Properties without # are framework properties

        /** {@return the name of the property} */
        String name();

        /** {@return the value of the property} */
        String value();

        /** An annotation that allows for placing multiple {@link ExtensionProperty} annotations on a single assembly. */
        @Retention(RetentionPolicy.RUNTIME)
        @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
        @Inherited
        @Documented
        @interface All {

            /** An array of property declarations. */
            ExtensionProperty[] value();
        }
    }
}

@ExtensionProperty(name = BaseModuleNames.CONFIG_EXTENSION_PROPERTY_DEFAULT_NAME, value = "web")
class PropUsage {}
