package app.packed.lifetime;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import app.packed.bean.BeanMirror;
import app.packed.extension.Extension;
import app.packed.operation.CompositeOperationMirror;
import app.packed.operation.OperationMirror;
import internal.app.packed.container.Mirror;
import internal.app.packed.lifetime.LifetimeSetup;
import internal.app.packed.operation.OperationSetup;

/**
 * A mirror of a lifetime.
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
// Lifetime - The period during which something exists, lasts, or is in progress.

// Life cycle - A series of stages through which an organism passes between recurrences of a primary stage.
//https://thesaurus.plus/related/life_cycle/lifetime

public abstract sealed class LifetimeMirror implements Mirror permits BeanLifetimeMirror, ContainerLifetimeMirror {

    /**
     * If the lifetime has any entry points. This method returns the extension that is responsible for choosing the right
     * entry point. This is typically also the extension that manages the lifetime's container. Except for the root
     * containers which always is BaseExtension but may be taken over by another extension.
     * <p>
     *
     * @return
     */
    // Maaske skal vi tillade at man overtager Root Application??
    // Maaske skal man bruge CliApp... Nej vi vil gerne have mulighed for at teste
    // med en TestApp
    // Hvad hvis man har en Completer... Saa har vi ingen entry points
    public Optional<Class<? extends Extension<?>>> entryPointDispatcher() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@return a collection of any entry points this lifetime may have.}
     *
     * An entry point may be located in a child lifetime of this lifetime. In which case the child lifetime is created on
     * the condition that the particular entry point is taken.
     */
    public final Collection<OperationMirror> entryPoints() {
        return lifetime().entryPoints().stream().map(OperationSetup::mirror).collect(Collectors.toUnmodifiableList());
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

    /** {@return the lifetime kind.} */
    public final LifetimeKind kind() {
        return lifetime().lifetimeKind();
    }

    abstract LifetimeSetup lifetime();

    /**
     * empty for statelss (or BootstrapApps)
     *
     * 1 for unmananged TBe 1 or 2 for managed
     *
     * <p>
     * If this method returns more than 1 lifetime operation mirror. The returned operations will always be defined on the
     * same bean
     *
     * @return a list of this lifetime's lifetime operations
     */
    // Fungere ikke specielt godt vil jeg mene. Vi vil jo gerne vide hvad der
    // bliver koert i de forskellige lifetime. Det skal jo ikke aendre sig
    // fordi vi bruger en anden Bootstrap App
    public List<CompositeOperationMirror> operations() {
        throw new UnsupportedOperationException();
    }

    /** {@return any parent lifetime this lifetime is contained within.} */
    public abstract Optional<ContainerLifetimeMirror> parent();

    /**
     * If the lifetime needs to produce a result.
     *
     * @return {@code void.class} if this lifetime does not produce a result, otherwise the type of result the lifetime
     *         produces
     */
    public final Class<?> resultType() {
        return lifetime().resultType();
    }
}

// If has a holder
// -- If is a bean -> Holder is in same container as the root of the lifetime
// -- If is a non-root container -> Holder is in parent container
// -- If is a non-root application -> Holder is in parent application
// -- If a a root application -> Holder is a single bean in an bootstrap application

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

///** {@return the type of lifetime.} */
//// Tror vi dropper den her. Og saa er application.container bare en container
//public LifetimeOriginKind originKind() {
//  if (lifetime() instanceof ContainerLifetimeSetup c) {
//      return c.container.depth == 0 ? LifetimeOriginKind.APPLICATION : LifetimeOriginKind.CONTAINER;
//  }
//  return LifetimeOriginKind.BEAN;
//}

//Kan man have Dependent beans... DVS beans

////Det er maaske mere noget a.la. hvornaar maa componenten benyttes
////istedet for hvor lang tid den lever.
////Her taenker jeg specielt paa functional beans og validation beans

//Things a lifetime does

//Determines a order between the components in the lifecycle when initializing/starting/stopping it it

//Individual initialization / stop

//Er lidt i tvivl om det her er 2 ting vi dealer med
interface LifetimeSandbox {

//// Vi dropper den her fordi vi simpelthen bare siger de ikke har nogen lifecycle
    // UNMANAGED (or Epheral)
    // Noget om hvordan den bliver aktiveret???
    //// Altsaa fx fra hvilken operation
    ////
    Set<Object> activators();

    // Altsaa det er taenkt paa at man kan have fx application dependencies.
    // Altsaa en egentlig graph af ting der skal vaere oppe og koere.
    Set<LifetimeMirror> dependants();

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

    boolean isSingleton(); // I relation til foraeldren

    // /**
//  * If this lifetime is not stateless returns the bean that controls creation and destruction of the lifetime.
//  *
//  * @return
//  */
    default Optional<BeanMirror> managedBy() {
        List<OperationMirror> operations = List.of();// operations();
        return operations.isEmpty() ? Optional.empty() : Optional.of(operations.get(0).bean());
    }

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

    // BeanLifecycleMirrorPlan plan();
}
