package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Set;

import app.packed.application.ApplicationDescriptor;
import app.packed.component.ComponentConfiguration;
import app.packed.extension.Extension;
import packed.internal.component.ComponentSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 * The configuration of a container.
 */
public final class ContainerConfiguration extends ComponentConfiguration {

    /** A method handle that can access superclass ComponentConfiguration#component(). */
    private static final MethodHandle MH_COMPONENT_CONFIGURATION_SETUP = MethodHandles.explicitCastArguments(
            LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ComponentConfiguration.class, "component", ComponentSetup.class),
            MethodType.methodType(ContainerSetup.class, ContainerConfiguration.class));

    /** Must be created through Assembly, ContainerDriver or Composer. */
    ContainerConfiguration() {}

    /** {@return a descriptor for the application the container is a part of.} */
    // Why not just an application mirror???
    public ApplicationDescriptor application() {
        return container().application.descriptor;
    }

    /** {@return the wrapped container.} */
    ContainerSetup container() {
        try {
            return (ContainerSetup) MH_COMPONENT_CONFIGURATION_SETUP.invokeExact(this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /**
     * {@return an unmodifiable view of the extensions that are currently used by this container.}
     * 
     * @see #use(Class)
     * @see BaseAssembly#extensionsTypes()
     * @see ContainerMirror#extensionsTypes()
     */
    public Set<Class<? extends Extension>> extensionTypes() {
        return container().extensionTypes();
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
    public boolean isExtensionUsed(Class<? extends Extension> extensionType) {
        return container().isExtensionUsed(extensionType);
    }

    public ContainerMirror link(Assembly assembly, Wirelet... wirelets) {
        return container().link(assembly, wirelets);
    }

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

    public void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
        container().realm.setLookup(lookup);
    }

    /** {@return a mirror for the container.} */
    @Override
    public ContainerMirror mirror() {
        return container().mirror();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerConfiguration named(String name) {
        super.named(name);
        return this;
    }

    public <W extends Wirelet> WireletSelection<W> selectWirelets(Class<W> wireletClass) {
        return container().selectWirelets(wireletClass);
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
    public <E extends Extension> E use(Class<E> extensionType) {
        return container().useExtension(extensionType);
    }
}
