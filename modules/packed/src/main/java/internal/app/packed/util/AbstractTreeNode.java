package internal.app.packed.util;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import app.packed.util.Nullable;

/**
 * A node in a tree.
 * <p>
 * Only {@link java.util.LinkedHashMap} you can go from one sibling to another and back again.
 */
public abstract class AbstractTreeNode<T extends AbstractTreeNode<T>> {

    /** The (nullable) first child of the node. */
    @Nullable
    public T treeFirstChild;

    /** The (nullable) last child of the node. */
    @Nullable
    T treeLastChild; // not exposed currently, as there are currently no use cases

    /** The (nullable) sibling of the node. */
    @Nullable
    public T treeNextSibling;

    /** Any parent this node may have. Only the root node does not have a parent. */
    @Nullable
    public final T treeParent;

    @SuppressWarnings("unchecked")
    protected AbstractTreeNode(@Nullable T treeParent) {
        this.treeParent = treeParent;
        if (treeParent != null) {
            // Tree maintenance
            if (treeParent.treeFirstChild == null) {
                treeParent.treeFirstChild = (T) this;
            } else {
                treeParent.treeLastChild.treeNextSibling = (T) this;
            }
            treeParent.treeLastChild = (T) this;
        }
    }

    /** {@return the depth of the node, with the root node having depth 0} */
    public final int depth() {
        int depth = 0;
        for (AbstractTreeNode<T> node = this; node.treeParent != null; node = node.treeParent) {
            depth++;
        }
        return depth;
    }

    public final T root() {
        @SuppressWarnings("unchecked")
        T t = (T) this;
        while (t.treeParent != null) {
            t = t.treeParent;
        }
        return t;
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

 // Returns an iterator over the children of this node
    public Iterator<T> childIterator() {
        return new Iterator<>() {
            private T current = treeFirstChild;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                T result = current;
                // Move to the next sibling
                current = ((AbstractTreeNode<T>) current).treeNextSibling;
                return result;
            }
        };
    }
    /**
     * Returns a lazy stream that includes this node and all its children in pre-order.
     *
     * @return a pre-order stream of the nodes
     */
    public final Stream<T> stream() {
        Iterator<T> iterator = new PreOrderIterator<>(self());

        // Convert the iterator to a Spliterator with appropriate characteristics (ORDERED, NONNULL)
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    /**
     * Returns a lazy stream that includes this node and its descendants in pre-order, excluding any node and its subtree if
     * it does not satisfy the given predicate.
     *
     * @param predicate
     *            the predicate to apply to nodes
     * @return a filtered pre-order stream of the nodes
     */
    public final Stream<T> stream(Predicate<? super T> predicate) {
        Iterator<T> iterator = new FilteredPreOrderIterator<>(self(), predicate);

        // Convert the iterator to a Spliterator with appropriate characteristics (ORDERED, NONNULL)
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    /** A pre-order iterator for a rooted tree. */
    private static final class PreOrderIterator<T extends AbstractTreeNode<T>> implements Iterator<T> {

        /** The next node to process, or null if no more nodes are left. */
        @Nullable
        private T next;

        /** The root node of the tree being iterated over. */
        private final T root;

        private PreOrderIterator(T root) {
            this.root = this.next = root;
        }

        /**
         * Recursively finds the next sibling or the parent's next sibling if this node has no siblings.
         *
         * @param current
         *            the current node
         * @return the next sibling, or null if there is none
         */
        private T findNextSibling(T current) {
            requireNonNull(current);
            if (current.treeNextSibling != null) {
                return current.treeNextSibling;
            }
            T parent = current.treeParent;
            if (parent == root || parent == null) {
                return null;
            } else {
                return findNextSibling(parent);
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return next != null;
        }

        /** {@inheritDoc} */
        @Override
        public T next() {
            T n = next;
            if (n == null) {
                throw new NoSuchElementException();
            }

            // If there's a first child, go to it
            if (n.treeFirstChild != null) {
                next = n.treeFirstChild;
            } else {
                // Otherwise, find the next sibling or ascend the tree to find the next node
                next = findNextSibling(n);
            }

            return n;
        }
    }

    /**
     * A pre-order iterator that skips entire subtrees of nodes that do not satisfy a given predicate.
     */
    private static final class FilteredPreOrderIterator<T extends AbstractTreeNode<T>> implements Iterator<T> {

        /** The predicate to apply to each node. */
        private final Predicate<? super T> predicate;

        /** The root node of the tree being iterated over. */
        private final T root;

        /** The next node to process, or null if no more nodes are left. */
        @Nullable
        private T next;

        private FilteredPreOrderIterator(T root, Predicate<? super T> predicate) {
            this.predicate = requireNonNull(predicate);
            this.root = root;
            this.next = root;

            // If the root node does not satisfy the predicate, find the next eligible node
            if (!predicate.test(root)) {
                this.next = findNextSiblingOrAncestorSibling(root);
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return next != null;
        }

        /** {@inheritDoc} */
        @Override
        public T next() {
            T current = next;
            if (current == null) {
                throw new NoSuchElementException();
            }

            // Advance to the next node
            next = findNext(current);

            return current;
        }

        /**
         * Finds the next node to process after the current node.
         *
         * @param current
         *            the current node
         * @return the next node to process, or null if there are no more nodes
         */
        private T findNext(T current) {
            // Try to move to the first child
            T child = current.treeFirstChild;
            if (child != null) {
                if (predicate.test(child)) {
                    return child;
                } else {
                    // Skip child's subtree
                    return findNextSiblingOrAncestorSibling(child);
                }
            } else {
                // No child, move to next sibling or ancestor's next sibling
                return findNextSiblingOrAncestorSibling(current);
            }
        }

        /**
         * Finds the next sibling or ancestor's sibling that satisfies the predicate.
         *
         * @param node
         *            the node to start from
         * @return the next eligible node, or null if none
         */
        private T findNextSiblingOrAncestorSibling(T node) {
            while (true) {
                T sibling = node.treeNextSibling;
                if (sibling != null) {
                    if (predicate.test(sibling)) {
                        return sibling;
                    } else {
                        // Skip sibling's subtree
                        node = sibling;
                        continue; // try to find sibling's sibling
                    }
                } else {
                    // No sibling, move up to parent
                    node = node.treeParent;
                    if (node == null || node == root) {
                        return null;
                    }
                }
            }
        }
    }
}
