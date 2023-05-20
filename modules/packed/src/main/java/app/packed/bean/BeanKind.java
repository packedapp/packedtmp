package app.packed.bean;

import internal.app.packed.lifetime.PackedBeanTemplate;
import sandbox.extension.bean.BeanTemplate;

/**
 * This enum details the various kinds of beans that are supported in Packed.
 */
// All to do with lifetime.... BeanLifetimeKind
public enum BeanKind {

    /**
     * A container bean is a stateful bean with a single instance in the container in which it is registered
     * <p>
     * Lives and dies with the container it is installed into. Is eagerly created. Only a single bean of the specified type
     * may exists in the container. Think we need to check other bean types as well.
     * <p>
     * Represents a bean whose lifetime is that of its container. This means it will always be created and destroyed
     * together with its container.
     * <p>
     * A single instance of the bean will be created (if the instance was not already provided when installing the bean)
     * when the container is instantiated. Where after its lifecycle will follow that of the container.
     * <p>
     * non-void
     *
     */
    CONTAINER,

    /**
     * A foreign bean is a bean whose lifecycle is managed outside of the container in which it is registered.
     *
     */
    // Can we have more than 1?
    // Can they be removed? Obviously if we have more than one
    FOREIGN,

    /**
     * A lazy bean is a bean that is lazily created when first needed.
     * <p>
     * While it may seem like. Lazy beans come with some overhead both memory and performance as there is some machinery
     * that needs to be stet and checks that needs to be performed every time it is accessed.
     * <p>
     *
     * @see BeanInstaller#install(Class)
     * @see BeanInstaller#installIfAbsent(Class, Consumer)
     * @see BeanInstaller#install(Op)
     */
    LAZY,

    /**
     * A unmanaged bean is a bean that is create within the context of a container. But once created the container no longer
     * keeps track of the bean. As a consequence unmanaged beans does not destructive lifecycle operations.
     * <p>
     * A typical example is prototype services
     */
    UNMANAGED,

    MANANGED,

    /**
     * A static bean is a bean with no runtime instances.
     * <p>
     * Since static beans are stateless, they have no lifecycle as this is always bound a bean instance. Trying to use
     * lifecycle annotations such as {@link Inject} or {@link OnStart} will fail with a {@link BeanInstallationException}.
     * <p>
     * Static beans are always bound to the lifetime of their container.
     *
     * @see BaseExtension#installStatic(Class)
     **/
    STATIC;

    // Operational <- A bean that is spawned (an instance created) for the sole duration of an operation
    // After which is will be destroyed
    // If only configured on the bean itself I don't know how much sense it makes?

    // Operational = Managed?? Altsaa hvis vi er vi er BeanLifetimeKind giver den vel ikke super mening
    // Men er det en BeanKind saa er det vel ok.


    public BeanTemplate template() {
        return new PackedBeanTemplate(this);
    }

    public boolean hasBeanLifetime() {
        return !hasContainerLifetime();
    }

    public boolean hasContainerLifetime() {
        return this == STATIC || this == CONTAINER;
    }

    /** @return whether or not the bean can have more than 1 instance. */
    public boolean isMultiInstance() {
        return this == MANANGED || this == UNMANAGED;
    }
}
//
///** @return whether or not the bean will have 1 or more instance. */
//public boolean hasInstances() {
//  return this != FUNCTIONAL && this != STATIC;
//}
//
///**
//* A functional bean is a stateless (cannot be instantiated) bean that is that defined with a {@code void} bean class.
//* It is typically used by extensions to group 1 or more functions that have been configured by users.
//* <p>
//* Functional beans are always bound to the lifetime of the container in which they are registered.
//* <p>
//* While not required, functional beans are typically named starting with lowercase 'f' followed by a single word with
//* an uppercase first character.
//* <p>
//* Is is currently not possible to create these explicit by end-users.
//*/
//FUNCTIONAL,
