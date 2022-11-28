package app.packed.container;

import java.util.NoSuchElementException;

import app.packed.framework.Nullable;
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

public /* primitive */ final class Realm {

    /** The application realm. */
    private static final Realm APPLICATION = new Realm(Extension.class);

    /** The extension the realms represents. Or {@code app.packed.extension.Extension} for the application realm. */
    @SuppressWarnings("rawtypes")
    @Nullable
    private final Class extensionClass;

    private Realm(@SuppressWarnings("rawtypes") Class extensionClass) {
        this.extensionClass = extensionClass;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Extension<?>> extension() {
        if (extensionClass == Extension.class) {
            throw new NoSuchElementException("Cannot call this method on the application realm");
        }
        return extensionClass;
    }

    /** {@return true if this realm represents the application realm, otherwise false.} */
    public boolean isApplication() {
        return extensionClass == Extension.class;
    }

    public boolean isExtension() {
        return extensionClass != Extension.class;
    }

    /** {@inheritDoc} */
    public String toString() {
        return isApplication() ? "Application" : extensionClass.getSimpleName();
    }

    /** {@return the application realm.} */
    public static Realm application() {
        return APPLICATION;
    }

    /**
     * Returns a realm for the specified extension.
     * 
     * @param extensionClass
     *            the type of extension to return a realm for
     * @return a realm for the specified extension
     * @throws IllegalArgumentException
     *             if the specified class is not a proper subclass of Extension
     */
    public static Realm extension(Class<? extends Extension<?>> extensionClass) {
        ClassUtil.checkProperSubclass(Extension.class, extensionClass, "extensionClass");
        return new Realm(extensionClass);
    }
}
