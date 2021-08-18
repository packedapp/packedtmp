package app.packed.container;

import app.packed.extension.Extension;
import app.packed.extension.InternalExtensionException;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.RealmSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.container.PackedContainerDriver;
import packed.internal.util.ThrowableUtil;

// Ja, lad os se om den giver mening at have en extension for det
// Og ikke bare smide det direkte paa ContainerConfiguration/Extension
// Altsaa vi har allerede installeret en Container. Som er den vi linker fra
//
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
        return link(assembly, container, container.realm, wirelets);
    }

    // Will maintain the realm of whoever called this method
    //// Ikke sikker på vi tager wirelets her...
    public ContainerConfiguration add(Wirelet... wirelets) {
        return add(ContainerDriver.defaultDriver(), wirelets);
    }

    //// Ikke sikker på vi tager wirelets her...
    public ContainerConfiguration add(ContainerDriver<?> driver, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    /**
     * Links a new assembly.
     * 
     * @param assembly
     *            the assembly to link
     * @param realm
     *            realm
     * @param wirelets
     *            optional wirelets
     * @return the component that was linked
     */
    static final ContainerMirror link(Assembly<?> assembly, ComponentSetup parent, RealmSetup realm, Wirelet... wirelets) {
        // Extract the component driver from the assembly
        PackedContainerDriver<?> driver = (PackedContainerDriver<?>) PackedComponentDriver.getDriver(assembly);

        // Create the new realm that should be used for linking
        RealmSetup newRealm = realm.link(driver, parent, assembly, wirelets);

        // Create the component configuration that is needed by the assembly
        ContainerConfiguration configuration = driver.toConfiguration(newRealm.root);

        // Invoke Assembly::doBuild which in turn will invoke Assembly::build
        try {
            RealmSetup.MH_ASSEMBLY_DO_BUILD.invoke(assembly, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Close the new realm again after the assembly has been successfully linked
        newRealm.close();

        return (ContainerMirror) newRealm.root.mirror();
    }

    public /* primitive */ class Sub {

        ContainerSetup container;

        Sub(ExtensionSetup setup) {
            this.container = setup.container;
//          public ContainerMirror link(Assembly<?> assembly, Wirelet... wirelets) {
//              return container.link(assembly, realm(), wirelets);
//          }
        }

        // Tror faktisk godt vi tillader at lave en container paa vegne af brugeren.
        // Fx lad os si
        
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
        ContainerMirror selfLink(Assembly<?> assembly, Wirelet... wirelets) {
            return ContainerExtension.link(assembly, container, extension.realm(), wirelets);
        }

        /**
         * Links the specified assembly. This method must be called from {@link Extension#onComplete()}. Other
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
        // Container.Owner = Operator.Extension
        // I am beginning to think that all components installed from the assembly belongs to the extension
        // And then extension is not allowed to use other extensions that its dependencies.
        public ContainerMirror link(Assembly<?> assembly, Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }
    }
}
