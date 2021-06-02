package app.packed.extension;

public @interface ExtensionService {
    Class<? extends Extension> extension();
    Class<?> implementation();
}
