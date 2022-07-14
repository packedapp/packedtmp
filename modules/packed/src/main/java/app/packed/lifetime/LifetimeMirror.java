package app.packed.lifetime;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.application.ComponentMirrorTree;
import internal.app.packed.container.Mirror;
import internal.app.packed.lifetime.LifetimeSetup.BuildtimeLifetimeMirror;

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

public sealed interface LifetimeMirror extends Mirror permits BuildtimeLifetimeMirror {

    // App run er vel den eneste der har en holder, men ikke en bean?

    // All lifetimes have a launcher except a bootstrap lifetime...

    /** {@return a stream of child lifetimes of this lifetime.} */
    Stream<LifetimeMirror> children();

    /** {@return all components that are part of the lifetime.} */
    // A tree of containers and beans
    // Maaske er det i virkeligheden bare en stream af componenter depth first
    ComponentMirrorTree components();

    LifetimeKind lifetimeKind();

    // If has a holder
    // -- If is a bean -> Holder is in same container as the root of the lifetime
    // -- If is a non-root container -> Holder is in parent container
    // -- If is a non-root application -> Holder is in parent application
    // -- If a a root application -> Holder is a single bean in an bootstrap application
    
    // Its unmanaged but have management operation Maybe its okay
    // Maaske kan vi launch en bean der ikke er i samme container..
    // Fx hvis vi er en extension bean. Og vi ikke gider installere saadan en.
    // I andet end root containeren.
    //// Stateless never has a management bean mirror
    default Optional<LifetimeBeanMirror> managedBy() {
        // App.run does not have a LifetimeHolder object
        // Prototype Service Bean does not have a lifetime holder
        // AsyncApp has a minimum a holder with a shutdown token
        return Optional.empty();
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
    LifetimeOriginKind originKind();

    /** {@return any parent lifetime this lifetime might have.} */
    Optional<LifetimeMirror> parent();

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
