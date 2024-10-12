package internal.app.packed.context;

import java.util.Set;

import app.packed.application.ApplicationMirror;
import app.packed.binding.Key;
import app.packed.component.guest.ComponentHostContext;
import app.packed.context.ContextTemplate;
import app.packed.runtime.ManagedLifecycle;
import app.packed.service.ServiceLocator;

public record PackedComponentHostContext(Set<Key<?>> keys) implements ComponentHostContext {

    public static final ContextTemplate TEMPLATE = ContextTemplate.of(ComponentHostContext.class).withImplementation(PackedComponentHostContext.class)
           .withBindAsConstant();

    static final Set<Key<?>> KEYS = Set.of(Key.of(ApplicationMirror.class), Key.of(String.class), Key.of(ManagedLifecycle.class), Key.of(ServiceLocator.class));

    public static final ComponentHostContext DEFAULT = new PackedComponentHostContext(KEYS);
}
