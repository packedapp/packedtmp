package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;

import app.packed.extension.ExtensionMirror;
import app.packed.extension.ExtensionTree;
import packed.internal.bean.BeanSetup;

/**
 * A mirror for a {@link BeanExtension}.
 */
public final class BeanExtensionMirror extends ExtensionMirror<BeanExtension> {

    /** The bean extension we are mirroring. */
    private final ExtensionTree<BeanExtension> tree;

    /* package-private */ BeanExtensionMirror(ExtensionTree<BeanExtension> tree) {
        this.tree = requireNonNull(tree);
    }

    /** {@return the total number of installed beans.} */
    public int beanCount() {
        return tree.sumInt(e -> (int) e.container.children.values().stream().filter(c -> c instanceof BeanSetup).count());
    }

    /** {@return returns a stream of all installed beans.} */
    public Stream<BeanMirror> stream() {
        throw new UnsupportedOperationException();
    }
}
//Ved ikke hvor meget vi bare smider direkte ind paa containeren?
