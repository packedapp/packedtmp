package app.packed.inject;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Key;
import app.packed.component.Assembly;
import app.packed.container.ContainerMirror;

// Wow wow wow...
public interface ServiceExtensionMirror {

    Set<Key<?>> exportedKeys();
    
    public static Optional<ServiceExtensionMirror> find(ContainerMirror container) {
        throw new UnsupportedOperationException();
    }

    public static ServiceExtensionMirror of(Assembly<?> assembly) {
        throw new UnsupportedOperationException();
    }

    public static ServiceExtensionMirror of(ContainerMirror container) {
        return find(container).orElseThrow(NoSuchElementException::new);
    }
}
