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
public /* primitive */ final class Operator {

    // Application???? As in the application owns it, whoever that application is.
    private static final Operator APPLICATION = new Operator(null);

    @Nullable
    private final Class<? extends Extension> extension;

    private Operator(Class<? extends Extension> extension) {
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

    public static Operator application() {
        return APPLICATION;
    }

    public static Operator extension(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return new Operator(extensionType);
    }
}
