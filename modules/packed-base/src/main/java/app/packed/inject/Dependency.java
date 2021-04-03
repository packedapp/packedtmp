package app.packed.inject;

import app.packed.base.Variable;

public interface Dependency {

    Class<?> rawType();
    
    boolean isOptional();
    
    boolean isProvider();
    
    Variable variable();
}
// InjectionContext -> Er ikke en dependency...