package app.packed.component;

import app.packed.container.ContainerConfiguration;
import app.packed.extension.ExtensionConfiguration;

public sealed interface ComposerConfiguration permits ContainerConfiguration,ExtensionConfiguration {

}
