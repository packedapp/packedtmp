package app.packed.lifetime;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.application.ComponentMirrorTree;
import app.packed.base.Nullable;
import app.packed.bean.BeanMirror;
import app.packed.container.Extension;
import app.packed.container.ExtensionMirror;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.Mirror;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.lifetime.LifetimeSetup;

/**
 * A component whose lifetime is managed by Packed.
 * <p>
 * Stuff managed
 * <p>
 * Stuff not managed
 * 
 * Functional beans
 * 
 * Validator beans
 * 
 */
//https://thesaurus.plus/related/life_cycle/lifetime

//Har vi ContainerLifetime/BeanLifetime???

// Har svaert ved at forstille mig man kan customize denne?
// StatelessLifetimeMirror, UnmanagedLifetimeMirror, ManagedLifetimeMirror????
public class LifetimeMirror implements Mirror {

    /**
     * The internal configuration of the operation we are mirrored. Is initially null but populated via
     * {@link #initialize(ExtensionSetup)}.
     */
    @Nullable
    private LifetimeSetup lifetime;

    /**
     * Create a new operation mirror.
     * <p>
     * Subclasses should have a single package-protected constructor.
     */
    public LifetimeMirror() {}

    /**
     * {@return the internal configuration of operation.}
     * 
     * @throws IllegalStateException
     *             if {@link #initialize(ApplicationSetup)} has not been called.
     */
    private LifetimeSetup lifetime() {
        LifetimeSetup a = lifetime;
        if (a == null) {
            throw new IllegalStateException(
                    "Either this method has been called from the constructor of the mirror. Or the mirror has not yet been initialized by the runtime.");
        }
        return a;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof LifetimeMirror m && lifetime() == m.lifetime();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return lifetime().hashCode();
    }

    /**
     * Invoked by {@link Extension#mirrorInitialize(ExtensionMirror)} to set the internal configuration of the extension.
     * 
     * @param owner
     *            the internal configuration of the extension to mirror
     */
    final void initialize(LifetimeSetup operation) {
        if (this.lifetime != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.lifetime = operation;
    }

    /**
     * If this lifetime is not stateless returns the bean that controls creation and destruction of the lifetime.
     * 
     * @return
     */
    public Optional<BeanMirror> managedByBean() {
        List<LifetimeOperationMirror> operations = operations();
        return operations.isEmpty() ? Optional.empty() : Optional.of(operations.get(0).bean());
    }

    /** {@return a stream of child lifetimes of this lifetime.} */
    public Stream<LifetimeMirror> children() {
        throw new UnsupportedOperationException();
    }

    /**
     * If this is container or application lifetime and it has a container lifetime wrapper bean. Returns the bean,
     * otherwise empty.
     * <p>
     * A wrapper and {@link #managedByBean()} is always two different beans.
     * 
     * @return
     */
    public Optional<BeanMirror> wrapper() { // Do we need a ContainerWrapperBeanMirror?
        return Optional.empty();
    }

    /**
     * empty for statelss
     * 
     * 1 for unmananged
     * 
     * 1 or 2 for managed
     * 
     * <p>
     * If this method returns more than 1 lifetime operation mirror. The returned operations will always be defined on the
     * same bean
     * 
     * @return a list of this lifetime's lifetime operations
     */
    public List<LifetimeOperationMirror> operations() {
        throw new UnsupportedOperationException();
    }

    /** {@return all components that are part of the lifetime.} */
    // A tree of containers and beans
    // Maaske er det i virkeligheden bare en stream af componenter depth first
    public ComponentMirrorTree components() {
        throw new UnsupportedOperationException();
    }

    // If has a holder
    // -- If is a bean -> Holder is in same container as the root of the lifetime
    // -- If is a non-root container -> Holder is in parent container
    // -- If is a non-root application -> Holder is in parent application
    // -- If a a root application -> Holder is a single bean in an bootstrap application

    public OldLifetimeKind lifetimeKind() {
        throw new UnsupportedOperationException();
    }

    // Hvad med sync/async start/stop??? Det er externt bestemt
    /**
     * First operation always creates the lifetime. Last operation always destroys the lifetime if managed.
     * 
     * If managed and size = 1 the operation does all of it
     * 
     * Only the create operation takes parameters
     * 
     * @return
     */
    // interne operationer (i lifetimen)
    // Externe operationer (der bliver brugt til at lave lifetimen, og destroy den)
    // Tror maaske man bare maa query managedBy().managementOperations()
    // Skal vi kunne query vi noget om hvilke operationer bliver koert hvornaar??
    // Eller maaske ligger vi det bare paa launcheren
    // List<LifetimeManagementOperationMirror> managementOperations();

    /** {@return the type of lifetime.} */
    // Tror vi dropper den her. Og saa er application.container bare en container
    public LifetimeOriginKind originKind() {
        if (lifetime() instanceof ContainerLifetimeSetup c) {
            return c.container.depth==0 ? LifetimeOriginKind.APPLICATION : LifetimeOriginKind.CONTAINER;
        }
        return LifetimeOriginKind.BEAN;
    }

    /** {@return any parent lifetime this lifetime might have.} */
    public Optional<LifetimeMirror> parent() {
        return lifetime().parent == null ? Optional.empty() : Optional.of(lifetime().parent.mirror());
    }

    /**
    *
    */
    // Er application bare det samme som en root container?
    // Syntes maaske bare det er en masse is() metoder paa LifetimeMirror
    // en mindre klasse
    // isBeanOrigin(), isContainerOrigin, is ApplicationOrigin();

    // Vi vil helst have at application.lifetime == application.container.lifetime...

    public enum LifetimeOriginKind {

        /** An application is created together with lifetime. */
        APPLICATION,

        /** A single bean is created with the lifetime. */
        BEAN,

        /** A (non-root) container is created together with lifetime. */
        CONTAINER;

        // Hvad med Operation??
        // I sidste ende bliver alle lifetimes jo lavet med en operation
    }

    // function/static beans har samme lifetime som deres container

    /// Hmm maaske har vi flere LifetimeKind???
    // BeanLifetimeKind [Container, OPERATION
    // OPERATION, DEPENDANT, ...

    // Omvendt saa kan vi vel godt lave en container pga en operation????

    //// Honorable mentions

    //// Haaber vi kan undgaa at tilfoeje den her
    // DEPENDANT,

    //// Lazy er ikke paa lifetimen, men paa componenten...
    //// Fordi du er jo lazy i forhold til applikationen eller containeren.
    // LAZY;

    //// Vi dropper den her fordi vi simpelthen bare siger de ikke har nogen lifecycle
    // UNMANAGED (or Epheral)

}

//Kan man have Dependent beans... DVS beans

//Component Lifetime?

////Det er maaske mere noget a.la. hvornaar maa componenten benyttes
////istedet for hvor lang tid den lever.
////Her taenker jeg specielt paa functional beans og validation beans

//Things a lifetime does

//Determines a order between the components in the lifecycle when initializing/starting/stopping it it

//Individual initialization / stop

//Er lidt i tvivl om det her er 2 ting vi dealer med
interface LifetimeSandbox {

    // Noget om hvordan den bliver aktiveret???
    //// Altsaa fx fra hvilken operation
    ////
    Set<Object> activators();

    // Altsaa det er taenkt paa at man kan have fx application dependencies.
    // Altsaa en egentlig graph af ting der skal vaere oppe og koere.
    Set<LifetimeMirror> dependants();

    boolean isSingleton(); // I relation til foraeldren

    // BeanLifecycleMirrorPlan plan();
}
