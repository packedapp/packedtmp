package app.packed.component;

import java.util.NoSuchElementException;

import app.packed.container.Extension;
import packed.internal.util.ClassUtil;

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

public /* primitive */ final class Realm {

    /** The application realm. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final Realm APPLICATION = new Realm((Class) Extension.class);

    /** The extension the realms represents. Or {@code app.packed.extension.Extension} for the application realm. */
    @SuppressWarnings("rawtypes")
    private final Class extension;

    private Realm(Class<? extends Extension<?>> extension) {
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

    /** {@inheritDoc} */
    public String toString() {
        return isApplication() ? "Application" : extension.getSimpleName();
    }

    public boolean isExtension() {
        return extension != Extension.class;
    }

    /** {@return the application realm.} */
    public static Realm application() {
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
    public static Realm extension(Class<? extends Extension<?>> extensionType) {
        ClassUtil.checkProperSubclass(Extension.class, extensionType);
        return new Realm(extensionType);
    }
}
