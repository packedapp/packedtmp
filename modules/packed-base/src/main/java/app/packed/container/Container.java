package app.packed.container;

import app.packed.application.Application;

public interface Container {
    
    /** {@return the application this container is a part of} */
    Application application();
    
    /** {@return the name of the container} */
    String name();
}
