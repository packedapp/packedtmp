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
}

interface Container {
    
}