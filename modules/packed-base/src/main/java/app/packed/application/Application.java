package app.packed.application;

import app.packed.component.Component;
import app.packed.container.Container;

/**
 * Represents an application
 */
public interface Application {

    /** {@return the root component in the application}. */
    Component component();

    /** {@return the root component in the application}. */
    Container container();
    
    /** {@return the name of the application} */
    String name();
    
    // Wired er parent component<->child component
    // connections er component til any component.
    boolean isStronglyWired();
}
