package packed.internal.util;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.extension.InternalExtensionException;

public final /* primitive */ class TmpLoader<T> {

    private final Class<?> baseType;

    private final ClassValue<Entry<T>> entries = new ClassValue<>() {

        @Override
        protected Entry<T> computeValue(Class<?> type) {
            @SuppressWarnings("unchecked")
            T t = factory.apply((Class<? extends T>) type);
            return new Entry<>(t);
        }
    };

    private final Function<Class<? extends T>, T> factory;

    public TmpLoader(Class<?> baseType, Function<Class<? extends T>, T> factory) {
        this.baseType = requireNonNull(baseType);
        this.factory = requireNonNull(factory);
    }

    public final T initializeClass(Class<?> type) {
        // Ensure that the class initializer of the extension has been run before we progress
        try {
            baseType.getModule().addReads(type.getModule());
            Lookup l = MethodHandles.privateLookupIn(type, MethodHandles.lookup());
            l.ensureInitialized(type);
        } catch (IllegalAccessException e) {
            // TODO this is likely the first place we check the extension is readable to Packed
            // Better error message..
            // Maybe we have other stuff that we need to check here...
            // We need to be open.. In order to create the extension...
            // So probably no point in just checking for Readable...
            throw new InternalExtensionException(type + " is not readable for Packed", e);
        }

        Entry<T> e = entries.get(type);
        synchronized (e) {
            T t = e.t;
            assert t != null;
            e.t = null;
            return t;
        }
    }

    public final void update(Class<?> type, Consumer<? super T> consumer) {
      //  ClassUtil.checkProperSubclass(baseType, type);
        Entry<T> e = entries.get(type);
        synchronized (e) {
            T t = e.t;
            if (t != null) {
                consumer.accept(t);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private static class Entry<T> {
        T t;

        private Entry(T t) {
            this.t = t;
        }

    }
}
