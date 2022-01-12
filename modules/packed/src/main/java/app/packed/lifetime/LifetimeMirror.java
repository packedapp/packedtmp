package app.packed.lifetime;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import app.packed.bean.BeanMirror;
import app.packed.bean.operation.BeanLifecycleOperationMirrorPlan;
import app.packed.component.ComponentMirrorTree;
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

    LifetimeMirror applicationRoot();
    
    Set<LifetimeMirror> children();
    
    ComponentMirrorTree components();

    boolean isSingleton(); // I relation til foraeldren

    Optional<LifetimeMirror> parent();

    BeanLifecycleOperationMirrorPlan plan();
    
    List<BeanMirror> beanLifecycleOrder();
    // Noget om hvordan den bliver aktiveret???
    //// Altsaa fx fra hvilken operation
}


interface LifetimeMirror2  {
    // Altsaa det er taenkt paa at man kan have fx application dependencies.
    // Altsaa en egentlig graph af ting der skal vaere oppe og koere.
    Set<LifetimeMirror> dependants();
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