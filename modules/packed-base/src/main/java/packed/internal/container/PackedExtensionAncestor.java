package packed.internal.container;

import java.util.NoSuchElementException;

import app.packed.base.Nullable;
import app.packed.extension.ExtensionConnection;
import app.packed.extension.ExtensionMember;

public class PackedExtensionAncestor<E extends ExtensionMember<?>> implements ExtensionConnection<E> {

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
    public static <E extends ExtensionMember<?>> ExtensionConnection<E> sameApplication(Object instance) {
        return new PackedExtensionAncestor<E>((E) instance);
    }

    public static <E extends ExtensionMember<?>> ExtensionConnection<E> missing() {
        return new PackedExtensionAncestor<E>(null);
    }
}
