package app.packed.container;

import java.util.Set;

import app.packed.application.AsyncApp;
import app.packed.container.sandbox.ExtensionLauncher;
import packed.internal.container.PackedContainerDriver;

// Maaske er det mere en Builder paa ContainerConfiguration

// Ligegyldigt hvad tror jeg ikke det skal bruges af end usere...
// Fx saadan noget med Lifetime og MethodHandle spawn

public sealed interface ContainerHandle permits PackedContainerDriver {

    /**
     * Returns an immutable set containing any extensions that are disabled for containers created by this driver.
     * <p>
     * When hosting an application, we must merge the parents unsupported extensions and the new guests applications drivers
     * unsupported extensions
     * 
     * @return a set of disabled extensions
     */
    public abstract Set<Class<? extends Extension<?>>> bannedExtensions();
    
    // Maaske man ikke kan lave den til en configuration hvis man bruger dissexxxx
    
    // MethodHandle instead????
    //// Jeg vil gerne lave en hel ny container for denne operation...
    //// a.la. in a trie. Don't care if it is 
    // container bean/functional bean/new bean/new container
    // Alt har samme signatur
    public default ExtensionLauncher<Void> voidLauncher(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
    
    public default ExtensionLauncher<AsyncApp> asyncApp(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}
