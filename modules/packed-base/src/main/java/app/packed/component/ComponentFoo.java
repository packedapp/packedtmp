package app.packed.component;

import java.util.function.Consumer;

import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerMirror;

@interface ComponentFoo {

    // Run before anything else
    Class<? extends Consumer<ContainerConfiguration>> preContainer();

    // Run after anything else
    Class<? extends Consumer<ContainerConfiguration>> postContainer();

    Class<? extends Consumer<ContainerMirror>> validator();
}

// Den skal jo saadan set fange
// Det er jo ikke rigtig en listener
interface ContainerConfigurationFooListener {
    default void preContainer(ContainerConfiguration cc) {}
    default void postContainer(ContainerConfiguration cc) {}
}