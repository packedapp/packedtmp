package app.packed.bean;

/**
 * This enum details the various kinds of beans that are supported in Packed.
 */
public enum BeanKind {

    /**
     * A container bean is a stateful bean with a single instance in the container in which it is registered
     * <p>
     * Lives and dies with the container it is installed into. Is eagerly created. Only a single bean of the specified type
     * may exists in the container. Think we need to check other bean types as well.
     * <p>
     * non-void
     *
     */
    CONTAINER,
//
//    /**
//     * A functional bean is a stateless (cannot be instantiated) bean that is that defined with a {@code void} bean class.
//     * It is typically used by extensions to group 1 or more functions that have been configured by users.
//     * <p>
//     * Functional beans are always bound to the lifetime of the container in which they are registered.
//     * <p>
//     * While not required, functional beans are typically named starting with lowercase 'f' followed by a single word with
//     * an uppercase first character.
//     * <p>
//     * Is is currently not possible to create these explicit by end-users.
//     */
//    FUNCTIONAL,

    /**
     * A lazy bean is a special type of container bean that is lazily created if needed.
     * <p>
     * While it may seem like. Lazy beans come with some overhead both memory and performance as there is some machinery
     * that needs to be stet and checks that needs to be performed every time it is accessed.
     */
    LAZY,

    MANYTON,

    /**
     * A static bean is stateless (is never instantiated) bean with a custom bean class.
     * <p>
     * Since static beans are stateless, they have no lifecycle as this is always bound a bean instance. Trying to use
     * lifecycle annotations such as {@link Inject} or {@link OnStart} will fail with
     * <p>
     * Functional beans are always bound to the lifetime of the container in which they are registered.
     *
     * @see BaseExtension#installStatic(Class)
     **/
    STATIC;

    // Maybe have Managed and Unmanaged anyways
    // Managed as in maybe just partial managed

    // Operational <- A bean that is spawned (an instance created) for the sole duration of an operation
    // After which is will be destroyed
    // If only configured on the bean itself I don't know how much sense it makes?

    public boolean hasBeanLifetime() {
        return !hasContainerLifetime();
    }

    public boolean hasContainerLifetime() {
        return this == STATIC || this == CONTAINER;
    }
//
//    /** @return whether or not the bean will have 1 or more instance. */
//    public boolean hasInstances() {
//        return this != FUNCTIONAL && this != STATIC;
//    }

    /** @return whether or not the bean will have 1 or more instance. */
    public boolean hasSingleInstance() {
        return this == CONTAINER || this == LAZY;
    }
}
