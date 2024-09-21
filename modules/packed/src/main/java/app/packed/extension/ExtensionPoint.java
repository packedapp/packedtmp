package app.packed.extension;

import static java.util.Objects.requireNonNull;

import app.packed.build.BuildAuthority;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.extension.PackedExtensionUseSite;

/**
 * Extension points are the main mechanism by which an extension can use another extension. Developers that do not
 * create their own extensions will likely never have to deal with these type of classes.
 * <p>
 * An extension point instance is acquired by calling {@link Extension#use(Class)}. Whereby the framework will create a
 * new instance (using constructor injection). Extension point instances are <strong>never</strong> cached, instead they
 * are instantiated every time they are requested.
 * <p>
 * An extension that requests a specific extension point must define the extension that the extension point is a part of
 * as a direct dependency using the {@link DependsOn} annotation. Failure to do so will result in an
 * {@link InternalExtensionException} being thrown when calling {@link Extension#use(Class)} or installing a bean that
 * belongs to the extension.
 * <p>
 * An extension point class contains no overridable life-cycle methods similar to those on Extension. Instead extension
 * points are just thin wrappers on top of an extension. Where every invocation on the extension point delegates to
 * package-private methods on the {@link ExtensionPoint#extension()} itself.
 * <p>
 * If an extension defines classes that are only used by other extensions and not directly by application developers.
 * They should be declared as nested classes on an extension point. See, for example, {@link EntryPointExtensionPoint}.
 *
 * NOTE: In order to properly implement an extension point you:
 * <ul>
 * <li>Must override {@link Extension#newExtensionPoint()} in order to provide a new instance of the extension
 * point.</li>
 * <li>Must place the extension point in the same module as the extension itself (iff the extension is defined in a
 * module).</li>
 * <li>Should name the extension point class {@code $NAME_OF_EXTENSION$}Point.</li>
 * </ul>
 * <p>
 * Attempting to use any of the methods on this class from the constructor of a subclass, will result in an
 * {@link IllegalStateException} being thrown.
 *
 * @see Extension#use(Class)
 * @see UseSite
 *
 * @param <E>
 *            The type of extension this extension point is a part of.
 */
public abstract class ExtensionPoint<E extends Extension<E>> {

    /**
     * A context for this extension point. Is initialized via {@link #initialize(PackedExtensionPointContext)}..
     * <p>
     * This field should only be read via {@link #contextUse()}.
     */
    private final PackedExtensionUseSite usesite;

    /**
     * Create a new extension point.
     * <p>
     * Subclasses should never exposed any public constructors.
     */
    protected ExtensionPoint(ExtensionUseSite usesite) {
        this.usesite = (internal.app.packed.extension.PackedExtensionUseSite) requireNonNull(usesite);
    }

    /**
     * Checks that the extension that uses this extension point is still configurable.
     * <p>
     * Subclasses typically want to call this method before any mutating operation
     *
     * @see Extension#checkIsConfigurable()
     * @throws IllegalStateException
     *             if the extension that uses this extension point is no longer configurable.
     */
    protected final void checkIsConfigurable() {
        // We only check the extension that uses the extension point.
        // Because the extension the extension points belongs to, is always a direct
        // dependency and will be closed before the extension that provides the extension point.
        ExtensionSetup extension = contextUse().usedBy();
        if (!extension.isConfigurable()) {
            throw new IllegalStateException(extension.extensionType + " is no longer configurable");
        }
    }

    protected final ExtensionUseSite context() {
        return contextUse();
    }

    /** {@return the context for this extension point.} */
    final PackedExtensionUseSite contextUse() {
        PackedExtensionUseSite c = usesite;
        if (c == null) {
            throw new IllegalStateException("This operation cannot be invoked from the constructor of an extension point.");
        }
        return c;
    }

    /** {@return the extension instance that this extension point is a part of} */
    @SuppressWarnings("unchecked")
    protected final E extension() {
        return (E) extensionSetup().instance();
    }

    final ExtensionSetup extensionSetup() {
        return contextUse().extension();
    }
//
//    /**
//     * Invoked by {@link packed.internal.container.ExtensionMirrorModel#initialize(ExtensionMirror, ExtensionSetup)} to set
//     * the context of this extension point.
//     *
//     * @param context
//     *            the context of this extension point
//     */
//    final void initialize(ExtensionSetup extension, ExtensionSetup usedBy) {
//        if (this.context != null) {
//            throw new IllegalStateException("This extension point has already been initialized.");
//        }
//        this.context = new PackedExtensionPointContext(extension, usedBy);
//    }

    /** {@return the type of extension that are using the extension point.} */
    protected final Class<? extends Extension<?>> usedBy() {
        return contextUse().usedBy().extensionType;
    }

    /**
     * A context object that can be injected into subclasses of {@link ExtensionPoint}.
     */
    // Inner class: UseSite
    //// Er lidt underlig maaske med UseSite hvis man tager den som parameter
    //// Men vil ikke mere hvor man skal tage et ExtensionPointContext???
    public sealed interface ExtensionUseSite permits PackedExtensionUseSite {
        BuildAuthority author();
    }
}
