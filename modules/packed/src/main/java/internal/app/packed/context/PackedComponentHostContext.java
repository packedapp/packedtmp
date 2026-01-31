package internal.app.packed.context;

import java.util.Set;

import app.packed.application.ApplicationMirror;
import app.packed.application.ManagedApplicationRuntime;
import app.packed.binding.Key;
import app.packed.component.SidehandleContext;
import app.packed.service.ServiceLocator;

public record PackedComponentHostContext(Set<Key<?>> keys) implements SidehandleContext {

    static final Set<Key<?>> KEYS = Set.of(Key.of(ApplicationMirror.class), Key.of(String.class), Key.of(ManagedApplicationRuntime.class), Key.of(ServiceLocator.class));

    public static final SidehandleContext DEFAULT = new PackedComponentHostContext(KEYS);
}
