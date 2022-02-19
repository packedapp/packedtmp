package app.packed.component;

import java.util.NoSuchElementException;

import app.packed.extension.Extension;
import packed.internal.util.ClassUtil;

// Registrant
// Operator, Contractor
// ComponentOwner

// Hvem ejer en component
// Hvem provider en injectable value

// Er ikke paa det er en Realm... anyway
// En AssemblyRealm

public /* primitive */ final class UserOrExtension {

    // Application???? As in the application owns it, whoever that application is.
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final UserOrExtension USER = new UserOrExtension((Class) Extension.class);

    @SuppressWarnings( "rawtypes")
    private final Class extension;

    private UserOrExtension(Class<? extends Extension<?>> extension) {
        this.extension = extension;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends Extension<?>> extension() {
        if (extension == Extension.class) {
            throw new NoSuchElementException("No extension present");
        }
        return extension;
    }

    public boolean isExtension() {
        return extension != Extension.class;
    }

    public boolean isUser() {
        return extension == Extension.class;
    }

    public static UserOrExtension extension(Class<? extends Extension<?>> extensionType) {
        ClassUtil.checkProperSubclass(Extension.class, extensionType);
        return new UserOrExtension(extensionType);
    }

    public static UserOrExtension user() {
        return USER;
    }
}
