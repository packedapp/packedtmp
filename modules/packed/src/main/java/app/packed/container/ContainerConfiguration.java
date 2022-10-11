package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Consumer;

import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import internal.app.packed.container.AssemblySetup;

/**
 * The configuration of a container.
 */
public class ContainerConfiguration {

    /**
     * A marker configuration object indicating that an assembly (or composer) has already been used for building. Should
     * never be exposed to end-users.
     */
    static final ContainerConfiguration USED = new ContainerConfiguration();

    /** The component we are configuring. Is only null for {@link #USED}. */
    @Nullable
    final ContainerHandle handle;

    /** Used by {@link #USED}. */
    private ContainerConfiguration() {
        this.handle = null;
    }

    /**
     * Create a new container configuration.
     * 
     * @param handle
     *            the container handle
     */
    public ContainerConfiguration(ContainerHandle handle) {
        this.handle = requireNonNull(handle, "handle is null");
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

    /**
     * 
     * @throws IllegalStateException
     *             if the container is no longer configurable
     */
    protected void checkIsConfigurable() {
        if (handle.container.realm.isClosed()) {
            throw new IllegalStateException("This container is no longer configurable");
        }
    }

    /** {@inheritDoc} */
    protected final void checkIsCurrent() {
        handle.container.checkIsCurrent();
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
        return handle.container.extensionTypes();
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
        return handle.container.isExtensionUsed(extensionType);
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
    public ContainerMirror link(Assembly assembly, Wirelet... wirelets) {

        // Check that the container is still configurable
        checkIsConfigurable();

        // Wire any current component
        handle.container.assembly.wireCurrentComponent();

        // Create a new realm for the assembly
        AssemblySetup as = new AssemblySetup(handle.container, assembly, wirelets);

        // Close the new realm again after the assembly has been successfully linked
        as.build();

        return (ContainerMirror) as.container.mirror();
    }

    /**
     * Sets the name of the component. The name must consists only of alphanumeric characters and '_', '-' or '.'. The name
     * is case sensitive.
     * <p>
     * If no name is explicitly set on a component. A name will be assigned to the component (at build time) in such a way
     * that it will have a unique name among other sibling components.
     *
     * @param name
     *            the name of the component
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @see Wirelet#named(String)
     */
    public ContainerConfiguration named(String name) {
        handle.container.named(name);
        return this;
    }

    public ContainerConfiguration newContainer(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    /**
     * Registers a callback that will be invoked whenever
     * <p>
     * If {@code Extension.class} is specified. The given action is invoked for every extension that is used.
     * 
     * @param <E>
     *            the type of
     * @param extensionType
     * @param action
     *            the action to invoke
     */
    // Skal vi have en version der tager en [Runnable alternative] hvis den ikke bliver installeret?
    public final <E extends Extension<E>> void onFirstUse(Class<E> extensionType, Consumer<? super E> action) {
        // bruger assignable
        // onFirstUse(Extension.class, ()-> Extension isUsed);
        // Taenker jeg kunne vaere brugbart i rooten....
        /// Fx for at sige (SRExtension.class, ()-> e.setPrivatelyOwned());
        // Er maaske taenkt
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the full path of the component.
     * <p>
     * Once this method has been invoked, the name of the component can no longer be changed via {@link #named(String)}.
     * <p>
     * If building an image, the path of the instantiated component might be prefixed with another path.
     * 
     * <p>
     * Returns the path of this configuration. Invoking this method will initialize the name of the component. The component
     * path returned does not maintain any reference to this configuration object.
     * 
     * @return the path of this configuration.
     */
    public final NamespacePath path() {
        return handle.container.path();
    }

    // never selects extension wirelets...
    public final <W extends Wirelet> WireletSelection<W> selectWirelets(Class<W> wireletClass) {
        return handle.container.selectWirelets(wireletClass);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return handle.toString();
    }

    /**
     * Returns an extension of the specified type.
     * <p>
     * If this is the first time an extension of the specified type has been requested. This method will create a new
     * instance of the extension. This instance will then be returned for all subsequent requests for the same extension
     * type.
     * 
     * @param <E>
     *            the type of extension to return
     * @param extensionClass
     *            the Class object corresponding to the extension type
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if the underlying container is no longer configurable and the specified type of extension has not been
     *             used previously
     * @see #extensionsTypes()
     * @see BaseAssembly#use(Class)
     */
    public final <E extends Extension<?>> E use(Class<E> extensionClass) {
        return handle.container.useExtension(extensionClass);
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
