package packed.internal.invoke;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.inject.FindInjectableConstructor;
import packed.internal.util.LookupUtil;

public final class Infuser {

    /** The entries of this infuser */
    final Map<Key<?>, Entry> entries;

    private final Lookup lookup;

    private final List<Class<?>> parameterTypes;

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

    public List<Class<?>> parameterTypes() {
        return parameterTypes;
    }

    @Deprecated
    public MethodHandle singleConstructor(Class<?> type, Class<?> returnType, Function<String, RuntimeException> errorMaker) {
        // First lets find a constructor
        Constructor<?> constructor = FindInjectableConstructor.get(type, false, errorMaker);

        ClassMemberAccessor oc = ClassMemberAccessor.of(lookup, type);
        MethodHandleBuilder mhb = MethodHandleBuilder.of(type, parameterTypes);
        mhb.add(this);
        MethodHandle mh = mhb.build(oc, constructor);

        // We need to adapt the method handle
        return mh.asType(mh.type().changeReturnType(returnType));
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

    public static Builder builder(MethodHandles.Lookup caller, Class<?>... parameterTypes) {
        return new Builder(caller, parameterTypes);
    }

    public static Infuser of(MethodHandles.Lookup lookup) {
        return build(lookup, c -> {});
    }

    public static class Builder {
        private final LinkedHashMap<Key<?>, Entry> entries = new LinkedHashMap<>();
        private final Lookup lookup;
        private final List<Class<?>> parameterTypes;

        Builder(Lookup caller, Class<?>... parameterTypes) {
            this.lookup = requireNonNull(caller, "caller is null");
            this.parameterTypes = List.of(parameterTypes);
        }

        private void add(EntryBuilder builder, Entry entry) {
            entries.put(builder.key, entry);
        }

        public Infuser build() {
            return new Infuser(this);
        }

        public MethodHandle findConstructor(Class<?> type, Class<?> returnType, Function<String, RuntimeException> errorMaker) {
            Infuser infuser = build();
            return infuser.singleConstructor(type, returnType, errorMaker);
        }

        // Ville vaere dejligt med en forklaring paa hvornaar den er tilgaengelig
        public EntryBuilder optional(Class<?> key) {
            return optional(Key.of(key));
        }

        public EntryBuilder optional(Key<?> key) {
            return new EntryBuilder(this, key, false, true);
        }

        public EntryBuilder provide(Class<?> key) {
            return provide(Key.of(key));
        }

        public EntryBuilder provide(Key<?> key) {
            return new EntryBuilder(this, key, false, false);
        }

        public EntryBuilder provideHidden(Class<?> key) {
            return provideHidden(Key.of(key));
        }

        public EntryBuilder provideHidden(Key<?> key) {
            return new EntryBuilder(this, key, true, false);
        }
    }

    /** A builder for key based entry in the infuser. */
    public static class EntryBuilder {
        private final Builder builder;
        private final boolean hide;
        private final Key<?> key;
        private final boolean optional;

        EntryBuilder(Builder builder, Key<?> key, boolean hide, boolean isOptional) {
            this.builder = builder;
            this.key = requireNonNull(key, "key is null");
            this.hide = hide;
            this.optional = isOptional;
        }

        /**
         * The service will be provided by adapting the infuser's indexed parameter to the raw type of the key.
         * <p>
         * It does so by automatically inserting casts when needed
         * 
         * @param index
         *            the index of the argument to adapt
         * @throws IndexOutOfBoundsException
         *             if the the infuser has no parameters
         */
        public void adaptArgument(int index) {
            Objects.checkFromIndexSize(index, 0, builder.parameterTypes.size());
            builder.add(this, new Entry(this, null, index));
        }

        public void byInvoking(MethodHandle methodHandle) {
            // Vil lave indexes om saa de skal match istedet for at tage den foerste..
            byInvoking(methodHandle, 0);
        }

        public void byInvoking(MethodHandle methodHandle, int... indexes) {
            requireNonNull(methodHandle, "methodHandle is null");
            for (int i = 0; i < indexes.length; i++) {
                Objects.checkFromIndexSize(indexes[i], 0, builder.parameterTypes.size());
            }
            // System.out.println("Adding transfoer " + transformer);
            builder.add(this, new Entry(this, methodHandle, indexes));
        }

        public EntryBuilder description(String description) {
            // Ideen er vi propper lidt descriptions paa igen, IDK
            return this;
        }

        public void invokeMethod(String methodName /* , Object... additional(Static)Arguments */ ) {
            invokeMethod(methodName, 0);
        }

        public void invokeMethod(String methodName, int index) {
            Class<?> cl = builder.parameterTypes.get(index);
            // We probably want to make our own call... This one throws java.lang.ExceptionInInitializerError
            MethodHandle mh = LookupUtil.lookupVirtualPrivate(builder.lookup, cl, methodName, key.rawType());
            byInvoking(mh, index);
        }

        public void invokePublicMethod(String methodName /* , Object... additional(Static)Arguments */ ) {
            invokePublicMethod(methodName, 0);
        }

        public void invokePublicMethod(String methodName, int index) {
            Class<?> cl = builder.parameterTypes.get(index);
            // We probably want to make our own call... This one throws java.lang.ExceptionInInitializerError
            MethodHandle mh = LookupUtil.lookupVirtualPublic(cl, methodName, key.rawType());
            byInvoking(mh, index);
        }
    }

    record Entry(@Nullable MethodHandle transformer, boolean isHidden, boolean isOptional, int... indexes) {
        Entry(EntryBuilder b, @Nullable MethodHandle transformer, int... indexes) {
            this(transformer, b.hide, b.optional, indexes);
        }

    }
}
