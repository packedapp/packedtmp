package packed.internal.container;

import java.util.Set;

import app.packed.component.ComponentMirror;
import app.packed.container.Extension;

public interface AbstractExtensionMirrorContext {

    @Override
    int hashCode();

    boolean equalsTo(Object other);

    default Set<ComponentMirror> installed() {
        throw new UnsupportedOperationException();
    }

    Class<? extends Extension> type();

}
