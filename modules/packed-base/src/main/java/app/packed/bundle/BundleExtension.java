package app.packed.bundle;

import app.packed.extension.Extension;
import packed.internal.bundle.ContainerSetup;
import packed.internal.bundle.ExtensionSetup;
import packed.internal.bundle.PackedBundleDriver;
import packed.internal.component.ComponentSetup;
import packed.internal.component.RealmSetup;
import packed.internal.util.ThrowableUtil;

// Ja, lad os se om den giver mening at have en extension for det
// Og ikke bare smide det direkte paa ContainerConfiguration/Extension
// Altsaa vi har allerede installeret en Container. Som er den vi linker fra

// Det der taler imod det, er hvis support er spredt udover flere extensions.
// Eftersom vi ikke kan tilfoeje flere extensions efter vi har tilfoejet den foerste af dem


// Taenker den har baade bundles + containers...


public class BundleExtension extends Extension {

    /** The service manager. */
    final ContainerSetup container;

    final ExtensionSetup extension;

    /**
     * Create a new container extension.
     * 
     * @param setup
     *            an extension setup object (hidden).
     */
    BundleExtension(ExtensionSetup extension) {
        this.extension = extension;
        this.container = extension.container;
    }

    // Er lidt ked af at returnere ComponentMirror... Det er ikke verdens undergang...
    // Men maaske skulle vi have noget vi kan refererer andre steder?
    // Jeg ved dog ikke hvad eftersom det er stateless
    
    // LinkedBundle
    public BundleMirror link(Bundle<?> assembly, Wirelet... wirelets) {
        return link(assembly, container, container.realm, wirelets);
    }

    // Will maintain the realm of whoever called this method
    //// Ikke sikker på vi tager wirelets her...
    public BundleConfiguration add(Wirelet... wirelets) {
        return add(BundleDriver.defaultDriver(), wirelets);
    }

    //// Ikke sikker på vi tager wirelets her...
    public BundleConfiguration add(BundleDriver<?> driver, Wirelet... wirelets) {
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
    static final BundleMirror link(Bundle<?> assembly, ComponentSetup parent, RealmSetup realm, Wirelet... wirelets) {
        // Extract the component driver from the assembly
        PackedBundleDriver<?> driver = PackedBundleDriver.getDriver(assembly);

        // Create the new realm that should be used for linking
        RealmSetup newRealm = realm.link(driver, parent, assembly, wirelets);

        // Create the component configuration that is needed by the assembly
        BundleConfiguration configuration = driver.toConfiguration(newRealm.root);

        // Invoke Assembly::doBuild which in turn will invoke Assembly::build
        try {
            RealmSetup.MH_ASSEMBLY_DO_BUILD.invoke(assembly, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Close the new realm again after the assembly has been successfully linked
        newRealm.close();

        return (BundleMirror) newRealm.root.mirror();
    }
}
