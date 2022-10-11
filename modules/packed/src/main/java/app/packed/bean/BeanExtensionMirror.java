package app.packed.bean;

import java.util.stream.Stream;

import app.packed.container.ExtensionMirror;

/** A specialized extension mirror for the {@link BeanExtension}. */
public final class BeanExtensionMirror extends ExtensionMirror<BeanExtension> {

    /* package-private */ BeanExtensionMirror() {}

    /** {@return the total number of installed beans.} */
    public int beanCount() {
        throw new UnsupportedOperationException();
    }

    /** {@return returns a stream of all installed beans.} */
    public Stream<BeanMirror> stream() {
        throw new UnsupportedOperationException();
    }
}
//Ved ikke hvor meget vi bare smider direkte ind paa containeren?
