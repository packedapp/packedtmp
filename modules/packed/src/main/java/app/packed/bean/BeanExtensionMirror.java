package app.packed.bean;

import java.util.stream.Stream;

import app.packed.container.ExtensionMirror;
import packed.internal.bean.BeanSetup;

/**
 * A mirror for a {@link BeanExtension}.
 */
public final class BeanExtensionMirror extends ExtensionMirror<BeanExtension> {

    /* package-private */ BeanExtensionMirror() {}

    /** {@return the total number of installed beans.} */
    public int beanCount() {
        return allSumInt(e -> (int) e.container.children.values().stream().filter(c -> c instanceof BeanSetup).count());
    }

    /** {@return returns a stream of all installed beans.} */
    public Stream<BeanMirror> stream() {
        throw new UnsupportedOperationException();
    }
}
//Ved ikke hvor meget vi bare smider direkte ind paa containeren?
