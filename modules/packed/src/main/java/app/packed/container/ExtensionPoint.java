package app.packed.container;

import static java.util.Objects.requireNonNull;

import app.packed.application.entrypoint.EntryPointExtensionPoint;
import app.packed.base.Nullable;
import app.packed.component.Realm;
import app.packed.container.Extension.DependsOn;
import packed.internal.container.ExtensionSetup;
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
 * NOTE: In order to properly implement an extension point you:
 * <ul>
 * <li>Must override {@link Extension#newExtensionPoint()} in order to provide a new instance of the extension point.</li>
 * <li>Must place the extension point in the same module as the extension itself (iff the extension is defined in a module).</li>
 * <li>Should name the extension point class {@code $NAME_OF_EXTENSION$}Point.</li>
 * </ul>
 * 
 * @see Extension#use(Class)
 * @see UseSite
 * 
 * @param <E>
 *            The type of extension this extension point is a part of.
 * 
 * @apiNote The reason that end-users uses {@code Extension} instances and extensions uses {@code ExtensionPoint}
 *          instances are twofold. It allows an extension to "hide" highly specialized methods that no end-users would
 *          ever need on the extension point class. Allow the application developer to be in full control.
 */
public abstract class ExtensionPoint<E extends Extension<E>> {

    /**
     * A context for this extension point. Is initialized after the extension point has been constructor injected.
     * <p>
     * This field must be accessed via {@link #context()}.
     */
    @Nullable
    private PackedExtensionPointContext context;

    /**
     * Create a new extension point.
     * <p>
     * Subclasses should have constructors with package-private access.
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

    /** {@return the extension instance that this extension point is extending.} */
    @SuppressWarnings("unchecked")
    protected final E extension() {
        return (E) context().extension().instance();
    }

    /** {@return the type of extension that are using the extension point.} */
    protected final Class<? extends Extension<?>> usedBy() {
        return context().usedBy().extensionType;
    }
    
    /**
     * Invoked by {@link packed.internal.container.ExtensionMirrorModel#initialize(ExtensionMirror, ExtensionSetup)} to set
     * the internal configuration of the extension.
     * 
     * @param extension
     *            the internal configuration of the extension to mirror
     */
    final void initialize(PackedExtensionPointContext context) {
        if (this.context != null) {
            throw new IllegalStateException("This extension point has already been initialized.");
        }
        this.context = requireNonNull(context);
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
