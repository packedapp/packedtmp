package packed.internal.container;

import java.util.Set;

import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;

public interface InternalExtensionMirror {

    ContainerMirror container();

    boolean equalsTo(Object other);

    @Override
    int hashCode();

    default Set<ComponentMirror> installed() {
        throw new UnsupportedOperationException();
    }

    Class<? extends Extension> type();
}
