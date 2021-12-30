package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;

import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.ExtensionTree;
import packed.internal.bean.BeanSetup;

/**
 * A mirror for a {@link BeanExtension}.
 */
@ExtensionMember(BeanExtension.class)
public final class BeanExtensionMirror extends ExtensionMirror /* extends Iterable<BeanMirror> */ {

    /** The bean extension we are mirroring. */
    private final ExtensionTree<BeanExtension> tree;

    BeanExtensionMirror(ExtensionTree<BeanExtension> tree) {
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
