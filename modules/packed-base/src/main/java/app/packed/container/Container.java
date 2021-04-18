package app.packed.container;

import java.util.Collection;
import java.util.Optional;

import app.packed.application.Application;
import app.packed.base.NamespacePath;
import app.packed.component.Component;

/**
 * A container is cool
 */
public interface Container {

    /** {@return the application this container is a part of} */
    Application application();

    /** {@return an unmodifiable view of all of this container's children} */
    Collection<Container> children();

    /** {@return the root container component in the container} */
    Component component();

    /**
     * Returns the distance to the root container. The root container having depth 0.
     * 
     * @return the distance to the root container
     */
    int depth();

    /** {@return the name of the container} */
    String name();
    
    /** {@return the parent container of this container. Or empty if this container has no parent} */
    Optional<Container> parent();

    /** {@return the path of this container} */
    NamespacePath path();
}
