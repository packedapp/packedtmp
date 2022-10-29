package app.packed.container;

import java.util.List;
import java.util.Set;

import app.packed.bean.InstanceBeanConfiguration;
import app.packed.operation.OperationHandle;
import internal.app.packed.container.ContainerSetup;

/**
 *
 */
// Must contain
public final class ContainerHandle {

    final ContainerSetup container;

    ContainerHandle(ContainerSetup container) {
        this.container = container;
    }

    /**
     * Returns an immutable set containing any extensions that are disabled for containers created by this driver.
     * <p>
     * When hosting an application, we must merge the parents unsupported extensions and the new guests applications drivers
     * unsupported extensions
     * 
     * @return a set of disabled extensions
     */
    public Set<Class<? extends Extension<?>>> bannedExtensions() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns whether or not the bean is still configurable.
     * 
     * @return {@code true} if the bean is still configurable
     */
    public boolean isConfigurable() {
        return !container.assembly.isClosed();
    }

    public List<OperationHandle> lifetimeOperations() {
        return List.of();
    }

    public interface InstallOption {

        static InstallOption allowRuntimeWirelets() {
            return null;
        }

        // Only Managed-Operation does not require a wrapper
        default void wrapIn(InstanceBeanConfiguration<?> wrapperBeanConfiguration) {
            // Gaar udfra vi maa definere wrapper beanen alene...Eller som minimum supportere det
            // Hvis vi vil dele den...

            // Det betyder ogsaa vi skal lave en wrapper bean alene
        }
    }
}
