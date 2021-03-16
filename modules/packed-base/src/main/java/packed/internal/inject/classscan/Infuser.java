package packed.internal.inject.classscan;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.util.LookupUtil;

public class Infuser {

    /** The entries of this infuser */
    final Map<Key<?>, Entry> entries;

    private final List<Class<?>> parameterTypes;

    private Infuser(Builder builder) {
        this.entries = Map.copyOf(builder.entries);
        this.parameterTypes = requireNonNull(builder.parameterTypes);
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

    public static Infuser build(Consumer<? super Infuser.Builder> action, Class<?>... parameterTypes) {
        requireNonNull(action, "action is null");
        Infuser.Builder b = new Infuser.Builder(parameterTypes);
        action.accept(b);
        return b.build();
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
            if (indexes.length == 0) {
                if (parameterTypes.size() != 1) {
                    throw new IllegalArgumentException("Must specify an index if the infuser has more than 1 parameter");
                }
                indexes = new int[] { 0 };
            }
            for (int i = 0; i < indexes.length; i++) {
                Objects.checkFromIndexSize(indexes[i], 0, parameterTypes.size());
            }
            // We might allow to override.. for example if we do not have parent infusers.
            // In which case it will just override parent keys...
            if (entries.putIfAbsent(key, new Entry(transformer, isHidden, indexes)) != null) {
                throw new IllegalArgumentException("The specified key " + key + " has already been added");
            }
        }

        public EntryBuilder expose(Class<?> key) {
            return expose(Key.of(key));
        }

        public EntryBuilder expose(Key<?> key) {
            return new EntryBuilder(this, key, false);
        }

        public EntryBuilder hide(Class<?> key) {
            return expose(Key.of(key));
        }

        public EntryBuilder hide(Key<?> key) {
            return new EntryBuilder(this, key, true);
        }

        public Infuser build() {
            return new Infuser(this);
        }
    }

    public static class EntryBuilder {
        private final Builder builder;
        private final Key<?> key;
        private final boolean hide;

        EntryBuilder(Builder builder, Key<?> key, boolean hide) {
            this.builder = builder;
            this.key = requireNonNull(key, "key is null");
            this.hide = hide;
        }

        public void extract(MethodHandles.Lookup lookup, String methodName /* , Object... additional(Static)Arguments */ ) {
            extract(lookup, methodName, 0);
        }

        public void extract(MethodHandles.Lookup lookup, String methodName, int index) {
            Class<?> cl = builder.parameterTypes.get(index);
            // We probably want to make our own call... This one throws java.lang.ExceptionInInitializerError
            MethodHandle mh = LookupUtil.lookupVirtualPrivate(lookup, cl, methodName, key.rawType());
            transform(mh, index);
        }

        public void transform(MethodHandle transformer) {
            transform(transformer, 0);
        }

        public void transform(MethodHandle transformer, int... indexes) {
            requireNonNull(transformer, "transformer is null");
            System.out.println("OK " + transformer);
        }

        public void cast() {
            cast(0);
        }

        public void cast(int index) {
            Objects.checkFromIndexSize(index, 0, builder.parameterTypes.size());
            builder.entries.put(key, new Entry(null, hide, index));
        }
    }

    record Entry(@Nullable MethodHandle transformer, boolean isHidden, int... indexes) {}
}
