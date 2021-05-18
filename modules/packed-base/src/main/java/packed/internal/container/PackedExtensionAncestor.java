package packed.internal.container;

import app.packed.container.ExtensionAncestor;

public class PackedExtensionAncestor<E> implements ExtensionAncestor<E> {

    @Override
    public E get() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isParent() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPresent() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSameApplication() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isStronglyWired() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onUninstall(Runnable r) {
        // TODO Auto-generated method stub

    }

    static <E> ExtensionAncestor<E> sameApplication(E e) {
        throw new UnsupportedOperationException();
    }

    static <E> ExtensionAncestor<E> missing() {
        throw new UnsupportedOperationException();
    }
}
