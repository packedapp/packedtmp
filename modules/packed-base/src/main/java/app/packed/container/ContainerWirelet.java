package app.packed.container;

import app.packed.component.Wirelet;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionBean;
import app.packed.extension.ExtensionConfiguration;
import app.packed.extension.ExtensionMember;
import app.packed.extension.InternalExtensionException;
import packed.internal.component.WireletModel;
import packed.internal.util.StackWalkerUtil;

/**
 * A container wirelet is a type of wirelet that can be specified when wiring a container.
 * <p>
 * Container wirelets are consumed in a couple of different ways depending on whether or not it is an extension or
 * user-code that defines the wirelet.
 * <p>
 * Wirelet scope
 * 
 * Naah kan ogsaa bruges paa drivere... Maa nok hellere snakke om target istedet for
 * <p>
 * Extensions that define their own wirelets must do so by extending this class (or a subclass hereof).
 * <p>
 * Extensions that extends this class should annotate the overridden class with {@link ExtensionMember} to indicate what
 * extension the wirelet is a member of.
 * 
 * Extension wirelets must be defined in the same module as the extension itself. Failure to do so will result in an
 * {@link InternalExtensionException} being thrown at runtime.
 * 
 * 
 * @see ExtensionConfiguration#selectWirelets(Class)
 * @see Extension#selectWirelets(Class)
 * @see Assembly#selectWirelets(Class)
 */
// The way ContainerWirelets can be received depends on whether or not is an extension or user code

// Buildtime -> selectWirelets from Extension/Assembly (ikke Composer fordi den mapper ikke til en component)

// At runtime injection into any bean/factory... Vi supportere det kun i forbindelse med bean creation...
// Eller jo vel ogsaa bean injection? Men ikke efter initialize
// BEAN MUST be application lifetime (non-lazy)

public abstract non-sealed class ContainerWirelet extends Wirelet {

    protected void onUnprocessed() {
        // Invoked by the runtime if the wirelet is not processed in some way

        // look up extension member
        // HOW to know from this method whether or not the extension is in use???
        // Could use ThreadLocal...
    }

    // Ideen er man ikke kan angives paa rod niveau
    // Tror faktisk kun den giver mening for extension, og ikke user wirelets

    // notOnRoot(); siger lidt mere end needsRealm...
    // Men det er maaske problem on app-on-app

    /**
     * A static initializer method that indicates that the wirelet must be specified at build-time.
     * 
     * <p>
     * Wirelets cannot be specified at runtime. This prohibits the wirelet from being specified when using an image.
     * 
     * <p>
     * If this method is called from an inheritable wirelet. All subclasses of the wirelet will retain build-time only
     * status. Invoking this method on subclasses with a super class that have already invoked it. Will fail with an
     * exception(or error).
     * <p>
     * I think you can only have wirelets injected at build-time if they are build-time only... Nej, vi skal fx
     * bruge @Provide naar vi linker assemblies...
     */
    // Den giver ogsaa meningen for brugere iff de kan faa fat i den fra en assembly
    // selectWirelets(Ssss.class).
    //// Den giver kun mening specifikt for container wirelets. Da fx Beanwirelets aldrig
    // bliver specificeret paa runtime

    // Tror maaske vi aendrer det til support runtime...
    protected static final void $buildtimeOnly() {
        WireletModel.bootstrap(StackWalkerUtil.SW.getCallerClass()).buildtimeOnly();
    }

    protected static final void $needsRealm() {
        // Wirelet.wireletRealm(Lookup); // <-- all subsequent wirelets
        // Wirelet.wireletRealm(Lookup, Wirelet... wirelets);

        // Tror det er vigtigt at der er forskel på REALM og BUILDTIME
        // Tror faktisk

        // f.x provide(Doo.class);
        // Hvad hvis vi koere composer.lookup()...
        // Saa laver vi jo saadan set en realm...
    }
    // Unless otherwise configured... An extension Wirelet

    // Metoden kan extendes med den
    protected @interface onRuntime {
        boolean noOnBuildInvoke() default false;
    }
}

//// Once upon a time we where ExtensionWirelet
// Invoked by the runtime.. Whenever
// Man skal naesten have onWireletsWired() callback saa
// Skal invokeres efter extensionen er blevet initialiseret, men foer
// onInitialize()
//protected void onBuild(E extension) {}

// A wirelet that can also be specified at runtime...
class ZExtensionRuntimeWirelet<E extends Extension, R extends ExtensionBean<E>> extends ContainerWirelet {
    protected void onInitialize(E extension) {} // maa det vaere
}
