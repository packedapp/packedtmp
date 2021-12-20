package app.packed.lifetime;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import app.packed.bean.operation.BeanLifecycleOperationMirror;
import app.packed.component.ComponentMirror;
import app.packed.mirror.Mirror;

// Kan man have Dependent beans... DVS beans

// Component Lifetime?
public interface LifetimeMirror extends Mirror {

    List<BeanLifecycleOperationMirror> beanInitializers();
    
    Set<LifetimeMirror> children();

    ComponentMirror component();

    Set<ComponentMirror> components();

    Optional<LifetimeMirror> parent();

    LifetimeMirror root(); // application?
}


enum LifetimeType {
    
    APPLICATION,
    
    DEPENDANT,
    
    LAZY, // A new instance is created per operation Request
    
    NON_APPLICATION_CONTAINER,
    
    OPERATION,
    
    STATIC;
}

// Static functions-> Application or Empty??
// Beans whose lifetime is not managed by Packed? Fx stuff that is validated

// Maaske har vi dependant??? Som en seperate Lifetime...

/// Vi kan maaske 