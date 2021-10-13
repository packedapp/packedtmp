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
public /* primitive */ final class Realm {

    // Application???? As in the application owns it, whoever that application is.
    private static final Realm APPLICATION = new Realm(null);

    @Nullable
    private final Class<? extends Extension> extension;

    private Realm(Class<? extends Extension> extension) {
        this.extension = extension;
    }

    public Class<? extends Extension> extension() {
        if (extension == null) {
            throw new NoSuchElementException("No extension present");
        }
        return extension;
    }

    public boolean isApplication() {
        return this == APPLICATION;
    }

    public boolean isExtension() {
        return this != APPLICATION;
    }

    public static Realm application() {
        return APPLICATION;
    }

    public static Realm extension(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return new Realm(extensionType);
    }
}
