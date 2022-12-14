package app.packed.extension;

import java.util.stream.Stream;

import app.packed.bean.BeanMirror;

/** A specialized extension mirror for the {@link BaseExtension}. */
// Let's see how useful this will end up being
public final class BaseExtensionMirror extends ExtensionMirror<BaseExtension> {

    /* package-private */ BaseExtensionMirror() {}

    /** {@return the total number of installed beans.} */
    public int beanCount() {
        throw new UnsupportedOperationException();
    }

    /** {@return returns a stream of all installed beans.} */
    public Stream<BeanMirror> stream() {
        throw new UnsupportedOperationException();
    }
}
