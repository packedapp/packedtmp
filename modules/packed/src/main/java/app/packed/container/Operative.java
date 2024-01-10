package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;

import app.packed.extension.Extension;
import internal.app.packed.util.types.ClassUtil;

// Cooperator
// Contributor
// Contractor
// Operator
// Operative
// Peer
// Participant

////// Nope
// Actor -> A competing computing concept
// Agent -> A competing computing concept
// Author -> Same as owner
// Owner -> A component has multiple owners???
// Partner -> Just doesn't sound right
// Player -> Cute, but this is not a game
// Registrant -> Something that has registered the component.
// User -> Not as long as we have framework users (end-users)

// ComponentX? Maybe it is time to introduct a component package again
/**
 * An operative represents either the user of the framework or a specific extension.
 *
 */
public /* value */ final class Operative {

    /** An application author. */
    private static final Operative APPLICATION = new Operative(Extension.class);

    /** Interned realm. */
    // Until we get values in which case it is always interned
    static final ClassValue<Operative> INTERNED = new ClassValue<Operative>() {

        @Override
        protected Operative computeValue(Class<?> extensionClass) {
            ClassUtil.checkProperSubclass(Extension.class, extensionClass, "extensionClass");
            return new Operative(extensionClass);
        }
    };

    /** The extension the realms represents. Or {@code app.packed.extension.Extension} for the application realm. */
    @SuppressWarnings("rawtypes")
    private final Class extensionClass;

    private Operative(@SuppressWarnings("rawtypes") Class extensionClass) {
        this.extensionClass = requireNonNull(extensionClass);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Extension<?>> extension() {
        if (extensionClass == Extension.class) {
            throw new NoSuchElementException("Cannot call this method on the user operative");
        }
        return extensionClass;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return extensionClass.hashCode();
    }

    public boolean isExtension() {
        return extensionClass != Extension.class;
    }

    public boolean isExtension(Class<? extends Extension<?>> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return extensionClass == extensionType;
    }

    /** {@return true if this author represents author or the application, otherwise false.} */
    public boolean isUser() {
        return extensionClass == Extension.class;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return isUser() ? "User" : extensionClass.getSimpleName();
    }

    /**
     * Returns a author representing the specified extension.
     *
     * @param extensionClass
     *            the type of extension to return a realm for
     * @return a realm for the specified extension
     * @throws IllegalArgumentException
     *             if the specified class is not a proper subclass of Extension
     */
    public static Operative extension(Class<? extends Extension<?>> extensionClass) {
        // ExtensionModel.get().operative???
        return INTERNED.get(extensionClass);
    }

    /** {@return the application author.} */
    public static Operative user() {
        return APPLICATION;
    }
}