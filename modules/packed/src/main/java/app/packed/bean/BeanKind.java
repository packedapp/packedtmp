package app.packed.bean;

import internal.app.packed.lifetime.PackedBeanTemplate;
import sandbox.extension.bean.BeanTemplate;

/**
 * This enum details the various kinds of beans that are supported in Packed.
 */
// All to do with lifetime.... BeanLifetimeKind

// Maybe ditching scope is bad
// https://marcelkliemannel.com/articles/2021/overview-of-bean-scopes-in-quarkus/

//Maybe we mixing different things... A bean can be container managed or container-unmanaged
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
     * A foreign bean is a bean whose lifecycle is managed outside of the container in which it is registered And where the
     * instance must be provided (typically by the end-user) for every operation.
     * <p>
     * A foerign bean is always presented on every operation. The instance is never stored any where.
     * <p>
     * A foerign bean is always unmanaged (Or static/stateless meaning we don't support shit)
     */
    /**
     * The lifetime of the bean is not managed by any extension. At least not in a standard way
     * <p>
     * {@link #operations()} always returns a empty list
     * <p>
     * All operations on the bean must take a bean instance.
     * <p>
     * Giver det mening overhoved at supportere operation
     * <p>
     * It is a failure to use lifecycle annotations on the bean
     **/
    // Cannot be exposed as a service
    // Other name:Per_Operattion <- Bean must be provided for each operation
    // Validationi9
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

    MANANGED,

    /**
     * A static bean is a bean with no runtime instances.
     * <p>
     * Since static beans are stateless, they have no lifecycle as lifecycles are always bound to a bean instance.
     * <p>
     * Trying to use lifecycle annotations such as {@link Inject} or {@link OnStart} will fail with a
     * {@link BeanInstallationException}.
     * <p>
     * Constructors are never scanned when creating static beans, and therefore never validated for correctness.
     * <p>
     * Static beans are always bound to the lifetime of their container. Meaning that they are only usable as long as their
     * container are active. If a container has been stopped the bean should no longer be used.
     *
     * @see BaseExtension#installStatic(Class)
     **/
    STATIC,

    /**
     * A unmanaged bean is a bean that is create within the context of a container. But once created the container no longer
     * keeps track of the bean. As a consequence unmanaged beans does not support destructive lifecycle operations.
     * <p>
     * A typical example is prototype services
     */
    UNMANAGED;

    // Operational <- A bean that is spawned (an instance created) for the sole duration of an operation
    // After which is will be destroyed
    // If only configured on the bean itself I don't know how much sense it makes?

    // Operational = Managed?? Altsaa hvis vi er vi er BeanLifetimeKind giver den vel ikke super mening
    // Men er det en BeanKind saa er det vel ok.

    public boolean hasBeanLifetime() {
        return !hasContainerLifetime();
    }

    public boolean hasContainerLifetime() {
        return this == STATIC || this == CONTAINER;
    }

    /** @return whether or not the bean can have more than 1 instance. */
    public boolean isMultiInstance() {
        return this == MANANGED || this == UNMANAGED || this == FOREIGN;
    }

    /**
     * {@return a bean template that can be used by extensions to install new beans}
     * <p>
     * This method is only relevant for extension developers.
     *
     * @return
     */
    public BeanTemplate template() {
        return new PackedBeanTemplate(this);
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
