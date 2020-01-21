package app.packed.service.dep2;

import java.util.Set;

import app.packed.base.Key;

public interface Dependable {

    Set<Key<?>> requires();

    Set<Key<?>> optional();
}
