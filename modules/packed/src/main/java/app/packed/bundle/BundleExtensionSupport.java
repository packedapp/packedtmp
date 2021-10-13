package app.packed.bundle;

import app.packed.component.Realm;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionSupport;
import app.packed.extension.InternalExtensionException;

public /* primitive */ class BundleExtensionSupport extends ExtensionSupport {

    /** The bundle extension instance. */
    private final BundleExtension extension;

    BundleExtensionSupport(BundleExtension extension) {
        this.extension = extension;
//      public ContainerMirror link(Assembly<?> assembly, Wirelet... wirelets) {
//          return container.link(assembly, realm(), wirelets);
//      }
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
    BundleMirror selfLink(BundleAssembly assembly, Wirelet... wirelets) {
        return BundleExtension.link(assembly, extension.container, extension.extension.realm(), wirelets);
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
    public BundleMirror link(BundleAssembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public BundleMirror link(Realm realm, BundleAssembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}
