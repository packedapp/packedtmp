package app.packed.application;

import app.packed.component.Component;

public interface Application {

    /** {@return the root component in the application}. */
    Component component();

    /** {@return the root component in the application}. */
    @SuppressWarnings("exports")
    Container container();
    
    /** {@return the name of the application} */
    String name();
    
    // Wired er parent component<->child component
    // connections er component til any component.
    boolean isStronglyWired();
}

interface Container {
    
}