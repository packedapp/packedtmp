package packed.internal.container;

import java.util.NoSuchElementException;

import app.packed.base.Nullable;
import app.packed.container.ExtensionAncestor;

public class PackedExtensionAncestor<E> implements ExtensionAncestor<E> {

    @Nullable
    final E instance;

    PackedExtensionAncestor(@Nullable E instance) {
        this.instance = instance;
    }

    @Override
    public E get() {
        if (instance == null) {
            throw new NoSuchElementException();
        }
        return instance;
    }

    @Override
    public boolean isParent() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPresent() {
        return instance != null;
    }

    @Override
    public boolean isSameApplication() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isStronglyWired() {
        return false;
    }

    @Override
    public void onUninstall(Runnable r) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public static <E> ExtensionAncestor<E> sameApplication(Class<E> type, Object instance) {
        if (!type.isInstance(instance)) {
            throw new ClassCastException("An ancestor of type " + instance.getClass() + " was found, but it is not assignable to the specified type " + type);
        }
        return new PackedExtensionAncestor<E>((E) instance);
    }

    public static <E> ExtensionAncestor<E> missing() {
        return new PackedExtensionAncestor<E>(null);
    }
}
