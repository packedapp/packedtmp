package app.packed.inject;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Key;
import app.packed.component.Assembly;
import app.packed.component.Wirelet;
import app.packed.container.ContainerMirror;
import app.packed.container.ExtensionMirror;
import packed.internal.component.ComponentSetup;
import packed.internal.container.ContainerSetup;

/**
 * A mirror for a {@link ServiceExtension}.
 */
public interface ServiceExtensionMirror extends ExtensionMirror {

    ServiceContract contract();

    Set<Key<?>> exportedKeys();

    public static Optional<ServiceExtensionMirror> find(Assembly<?> assembly, Wirelet... wirelets) {
        return find(ContainerMirror.of(assembly, wirelets));
    }

    public static Optional<ServiceExtensionMirror> find(ContainerMirror container) {
        ContainerSetup cs = (ContainerSetup) ComponentSetup.unadapt(null, container.component());
        if (cs.isUsed(ServiceExtension.class)) {
            return Optional.of(cs.injection.getServiceManager().mirror());
        } else {
            return Optional.empty();
        }
    }

    public static ServiceExtensionMirror of(Assembly<?> assembly, Wirelet... wirelets) {
        return find(assembly).orElseThrow(NoSuchElementException::new);
    }

    public static ServiceExtensionMirror of(ContainerMirror container) {
        return find(container).orElseThrow(NoSuchElementException::new);
    }
}


// Vi vil gerne have ContainerMirror.use(ServiceExtensionMirror.class).contract
// Must have a static XMirror of(

// Altsaa hvis vi siger den kun skal vaere til raadighed paa