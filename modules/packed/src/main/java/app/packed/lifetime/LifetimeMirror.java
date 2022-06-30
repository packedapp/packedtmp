package app.packed.lifetime;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.application.ComponentMirrorTree;
import app.packed.bean.BeanMirror;
import app.packed.operation.OperationMirror;
import internal.app.packed.container.Mirror;
import internal.app.packed.lifetime.LifetimeSetup.BuildtimeLifetimeMirror;

// Kan man have Dependent beans... DVS beans

// Component Lifetime?

//// Det er maaske mere noget a.la. hvornaar maa componenten benyttes
//// istedet for hvor lang tid den lever.
//// Her taenker jeg specielt paa functional beans og validation beans

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

public sealed interface LifetimeMirror extends Mirror permits BuildtimeLifetimeMirror {
    
    /** {@return a collection view of all beans that are part of the lifetime.} */
    Stream<BeanMirror> beans();

    /** {@return any child lifetimes of this lifetime.} */
    Stream<LifetimeMirror> children();

    /** {@return all components that are part of the lifetime.} */
    ComponentMirrorTree components();

    /** {@return the type of lifetime.} */
    LifetimeKind lifetimeType();

    // Hvad med sync/async start/stop???
    List<OperationMirror> operations(LifetimePhase phase);

    /** {@return any parent lifetime this lifetime might have.} */
    Optional<LifetimeMirror> parent();
}

//Things a lifetime does

//Determines a order between the components in the lifecycle when initializing/starting/stopping it it

//Individual initialization / stop

//Er lidt i tvivl om det her er 2 ting vi dealer med
interface LifetimeSandbox {

    // Noget om hvordan den bliver aktiveret???
    //// Altsaa fx fra hvilken operation
    ////
    Set<Object> activators();

    /** {@return the lifetime of the application.} */
    @Deprecated(since = "Tror bare man maa kalde application().lifetime()")
    LifetimeMirror applicationLifetime();

    // Altsaa det er taenkt paa at man kan have fx application dependencies.
    // Altsaa en egentlig graph af ting der skal vaere oppe og koere.
    Set<LifetimeMirror> dependants();

    default List<OperationMirror> initialization() {
        throw new UnsupportedOperationException();
    }

    boolean isCloseable();

    boolean isSingleton(); // I relation til foraeldren

   // BeanLifecycleMirrorPlan plan();

}

// Static functions-> Application or Empty??
// Beans whose lifetime is not managed by Packed? Fx stuff that is validated

// Maaske har vi dependant??? Som en seperate Lifetime...

/// Vi kan maaske 