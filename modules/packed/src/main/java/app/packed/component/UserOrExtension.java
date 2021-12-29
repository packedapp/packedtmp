package app.packed.component;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;

import app.packed.base.Nullable;
import app.packed.extension.Extension;

// Registrant
// Operator, Contractor
// ComponentOwner

// Hvem ejer en component
// Hvem provider en injectable value


// Er ikke paa det er en Realm... anyway
// En AssemblyRealm

public /* primitive */ final class UserOrExtension {

    // Application???? As in the application owns it, whoever that application is.
    private static final UserOrExtension USER = new UserOrExtension(null);

    @Nullable
    private final Class<? extends Extension<?>> extension;

    private UserOrExtension(Class<? extends Extension<?>> extension) {
        this.extension = extension;
    }

    public Class<? extends Extension<?>> extension() {
        if (extension == null) {
            throw new NoSuchElementException("No extension present");
        }
        return extension;
    }

    public boolean isUser() {
        return this == USER;
    }

    public boolean isExtension() {
        return this != USER;
    }

    public static UserOrExtension user() {
        return USER;
    }

    public static UserOrExtension extension(Class<? extends Extension<?>> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return new UserOrExtension(extensionType);
    }
}
