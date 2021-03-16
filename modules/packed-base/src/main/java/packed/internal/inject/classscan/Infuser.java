package packed.internal.inject.classscan;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.inject.FindInjectableConstructor;
import packed.internal.util.LookupUtil;
import packed.internal.util.TypeUtil;

public class Infuser {

    /** The entries of this infuser */
    final Map<Key<?>, Entry> entries;

    private final List<Class<?>> parameterTypes;

    private final Lookup lookup;

    private Infuser(Builder builder) {
        this.entries = Map.copyOf(builder.entries);
        this.parameterTypes = requireNonNull(builder.parameterTypes);
        this.lookup = builder.lookup;
    }

    public Set<Key<?>> keys() {
        return entries.keySet();
    }

    public int parameterCount() {
        return parameterTypes.size();
    }

    public MethodHandle findConstructorFor(Class<?> type) {
        TypeUtil.checkClassIsInstantiable(type);
        ClassMemberAccessor oc = ClassMemberAccessor.of(lookup, type);
        MethodHandleBuilder mhb = MethodHandleBuilder.of(type, parameterTypes);
        mhb.add(this);
        Constructor<?> constructor = FindInjectableConstructor.findConstructorIAE(type);
        return mhb.build(oc, constructor);
    }

    public List<Class<?>> parameterTypes() {
        return parameterTypes;
    }

    public Infuser withExposed(Class<?> key, Consumer<? extends EntryBuilder> action) {
        throw new UnsupportedOperationException();
    }

    public static Infuser build(MethodHandles.Lookup lookup, Consumer<? super Infuser.Builder> action, Class<?>... parameterTypes) {
        requireNonNull(action, "action is null");
        Infuser.Builder b = new Infuser.Builder(lookup, parameterTypes);
        action.accept(b);
        return b.build();
    }

    public static Infuser of(MethodHandles.Lookup lookup) {
        return build(lookup, c -> {});
    }

    public static Builder builder(MethodHandles.Lookup lookup, Class<?>... parameterTypes) {
        return new Builder(lookup, parameterTypes);
    }

    public static class Builder {
        private final HashMap<Key<?>, Entry> entries = new HashMap<>();
        private final Lookup lookup;
        private final List<Class<?>> parameterTypes;

        Builder(Lookup lookup, Class<?>... parameterTypes) {
            this.lookup = requireNonNull(lookup, "lookup is null");
            this.parameterTypes = List.of(parameterTypes);
        }

        public Infuser build() {
            return new Infuser(this);
        }

        public EntryBuilder provide(Class<?> key) {
            return provide(Key.of(key));
        }

        public EntryBuilder provide(Key<?> key) {
            return new EntryBuilder(this, key, false);
        }

        public EntryBuilder provideHidden(Class<?> key) {
            return provideHidden(Key.of(key));
        }

        public EntryBuilder provideHidden(Key<?> key) {
            return new EntryBuilder(this, key, true);
        }

        private void add(EntryBuilder builder, Entry entry) {
            entries.put(builder.key, entry);
        }
    }

    public static class EntryBuilder {
        private final Builder builder;
        private final boolean hide;
        private final Key<?> key;

        EntryBuilder(Builder builder, Key<?> key, boolean hide) {
            this.builder = builder;
            this.key = requireNonNull(key, "key is null");
            this.hide = hide;
        }

        public EntryBuilder description(String description) {
            // Ideen er vi propper lidt descriptions paa igen, IDK
            return this;
        }

        /**
         * The service will be provided by adapting the infuser's first (index 0) parameter to the raw type of the key.
         * <p>
         * It does so by automatically inserting casts when needed
         * 
         * @throws IndexOutOfBoundsException
         *             if the the infuser has no parameters
         */
        public void adapt() {
            adapt(0);
        }

        public void adapt(int index) {
            Objects.checkFromIndexSize(index, 0, builder.parameterTypes.size());
            builder.add(this, new Entry(null, hide, index));
        }

        public void extract(String methodName /* , Object... additional(Static)Arguments */ ) {
            extract(methodName, 0);
        }

        public void extract(String methodName, int index) {
            Class<?> cl = builder.parameterTypes.get(index);
            // We probably want to make our own call... This one throws java.lang.ExceptionInInitializerError
            MethodHandle mh = LookupUtil.lookupVirtualPrivate(builder.lookup, cl, methodName, key.rawType());
            transform(mh, index);
        }

        public void extractPublic(String methodName /* , Object... additional(Static)Arguments */ ) {
            extractPublic(methodName, 0);
        }

        public void extractPublic(String methodName, int index) {
            Class<?> cl = builder.parameterTypes.get(index);
            // We probably want to make our own call... This one throws java.lang.ExceptionInInitializerError
            MethodHandle mh = LookupUtil.lookupVirtualPublic(cl, methodName, key.rawType());
            transform(mh, index);
        }

        public void transform(MethodHandle transformer) {
            transform(transformer, 0);
        }

        public void transform(MethodHandle transformer, int... indexes) {
            requireNonNull(transformer, "transformer is null");
            for (int i = 0; i < indexes.length; i++) {
                Objects.checkFromIndexSize(indexes[i], 0, builder.parameterTypes.size());
            }
            builder.add(this, new Entry(transformer, hide, indexes));
        }
    }

    record Entry(@Nullable MethodHandle transformer, boolean isHidden, int... indexes) {}
}
