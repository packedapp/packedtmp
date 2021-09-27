package packed.internal.bundle;

import java.util.NoSuchElementException;

import app.packed.base.Nullable;
import app.packed.extension.old.ExtensionBeanConnection;

public class PackedExtensionAncestor<E> implements ExtensionBeanConnection<E> {

    @Nullable
    final E instance;

    PackedExtensionAncestor(@Nullable E instance) {
        this.instance = instance;
    }

    @Override
    public E instance() {
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
    public boolean isInSameApplication() {
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
    public static <E> ExtensionBeanConnection<E> sameApplication(Object instance) {
        return new PackedExtensionAncestor<E>((E) instance);
    }

    public static <E> ExtensionBeanConnection<E> missing() {
        return new PackedExtensionAncestor<E>(null);
    }
}
