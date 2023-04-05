package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;

import app.packed.extension.Extension;
import internal.app.packed.util.types.ClassUtil;

// Registrant
// Operator, Contractor
// ComponentOwner

// ComponentUser
// FooUser
//// Something that ends with user

// Hvem ejer en component
// Hvem provider en injectable value

// Er ikke paa det er en Realm... anyway
// En AssemblyRealm

/// XIdentity

// Player, Operator, Partner (maybe like Operator best)
public /* primitive */ final class Author {

    /** An application author. */
    private static final Author APPLICATION = new Author(Extension.class);

    /** Interned realm. */
    // Until we get primitives in which case it is always interned
    static final ClassValue<Author> INTERNED = new ClassValue<Author>() {

        @Override
        protected Author computeValue(Class<?> extensionClass) {
            ClassUtil.checkProperSubclass(Extension.class, extensionClass, "extensionClass");
            return new Author(extensionClass);
        }
    };

    /** The extension the realms represents. Or {@code app.packed.extension.Extension} for the application realm. */
    @SuppressWarnings("rawtypes")
    private final Class extensionClass;

    private Author(@SuppressWarnings("rawtypes") Class extensionClass) {
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
            throw new NoSuchElementException("Cannot call this method on an application author");
        }
        return extensionClass;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return extensionClass.hashCode();
    }

    /** {@return true if this author represents author or the application, otherwise false.} */
    public boolean isApplication() {
        return extensionClass == Extension.class;
    }

    public boolean isExtension() {
        return extensionClass != Extension.class;
    }

    public boolean isExtension(Class<? extends Extension<?>> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return extensionClass == extensionType;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return isApplication() ? "Application" : extensionClass.getSimpleName();
    }

    /** {@return the application author.} */
    public static Author application() {
        return APPLICATION;
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
    public static Author extension(Class<? extends Extension<?>> extensionClass) {
        return INTERNED.get(extensionClass);
    }
}
