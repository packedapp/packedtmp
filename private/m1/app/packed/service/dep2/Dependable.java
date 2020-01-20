package app.packed.service.dep2;

import java.util.Set;

import app.packed.lang.Key;

public interface Dependable {

    Set<Key<?>> requires();

    Set<Key<?>> optional();
}
