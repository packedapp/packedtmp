package app.packed.application;

import java.util.Optional;

import app.packed.component.Component;
import app.packed.container.Container;

/**
 * Represents an application
 */
public interface Application {

    /** {@return the root component in the application}. */
    Component component();

    /** {@return the root container in the application}. */
    Container container();

    // Wired er parent component<->child component
    // connections er component til any component.
    /**
     * Returns whether or not this application is strongly wired to a parent application.
     * <p>
     * An application with no parent will always return false. 
     * 
     * @return
     */
    boolean isStronglyWired();

    
    /** {@return the name of the application} */
    String name();

    /** {@return the parent application of this application. Or empty if this component has no parent} */
    default Optional<Application> parent() {
        return Optional.empty();
    }
}
