package app.packed.application;

import java.util.Optional;

import app.packed.component.Component;
import app.packed.container.Container;

/**
 * Represents an application.
 */
public interface Application {

    /** {@return the root component in the application}. */
    Component component();

    /** {@return the root container in the application}. */
    Container container();

    /**
     * Returns whether or the application is runnable. The value is always determined by
     * {@link ApplicationDriver#isRunnable()}.
     * 
     * @return whether or the application is runnable
     */
    boolean isRunnable();

    // Wired er parent component<->child component
    // connections er component til any component.
    /**
     * Returns whether or not this application is strongly wired to a parent application.
     * <p>
     * A root application will always return false.
     * 
     * @return {@code true} if this application is strongly wired to a parent application, otherwise {@code false}
     */
    boolean isStronglyWired();

    /** {@return the name of the application} */
    String name();

    /** {@return the parent application of this application. Or empty if this application has no parent} */
    Optional<Application> parent();
}
