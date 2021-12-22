package app.packed.lifetime;

import java.util.Optional;
import java.util.Set;

import app.packed.bean.operation.BeanLifecycleOperationMirrorPlan;
import app.packed.component.ComponentMirror;
import app.packed.mirror.Mirror;

// Kan man have Dependent beans... DVS beans

// Component Lifetime?

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
public interface LifetimeMirror extends Mirror {

    Set<LifetimeMirror> children();
    
    ComponentMirror component();

    Set<ComponentMirror> components();

    Optional<LifetimeMirror> parent();

    BeanLifecycleOperationMirrorPlan plan();

    LifetimeMirror root(); // application?
    
    // Noget om hvordan den bliver aktiveret???
    //// Altsaa fx fra hvilken operation
}


enum LifetimeType {
    
    CONTAINER,

    DEPENDANT,
    
    
    LAZY,
    
    NON_APPLICATION_CONTAINER, // A new instance is created per operation Request
    
    OPERATION,
    ;
}

// Static functions-> Application or Empty??
// Beans whose lifetime is not managed by Packed? Fx stuff that is validated

// Maaske har vi dependant??? Som en seperate Lifetime...

/// Vi kan maaske 