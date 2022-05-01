package app.packed.container;

import app.packed.application.entrypoint.EntryPointExtensionPoint;
import app.packed.base.Nullable;
import app.packed.component.Realm;
import app.packed.container.Extension.DependsOn;
import packed.internal.container.PackedExtensionPointContext;

/**
 * Extension points are the main mechanism by which an extension can use other extensions. Developers that are not
 * creating their own extensions will likely never have to deal with these type of classes.
 * <p>
 * An extension point instance is acquired by calling {@link Extension#use(Class)}. Whereby Packed will create a new
 * instance using constructor injection. These instances are <strong>never</strong> cached but created every time they
 * are requested.
 * <p>
 * Any extension that requests an extension point by calling {@link Extension#use(Class)}. Must define the extension
 * that the extension point is a part of as a direct dependency using {@link DependsOn}. Failure to do so will result in
 * an exception being thrown when calling {@link Extension#use(Class)}.
 * <p>
 * An extension point class contains no lifecycle methods similar to those on Extension. Instead extension points are
 * merely thought of as thin wrappers on an extension. Where every invocation on an extension point delegates to
 * package-private methods on the {@link ExtensionPoint#extension()} itself.
 * <p>
 * Attempting to use any of the methods on this class from the constructor of a subclass, will result in an
 * {@link IllegalStateException} being thrown.
 * <p>
 * If an extension defines classes that are only usable by other extension and not end-users. They should be declared as
 * nested classes on an extension point. See, for example, {@link EntryPointExtensionPoint}.
 * 
 * @see Extension#use(Class)
 * @see UseSite
 * 
 * @param <E>
 *            The type of extension this extension point is a part of. The extension point must be located in the
 *            same module as the extension itself.
 * 
 * @apiNote The main reason that other extension is two fold. Hide methods that end-users should not use. Allow the
 *          application developer to be in full control.
 */
public abstract class ExtensionPoint<E extends Extension<E>> {

    /**
     * A context for this extension point. Is initialized after the extension point has been constructor injected. This
     * field must be accessed via {@link #context()}.
     */
    @Nullable
    private PackedExtensionPointContext context;

    /**
     * Create a new extension point.
     * <p>
     * Subclasses should have a single constructor with package access.
     */
    protected ExtensionPoint() {}

    /**
     * Checks that the extension that uses this extension point is still configurable.
     * 
     * @see Extension#checkConfigurable()
     * @throws IllegalStateException
     *             if the extension that uses this extension point is no longer configurable.
     */
    protected final void checkConfigurable() {
        context().extension().extensionRealm.checkOpen();
    }

    /** {@return the context for this extension point.} */
    private final PackedExtensionPointContext context() {
        PackedExtensionPointContext c = context;
        if (c == null) {
            throw new IllegalStateException("This operation cannot be invoked from the constructor of an extension point.");
        }
        return c;
    }

    /** {@return the extension this extension point is a part of.} */
    @SuppressWarnings("unchecked")
    protected final E extension() {
        return (E) context().extension().instance();
    }

    /** {@return the type of extension that uses the extension point.} */
    protected final Class<? extends Extension<?>> usedBy() {
        return context().usedBy().extensionType;
    }

    protected final UseSite useSite() {
        return context();
    }

    /**
     * A context object that can be injected into subclasses of {@link ExtensionPoint}.
     */
    // Inner class: UseSite
    //// Er lidt underlig maaske med UseSite hvis man tager den som parameter
    //// Men vil ikke mere hvor man skal tage et ExtensionPointContext???
    public sealed interface UseSite permits PackedExtensionPointContext {
        Realm realm();
    }
}
// TODO
// UseSite er lidt underlig. Ideen er jo at den fungere som en slags context
// Skal vi have noget med realm udover usedBy?
