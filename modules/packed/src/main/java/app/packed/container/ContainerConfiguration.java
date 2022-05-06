package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.ComponentConfiguration;
import packed.internal.container.AssemblyUserRealmSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.PackedContainerDriver;

/**
 * The configuration of a container.
 */
public non-sealed class ContainerConfiguration extends ComponentConfiguration {

    /**
     * A marker configuration object to indicate that a composer or assembly has already been used to build something. Must
     * never be exposed to end-users.
     */
    static final ContainerConfiguration USED = new ContainerConfiguration();

    /** The component we are configuring. Is only null for {@link #USED}. */
    @Nullable
    final ContainerSetup container;

    /** Used by {@link #USED}. */
    private ContainerConfiguration() {
        this.container = null;
    }

    public ContainerConfiguration(ContainerDriver containerHandle) {
        PackedContainerDriver bh = (PackedContainerDriver) containerHandle;
        this.container = bh.setup;
    }

    // On container or assembly???
    // Not container I think...
    // Assembly + Extension
    // So on Realm, eller application maaske
    // Maaske holder vi den bare paa det respektive configurations objekt.
    // Consumer<Throwable>
    // Tror kun vi har behov for den for extension ikke?
    final void addCloseAction(Runnable action) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    protected void checkIsConfigurable() {
        if (container.realm.isClosed()) {
            throw new IllegalStateException("This container is no longer configurable");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected final void checkIsCurrent() {
        container.checkIsCurrent();
    }

    final void embed(Assembly assembly) {
        /// MHT til hooks. Saa tror jeg faktisk at man tager de bean hooks
        // der er paa den assembly der definere dem
        
        // Men der er helt klart noget arbejde der
        throw new UnsupportedOperationException();
    }

    /**
     * {@return an unmodifiable view of the extensions that are currently used by this container.}
     * 
     * @see #use(Class)
     * @see BaseAssembly#extensionsTypes()
     * @see ContainerMirror#extensionsTypes()
     */
    public final Set<Class<? extends Extension<?>>> extensionTypes() {
        return container.extensionTypes();
    }

    /**
     * Returns whether or not the specified extension is used by this extension, other extensions, or user code in the same
     * container as this extension.
     * 
     * @param extensionType
     *            the extension type to test
     * @return {@code true} if the extension is currently in use, otherwise {@code false}
     * @implNote Packed does not perform detailed tracking on which extensions use other extensions. As a consequence it
     *           cannot give a more detailed answer about who is using a particular extension
     */
    public final boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
        return container.isExtensionUsed(extensionType);
    }

    public ContainerMirror link(Assembly assembly, Wirelet... wirelets) {
        return link(new PackedContainerDriver(container), assembly, wirelets);
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
    //// Har svaert ved at se at brugere vil bruge deres egen ContainerDRiver...
    public ContainerMirror link(ContainerDriver driver, Assembly assembly, Wirelet... wirelets) {
        PackedContainerDriver d = (PackedContainerDriver) requireNonNull(driver, "driver is null");

        checkIsConfigurable();
        // Wire the current component
        container.assembly.wireComplete();
        
        // Create a new realm for the assembly
        AssemblyUserRealmSetup newRealm = new AssemblyUserRealmSetup(d, container, assembly, wirelets);
        

        // Close the new realm again after the assembly has been successfully linked
        newRealm.build();

        return (ContainerMirror) newRealm.container.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerConfiguration named(String name) {
        container.named(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final NamespacePath path() {
        return container.path();
    }
    
    // never selects extension wirelets...
    public final <W extends Wirelet> WireletSelection<W> selectWirelets(Class<W> wireletClass) {
        return container.selectWirelets(wireletClass);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return container.toString();
    }

    /**
     * Returns an extension of the specified type.
     * <p>
     * If this is the first time an extension of the specified type has been requested. This method will create a new
     * instance of the extension. This instance will then be returned for all subsequent calls to this method for the same
     * extension type.
     * 
     * @param <E>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if the underlying container is no longer configurable and an extension of the specified type is not
     *             already in used
     * @see #extensionsTypes()
     */
    public final <E extends Extension<?>> E use(Class<E> extensionType) {
        return container.useExtension(extensionType);
    }
}

//// Virker underlig den ikke er paa component
///** {@return a descriptor for the application the container is a part of.} */
//// Why not just an application mirror??? Why not on Component?
//// I think it is better on (user) realm
//public ApplicationDescriptor application() {
//    return container.application.descriptor;
//}

// Altsaa vi har den kun paa assembly, men maaske
/**
 * The lookup object passed to this method is never made available through the public API. It is only used internally.
 * Unless your private
 * 
 * @param lookup
 *            the lookup object
 */
// Used by beans/functions??? We actually need it functions as well, Or hmmm....... tror ikke vi vil
// Maaske skal den bare paa bean extension????
// !!! Maaske er det en del af assemblien
// Men saa kan man ikke bruge ContainerConfiguration???
// Ellers syntes jeg bare det skal vaere paa ComponentConfiguration...

// Alle ComponentConfiguration har en lookup function... Hmm
// Passer ikke saa godt med Beans vi vil gerne have lookup funktionen inden vi installere boennen
//void lookup(Lookup lookup) {
//    requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
//    container().realm.lookup(lookup);
//}
