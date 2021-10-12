package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionMirror;

/**
 * A mirror for the {@link BeanExtension}.
 */
@ExtensionMember(BeanExtension.class)
public final class BeanExtensionMirror extends ExtensionMirror {

    /** The bean extension we are mirroring.*/
    private final BeanExtension extension;

    BeanExtensionMirror(BeanExtension extension) {
        this.extension = requireNonNull(extension);
    }

    /** {@return the total number of configured beans in the bundle.} */
    public int beanCount() {
        return extension.extension.hashCode();
    }
}
