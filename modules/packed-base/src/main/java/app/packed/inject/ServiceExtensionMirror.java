package app.packed.inject;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import app.packed.application.BaseMirror;
import app.packed.base.Key;
import app.packed.component.Assembly;
import app.packed.container.ContainerMirror;
import packed.internal.component.ComponentSetup;
import packed.internal.container.ContainerSetup;

// Wow wow wow...
public interface ServiceExtensionMirror {

    Set<Key<?>> exportedKeys();

    public static Optional<ServiceExtensionMirror> find(ContainerMirror container) {
        ContainerSetup cs = (ContainerSetup) ComponentSetup.unadapt(null, container.component());
        if (cs.isUsed(ServiceExtension.class)) {
            return Optional.of(cs.injection.getServiceManager().mirror());
        } else {
            return Optional.empty();
        }
    }

    public static ServiceExtensionMirror of(ContainerMirror container) {
        return find(container).orElseThrow(NoSuchElementException::new);
    }

    public static ServiceExtensionMirror reflect(Assembly<?> assembly) {
        return tryReflect(assembly).orElseThrow(NoSuchElementException::new);
    }

    public static Optional<ServiceExtensionMirror> tryReflect(Assembly<?> assembly) {
        BaseMirror application = BaseMirror.reflect(assembly);
        return find(application.container());
    }
}
