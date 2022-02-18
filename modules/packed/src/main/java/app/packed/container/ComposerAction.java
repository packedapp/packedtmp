package app.packed.container;

import app.packed.component.ComponentRealm;

/**
 *
 */
@FunctionalInterface
public non-sealed interface ComposerAction<C extends Composer> extends ComponentRealm {

    /**
     * Configures the given composer.
     *
     * @param composer
     *            the composer that is to be configured
     */
    void build(C composer);
}
