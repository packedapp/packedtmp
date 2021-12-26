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
public /* primitive */ final class RealmMirror {

    // Application???? As in the application owns it, whoever that application is.
    private static final RealmMirror APPLICATION = new RealmMirror(null);

    @Nullable
    private final Class<? extends Extension> extension;

    private RealmMirror(Class<? extends Extension> extension) {
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

    public static RealmMirror application() {
        return APPLICATION;
    }

    public static RealmMirror extension(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return new RealmMirror(extensionType);
    }
}
