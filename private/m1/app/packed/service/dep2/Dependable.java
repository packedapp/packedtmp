package app.packed.service.dep2;

import java.util.Set;

import app.packed.base.Key;

//If exist ServiceContract should extend it...

// Main problem is sidecars....
// Various extensions might 
public interface Dependable {

    Set<Key<?>> requires();

    Set<Key<?>> optional();
}
