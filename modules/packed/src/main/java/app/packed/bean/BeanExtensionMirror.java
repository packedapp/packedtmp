package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;

import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionMirror;

/**
 * A mirror for the {@link BeanExtension}.
 */
@ExtensionMember(BeanExtension.class)
public final class BeanExtensionMirror extends ExtensionMirror /* extends Iterable<BeanMirror> */ {

    /** The bean extension we are mirroring.*/
    final BeanExtension extension;

    BeanExtensionMirror(BeanExtension extension) {
        this.extension = requireNonNull(extension);
    }

    /** {@return the total number of installed beans.} */
    public int beanCount() {
        return 123;
    }
    
    public Stream<BeanMirror> stream() {
        throw new UnsupportedOperationException();
    }
}
