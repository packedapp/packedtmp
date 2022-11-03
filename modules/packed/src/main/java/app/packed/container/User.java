package app.packed.container;

import java.util.NoSuchElementException;

import internal.app.packed.util.ClassUtil;

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

public /* primitive */ final class User {

    /** The application realm. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final User APPLICATION = new User((Class) Extension.class);

    /** The extension the realms represents. Or {@code app.packed.extension.Extension} for the application realm. */
    @SuppressWarnings("rawtypes")
    private final Class extension;

    private User(Class<? extends Extension<?>> extension) {
        this.extension = extension;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Extension<?>> extension() {
        if (extension == Extension.class) {
            throw new NoSuchElementException("Cannot call this method on the application realm");
        }
        return extension;
    }

    /** {@return true if this realm represents the application realm, otherwise false.} */
    public boolean isApplication() {
        return extension == Extension.class;
    }

    public boolean isExtension() {
        return extension != Extension.class;
    }

    /** {@inheritDoc} */
    public String toString() {
        return isApplication() ? "Application" : extension.getSimpleName();
    }

    /** {@return the application realm.} */
    public static User application() {
        return APPLICATION;
    }

    /**
     * Returns a realm for the specified extension.
     * 
     * @param extensionType
     *            the type of extension to return a realm for
     * @return a realm for the specified extension
     * @throws IllegalArgumentException
     *             if the specified class is not a proper subclass of Extension
     */
    public static User extension(Class<? extends Extension<?>> extensionType) {
        ClassUtil.checkProperSubclass(Extension.class, extensionType, "extensionType");
        return new User(extensionType);
    }
}
