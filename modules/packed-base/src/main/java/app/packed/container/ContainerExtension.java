package app.packed.container;

import app.packed.extension.Extension;
import app.packed.extension.InternalExtensionException;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;

public class ContainerExtension extends Extension {
    
    /** The service manager. */
    final ContainerSetup container;

    final ExtensionSetup extension;

    /**
     * Create a new container extension.
     * 
     * @param setup
     *            an extension setup object (hidden).
     */
    /* package-private */ ContainerExtension(ExtensionSetup extension) {
        this.extension = extension;
        this.container = extension.container;
    }
    // Er lidt ked af at returnere ComponentMirror... Det er ikke verdens undergang...
    // Men maaske skulle vi have noget vi kan refererer andre steder?
    // Jeg ved dog ikke hvad eftersom det er stateless
    public ContainerMirror link(Assembly<?> assembly, Wirelet... wirelets) {
        return container.link(assembly, container.realm, wirelets);
    }

    // Will maintain the realm of whoever called this method 
    public ContainerConfiguration add(Wirelet... wirelets) {
        return add(ContainerDriver.defaultDriver(), wirelets);
    }
    
    public ContainerConfiguration add(ContainerDriver<?> driver, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    
    public class Sub {


        /**
         * <p>
         * If this assembly links a container this method must be called from {@link #onComplete()}.
         * 
         * @param assembly
         *            the assembly to link
         * @param wirelets
         *            optional wirelets
         * @throws InternalExtensionException
         *             if the assembly links a container and this method was called from outside of {@link #onComplete()}
         */
        // self link... There should be no reason why users would link a container via an extension. As the container driver is
        // already fixed, so the extension can provide no additional functionality
        protected final void selfLink(Assembly<?> assembly, Wirelet... wirelets) {
           throw new UnsupportedOperationException();
        }
        
        /**
         * Links the specified assembly. This method must be called from {@link Extension#onComplete()}. Other
         * 
         * <p>
         * Creates a new container with this extensions container as its parent by linking the specified assembly. The new
         * container will have this extension as owner. Thus will be hidden from normal view
         * <p>
         * The parent component of the linked assembly will have the container of this extension as its parent.
         * 
         * @param assembly
         *            the assembly to link
         * @param wirelets
         *            optional wirelets
         * @return a model of the component that was linked
         * @throws InternalExtensionException
         *             if called from outside of {@link Extension#onComplete()} (if wiring a container)
         * @see Extension#onComplete()
         */
        public ContainerMirror link(Assembly<?> assembly, Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }
    }
}
