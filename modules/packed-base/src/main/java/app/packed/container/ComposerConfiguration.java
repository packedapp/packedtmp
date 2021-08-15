package app.packed.container;

import app.packed.extension.ExtensionConfiguration;

public sealed interface ComposerConfiguration permits ContainerConfiguration,ExtensionConfiguration {}
