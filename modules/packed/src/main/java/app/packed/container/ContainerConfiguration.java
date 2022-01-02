package app.packed.container;

import java.util.Set;

import app.packed.application.ApplicationDescriptor;
import app.packed.base.NamespacePath;
import app.packed.component.ComponentConfiguration;
import app.packed.extension.Extension;
import packed.internal.container.ContainerSetup;
import packed.internal.container.PackedContainerHandle;

/**
 * The configuration of a container.
 */
public final class ContainerConfiguration extends ComponentConfiguration {

    /** Must be created through Assembly, ContainerDriver or Composer. */
    ContainerConfiguration() {
        this.container = null;
    }

    /** The component we are configuring. Is initially null until initialized by someone. */
    final ContainerSetup container;

    @Override
    protected void checkIsWiring() {
        container.checkIsWiring();
    }

    @Override
    public NamespacePath path() {
        return container.path();
    }

    @Override
    public String toString() {
        return container.toString();
    }

    public ContainerConfiguration(ContainerHandle containerHandle) {
        PackedContainerHandle bh = (PackedContainerHandle) containerHandle;
        this.container = bh.setup;
    }

    /** {@return a descriptor for the application the container is a part of.} */
    // Why not just an application mirror???
    public ApplicationDescriptor application() {
        return container.application.descriptor;
    }


    public void embed(Assembly assembly) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@return an unmodifiable view of the extensions that are currently used by this container.}
     * 
     * @see #use(Class)
     * @see BaseAssembly#extensionsTypes()
     * @see ContainerMirror#extensionsTypes()
     */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
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
    public boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
        return container.isExtensionUsed(extensionType);
    }

    /** {@return a mirror for the container.} */
    @Override
    public ContainerMirror mirror() {
        return container.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerConfiguration named(String name) {
        container.named(name);
        return this;
    }

    public <W extends Wirelet> WireletSelection<W> selectWirelets(Class<W> wireletClass) {
        return container.selectWirelets(wireletClass);
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
    public <E extends Extension<?>> E use(Class<E> extensionType) {
        return container.useExtension(extensionType);
    }
}

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
