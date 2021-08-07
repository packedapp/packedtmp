package app.packed.component;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;

import app.packed.base.Nullable;
import app.packed.extension.Extension;

// Registrant
// Operator, Contractor
// ComponentOwner
public /* primitive */ final class ComponentOwner {

    // Application???? As in the application owns it, whoever that application is.
    private static final ComponentOwner USER = new ComponentOwner(null);

    @Nullable
    private final Class<? extends Extension> extension;

    private ComponentOwner(Class<? extends Extension> extension) {
        this.extension = extension;
    }

    public Class<? extends Extension> extension() {
        if (extension == null) {
            throw new NoSuchElementException("No extension present");
        }
        return extension;
    }

    public boolean isExtension() {
        return this != USER;
    }

    public boolean isUser() {
        return this == USER;
    }

    public static ComponentOwner extension(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return new ComponentOwner(extensionType);
    }

    public static ComponentOwner user() {
        return USER;
    }
}
