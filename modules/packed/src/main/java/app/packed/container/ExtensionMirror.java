package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.base.Nullable;
import packed.internal.container.Mirror;
import packed.internal.container.PackedExtensionTree;

/**
 * Provides generic information about the usage of an extension.
 * <p>
 * Noget omkring local mode and non-local mode.
 * <p>
 * This class can be extended by an extension to provide more detailed information about the extension. For example,
 * {@link app.packed.bean.BeanExtension} extends this class via {@link app.packed.bean.BeanExtensionMirror}.
 * <p>
 * Extension mirror instances are typically obtained via calls to {@link ApplicationMirror#useExtension(Class)} or
 * {@link ContainerMirror#useExtension(Class)}.
 * <p>
 * NOTE: In order to properly implement a specialized extension mirror you:
 * <ul>
 * <li>Must override {@link Extension#newExtensionMirror()} in order to provide a new instance of the mirror.</li>
 * <li>Must place the mirror in the same module as the extension itself (iff the extension is defined in a module).</li>
 * <li>Should name the mirror class {@code $NAME_OF_EXTENSION$}Mirror.</li>
 * </ul>
 * 
 * @param <E>
 *            The type of extension this mirror is a part of.
 * 
 * @see ApplicationMirror#useExtension(Class)
 * @see ContainerMirror#useExtension(Class)
 * @see Extension#newExtensionMirror()
 */
public class ExtensionMirror<E extends Extension<E>> implements Mirror {

    /*
     * When naming methods in this class try to avoid using trivial names such as {@code name}, {@code type}, {@code stream}
     * as sub-classes might want to make use of such names.
     * 
     * This class contains a number of all* methods. There are no exact criteria for what methods to include. Only that they
     * should generally helpful for people writing extension mirrors.
     */

    /**
     * The extensions that are being mirrored. Is initially null but populated via {@link #initialize(PackedExtensionTree)}
     */
    @Nullable
    private PackedExtensionTree<E> extensions;

    /**
     * Create a new extension mirror.
     * <p>
     * Subclasses should have a single constructor with package-private access.
     * <p>
     * Attempting to use any of the methods on this class from the constructor of a subclass, will result in an
     * {@link IllegalStateException} being thrown.
     */
    protected ExtensionMirror() {}

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
        for (E t : extensionTree()) {
            action.accept(t, result);
        }
        return result;
    }

    protected final void allForEach(Consumer<E> action) {
        requireNonNull(action, "action is null");
        for (E t : extensionTree()) {
            action.accept(t);
        }
    }

    /** {@return a non-empty stream of all of the extension instances we are mirroring.} */
    protected final Stream<E> allStream() {
        return extensionTree().stream();
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
        for (E t : extensionTree()) {
            int tmp = mapper.applyAsInt(t);
            result = Math.addExact(result, tmp);
        }
        return result;
    }

    protected final long allSumLong(ToLongFunction<? super E> mapper) {
        requireNonNull(mapper, "mapper is null");
        long result = 0;
        for (E t : extensionTree()) {
            long tmp = mapper.applyAsLong(t);
            result = Math.addExact(result, tmp);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return other instanceof ExtensionMirror<?> m && getClass() == m.getClass() && extensionTree().equals(m.extensionTree());
    }

    /** {@return a descriptor for the extension this mirror is a part of.} */
    public final ExtensionDescriptor extensionDescriptor() {
        return extensionTree().extension().model;
    }

    /** {@return the full name of the extension.} */
    public final String extensionFullName() {
        return extensionDescriptor().fullName();
    }

    /** {@return the name of the extension.} */
    public final String extensionName() {
        return extensionDescriptor().name();
    }

    protected final E extensionRoot() {
        return extensionTree().root();
    }

    /** {@return all the extensions that are being mirrored.} */
    protected final ExtensionTree<E> extensions() {
        return extensionTree();
    }

    /**
     * {@return the mirrored extension's internal configuration.}
     * 
     * @throws InternalExtensionException
     *             if called from the constructor of the mirror
     */
    private PackedExtensionTree<E> extensionTree() {
        PackedExtensionTree<E> e = extensions;
        if (e == null) {
            throw new InternalExtensionException(
                    "Either this method has been called from the constructor of the mirror. Or an extension forgot to invoke Extension#mirrorInitialize.");
        }
        return e;
    }

    /** {@return the type of extension this mirror is a part of.} */
    public final Class<? extends Extension<?>> extensionType() {
        return extensionTree().extension().extensionType;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return extensionTree().hashCode();
    }

    /**
     * Invoked by a MethodHandle from ExtensionMirrorHelper to set the internal configuration of the extension.
     * 
     * @param extension
     *            the internal configuration of the extension to mirror
     */
    final void initialize(PackedExtensionTree<E> extensions) {
        if (this.extensions != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.extensions = requireNonNull(extensions);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return extensionType().getCanonicalName();
    }
}
