package app.packed.extension;

import app.packed.base.Nullable;
import app.packed.container.Assembly;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.inject.service.ServiceExtension;
import app.packed.inject.service.ServiceExtensionMirror;
import app.packed.mirror.Mirror;
import packed.internal.container.ExtensionSetup;

/**
 * Provides generic information about an extension used by a {@link #container}.
 * <p>
 * This class can be extended by an extension to provide more detailed information about itself. For example,
 * {@link app.packed.inject.service.ServiceExtension} extends this class via
 * {@link app.packed.inject.service.ServiceExtensionMirror}.
 * <p>
 * Extension mirror instances are typically obtained in one of the following ways:
 * <ul>
 * <li>By calling methods on other mirrors, for example, {@link ContainerMirror#extensions()} or
 * {@link ContainerMirror#findExtension(Class)}.</li>
 * <li>By calling {@link Extension#mirror()}, for example, {@link ServiceExtension#mirror()}.</li>
 * <li>By calling a factory method on the mirror class, for example,
 * {@link ServiceExtensionMirror#use(Assembly, app.packed.container.Wirelet...)}.</li>
 * </ul>
 * <p>
 * NOTE: Subclasses of this class:
 * <ul>
 * <li>Must be annotated with {@link ExtensionMember} indicating the type of extension that is being mirrored.</li>
 * <li>Must be located in the same module as the extension itself (iff the extension is defined in a module).</li>
 * <li>Must override {@link Extension#mirror()} in order to provide a mirror instance to the framework.</li>
 * <li>May provide factory methods, similar to {@link ServiceExtensionMirror#of(Assembly, Wirelet...)}.
 * </ul>
 */
public class ExtensionMirror implements Mirror {

    /**
     * The internal configuration of the extension we are mirrored. Is initially null but populated via
     * {@link #initialize(ExtensionSetup)} which must be called by extension developers via
     * {@link Extension#mirrorInitialize(ExtensionMirror)}.
     */
    @Nullable
    private ExtensionSetup extension;

    /**
     * Create a new extension mirror.
     * <p>
     * Subclasses should have a single package-protected constructor. Instantiated by overriding {@link Extension#mirror()}.
     */
    protected ExtensionMirror() {}

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        // Use case for equals on mirrors are
        // FooBean.getExtension().equals(OtherBean.getExtension())...

        // Normally there should be no reason for subclasses to override this method...
        // If we find a valid use case we can always remove final

        // Check other.getType()==getType()????
        
        // TODO if we have local extensions, we cannot just rely on extension=extension
        
        return this == other || other instanceof ExtensionMirror m && extension() == m.extension();
    }

    /**
     * {@return the mirrored extension's internal configuration.}
     * 
     * @throws InternalExtensionException
     *             if called from the constructor of the mirror, or the implementation of the extension forgot to call
     *             {@link Extension#mirrorInitialize(ExtensionMirror)} from {@link Extension#mirror()}.
     */
    private ExtensionSetup extension() {
        ExtensionSetup e = extension;
        if (e == null) {
            throw new InternalExtensionException(
                    "Either this method has been called from the constructor of the mirror. Or an extension forgot to invoke Extension#mirrorInitialize.");
        }
        return e;
    }

    // All methods are named extension*() instead of *() because subclasses might want to use descriptor()
    
    /** {@return a descriptor for the extension this mirror is a part of.} */
    public final ExtensionDescriptor extensionDescriptor() {
        return extension().model;
    }

    /** {@return the full name of the extension.} */
    public final String extensionFullName() {
        return extensionDescriptor().fullName();
    }

    /** {@return the name of the extension.} */
    public final String extensionName() {
        return extensionDescriptor().name();
    }

    /** {@return the type of extension this mirror is a part of.} */
    public final Class<? extends Extension<?>> extensionType() {
        return extension().extensionType;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return extension().hashCode();
    }

    /**
     * Invoked by {@link Extension#mirrorInitialize(ExtensionMirror)} to set the internal configuration of the extension.
     * 
     * @param extension
     *            the internal configuration of the extension to mirror
     */
    final void initialize(ExtensionSetup extension) {
        if (this.extension != null) {
            throw new IllegalStateException("The specified mirror has already been initialized.");
        }
        this.extension = extension;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return extensionType().getCanonicalName();
    }
}


//// Nahh, hellere noget beans().findAllFrom(FooExtension.class)
///** {@return a set of all beans that are registered explicitly by the extension.} */
//public ComponentMirrorSet extensionMemberBeans() {
//    // All installed beans that are owned by this extension
//    throw new UnsupportedOperationException();
//}

///** {@return the application the extension is used in.} */
//public final ApplicationMirror application() {
//  return extension().container.application.mirror();
//}

///** {@return the container the extension is used in.} */
//public final ContainerMirror container() {
//  return extension().container.mirror();
//}

//// Disse blev fjernet da vi begyndte at share extension mirrors mellem containere og applikationer.
///**
//* Builds an application, and returns a mirror for an extension in the root container if it is present. Otherwise
//* {@link Optional#empty()}.
//* 
//* @param <E>
//*            the type of extension mirror
//* @param extensionMirrorType
//*            the type of extension mirror to return
//* @param assembly
//*            the assembly
//* @param wirelets
//*            optional wirelets
//* @return stuff
//* @see ContainerMirror#findExtension(Class)
//*/
//@Deprecated
//public static <E extends ExtensionMirror> Optional<E> find(Class<E> extensionMirrorType, Assembly assembly, Wirelet... wirelets) {
//  return ContainerMirror.of(assembly, wirelets).findExtension(extensionMirrorType);
//}

///**
//* Builds an application. Throws {@link NoSuchElementException} if the root container does not use the extension that
//* the mirror represents.
//* 
//* @param <E>
//*            the type of extension mirror
//* @param mirrorType
//*            the type of extension mirror to return
//* @param assembly
//*            the assembly
//* @param wirelets
//*            optional wirelets
//* @return an extension mirror
//* @see ContainerMirror#useExtension(Class)
//* @throws NoSuchElementException
//*             if the root container in the mirrored application does not use the extension that the specified mirror is
//*             a part of
//*/
//@Deprecated
//public static <E extends ExtensionMirror> E of(Class<E> extensionMirrorType, Assembly assembly, Wirelet... wirelets) {
//  return ContainerMirror.of(assembly, wirelets).useExtension(extensionMirrorType);
//}