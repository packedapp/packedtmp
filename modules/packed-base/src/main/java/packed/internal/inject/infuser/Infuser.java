package packed.internal.inject.infuser;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import app.packed.base.Key;
import app.packed.base.Nullable;

public class Infuser {

    /** The entries of this infuser */
    final Map<Key<?>, Entry> entries;

    private final List<Class<?>> parameterTypes;

    @Nullable
    private final Infuser parent = null;

    private Infuser(Builder builder) {
        this.entries = Map.copyOf(builder.entries);
        this.parameterTypes = requireNonNull(builder.parameterTypes);
    }

    public Builder addTo(Class<?>... additionalParameters) {
        // Adds key
        throw new UnsupportedOperationException();
    }

    public Set<Key<?>> keys() {
        return entries.keySet();
    }

    public int parameterCount() {
        return parameterTypes.size();
    }

    public List<Class<?>> parameterTypes() {
        return parameterTypes;
    }

    public Infuser withDirect(Class<?> key, int index) {
        throw new UnsupportedOperationException();
    }

    public static Builder builder(Class<?>... parameterTypes) {
        return new Builder(parameterTypes);
    }

    public static class Builder {
        private final HashMap<Key<?>, Entry> entries = new HashMap<>();

        private final List<Class<?>> parameterTypes;

        Builder(Class<?>... parameterTypes) {
            this.parameterTypes = List.of(parameterTypes);
        }

        private void add(Key<?> key, boolean isHidden, MethodHandle transformer, int... indexes) {
            requireNonNull(key, "key is null");
            for (int i = 0; i < indexes.length; i++) {
                Objects.checkFromIndexSize(indexes[i], 0, parameterTypes.size());
            }
            // We might allow to override.. for example if we do not have parent infusers.
            // In which case it will just override parent keys...
            if (entries.putIfAbsent(key, new Entry(transformer, indexes, isHidden)) != null) {
                throw new IllegalArgumentException("The specified key " + key + " has already been added");
            }
        }

        public void direct(Class<?> key, int index) {
            direct(key, index);
        }

        public void direct(Key<?> key, int index) {
            add(key, false, null, index);
        }

        public void directHidden(Class<?> key, int index) {
            directHidden(Key.of(key), index);
        }

        public void directHidden(Key<?> key, int index) {
            add(key, true, null, index);
        }

        public void transform(Class<?> key, MethodHandle transformer, int... indexes) {
            transform(Key.of(key), transformer, indexes);
        }

        public void transform(Key<?> key, MethodHandle transformer, int... indexes) {
            requireNonNull(transformer, "transformer is null");
            add(key, false, transformer, indexes);
        }

        public void transformHidden(Class<?> key, MethodHandle transformer, int... indexes) {
            transformHidden(Key.of(key), transformer, indexes);
        }

        public void transformHidden(Key<?> key, MethodHandle transformer, int... indexes) {
            requireNonNull(transformer, "transformer is null");
            add(key, true, transformer, indexes);
        }
    }

    record Entry(@Nullable MethodHandle transformer, int[] indexes, boolean isHidden) {}
}
