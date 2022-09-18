package app.packed.container;

import java.util.List;
import java.util.Set;

import app.packed.bean.InstanceBeanConfiguration;
import app.packed.operation.invokesandbox.OperationHandle;
import internal.app.packed.container.PackedContainerHandle;

// Maaske er det mere en Builder paa ContainerConfiguration

// Ligegyldigt hvad tror jeg ikke det skal bruges af end usere...
// Fx saadan noget med Lifetime og MethodHandle spawn


// stateless containers are not supported (obviously)
public sealed interface ContainerHandle permits PackedContainerHandle {

    /**
     * Returns an immutable set containing any extensions that are disabled for containers created by this driver.
     * <p>
     * When hosting an application, we must merge the parents unsupported extensions and the new guests applications drivers
     * unsupported extensions
     * 
     * @return a set of disabled extensions
     */
    public abstract Set<Class<? extends Extension<?>>> bannedExtensions();
    
    default List<OperationHandle> lifetimeOperations() {
        return List.of();
    }
    
    interface Installer {

        Installer allowRuntimeWirelets();
        
        ContainerHandle install();
        
        // Only Managed-Operation does not require a wrapper
        default void wrapIn(InstanceBeanConfiguration<?> wrapperBeanConfiguration) {
            // Gaar udfra vi maa definere wrapper beanen alene...Eller som minimum supportere det
            // Hvis vi vil dele den...
            
            // Det betyder ogsaa vi skal lave en wrapper bean alene
        }
    }
}

// Maaske man ikke kan lave den til en configuration hvis man bruger dissexxxx

// MethodHandle instead????
//// Jeg vil gerne lave en hel ny container for denne operation...
//// a.la. in a trie. Don't care if it is 
// container bean/functional bean/new bean/new container
// Alt har samme signatur
//public default ExtensionLauncher<Void> voidLauncher(Wirelet... wirelets) {
//    throw new UnsupportedOperationException();
//}
//
//public default ExtensionLauncher<AsyncApp> asyncApp(Wirelet... wirelets) {
//    throw new UnsupportedOperationException();
//}
