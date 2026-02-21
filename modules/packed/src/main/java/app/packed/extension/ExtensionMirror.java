package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

import app.packed.build.BuildCodeSourceMirror;
import app.packed.container.ContainerMirror;
import app.packed.util.TreeView.Node;
import internal.app.packed.extension.PackedExtensionHandle;

/**
 * The base class for specialized extension mirrors.
 * <p>
 * This class can be extended by an extension to provide more detailed information about the extension. For example,
 * {@link app.packed.extension.BaseExtension} extends this class via {@link app.packed.extension.BaseExtensionMirror}.
 * <p>
 * Extension mirror instances are typically obtained via calls to {@link ApplicationMirror#use(Class)} or
 * {@link ContainerMirror#use(Class)}.
 * <p>
 * Noget omkring local mode and non-local mode.
 * <p>
 * NOTE: In order to properly implement a specialized extension mirror you:
 * <ul>
 * <li>Must override {@link Extension#newExtensionMirror()} in order to provide a new instance of the mirror.</li>
 * <li>Must place the mirror in the same module as the extension itself (iff the extension is defined in a module).</li>
 * <li>Should name the mirror class {@code $NAME_OF_EXTENSION$}Mirror.</li>
 * </ul>
 *
 * @param <E>
 *            The type of extension the subclassed mirror belongs to.
 *
 * @see ApplicationMirror#use(Class)
 * @see ContainerMirror#use(Class)
 * @see Extension#newExtensionMirror()
 */
// Extensions does not have a runtime representation and is hence not a component.
// Unfortunately, you can argue the same with Assembly...

///// DO WE NEED EXTENSION MIRROR???? Or is this information better handled, for example, from namespace
public abstract non-sealed class ExtensionMirror<E extends Extension<E>> implements BuildCodeSourceMirror {

    /*
     * When naming methods in this class try to avoid using trivial names such as {@code name}, {@code type}, {@code stream}
     * as sub-classes might want to make use of such names.
     *
     * This class contains a number of all* methods. There are no exact criteria for what methods to include. Only that they
     * should be generally helpful for developers extending this class.
     */

    final PackedExtensionHandle<E> handle;

    /**
     * Create a new extension mirror.
     * <p>
     * Subclasses should have a single constructor with package-private access.
     * <p>
     * Attempting to use any of the methods on this class from the constructor of a subclass, will result in an
     * {@link IllegalStateException} being thrown.
     */
     ExtensionMirror(ExtensionHandle<E> handle) {
        this.handle = (PackedExtensionHandle<E>) requireNonNull(handle);
    }

    /**
     * Returns whether any extensions match the provided predicate.
     * <p>
     * May not evaluate the predicate on all extensions if not necessary for determining the result.
     *
     * @param predicate
     *            a predicate to apply to all extensions of this mirror
     * @return {@code true} if any extensions of the mirror match the provided predicate, otherwise {@code false}
     * @see Stream#anyMatch(Predicate)
     */
    // all->each?
    protected final boolean allAnyMatch(Predicate<? super E> predicate) {
        return allStream().anyMatch(predicate);
    }

    protected final <T> List<T> allCollectToList(BiConsumer<E, List<T>> action) {
        requireNonNull(action, "action is null");
        ArrayList<T> result = new ArrayList<>();
        for (E t : node()) {
            action.accept(t, result);
        }
        return result;
    }

    protected final void allForEach(Consumer<E> action) {
        requireNonNull(action, "action is null");
        for (E t : node()) {
            action.accept(t);
        }
    }

    protected final Stream<E> allStream() {
        return node().stream();
    }

    /**
     * @param mapper
     *            a mapper from the extension to an integer
     * @return the sum
     *
     * @throws ArithmeticException
     *             if the result overflows an int
     * @see Math#addExact(int, int)
     */
    protected final int allSumInt(ToIntFunction<? super E> mapper) {
        requireNonNull(mapper, "mapper is null");
        int result = 0;
        for (E t : node()) {
            int tmp = mapper.applyAsInt(t);
            result = Math.addExact(result, tmp);
        }
        return result;
    }

    protected final long allSumLong(ToLongFunction<? super E> mapper) {
        requireNonNull(mapper, "mapper is null");
        long result = 0;
        for (E t : node()) {
            long tmp = mapper.applyAsLong(t);
            result = Math.addExact(result, tmp);
        }
        return result;
    }

    /** {@return the container this extension is part of.} */
    public final ContainerMirror container() {
        return handle.extension().container.mirror();
    }

    /** {@return the class of the extension.} */
    public final Class<? extends Extension<?>> extensionClass() {
        return handle.extension().extensionType;
    }

    /** {@return a descriptor for the extension this mirror is a part of.} */
    public final ExtensionDescriptor extensionDescriptor() {
        return ExtensionDescriptor.of(extensionClass());
    }

    /**
     * {@return all the extensions that are being mirrored.}
     *
     * @throws IllegalStateException
     *             if called from the constructor of the mirror
     */
    protected final Node<E> node() {
        return handle.applicationNode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Mirror for " + extensionDescriptor().type().getCanonicalName();
    }
}
