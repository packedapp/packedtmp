package packed.internal.inject.invoke;

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
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.util.MemberScanner;
import packed.internal.util.OpenClass;

// MemberInjector
public final class Infuser {

    /** The services provided by this infuser. */
    final Map<Key<?>, Entry> services;

    private final Lookup lookup;

    private final List<Class<?>> parameterTypes;

    private Infuser(Builder builder) {
        this.services = Map.copyOf(builder.services);
        this.parameterTypes = requireNonNull(builder.parameterTypes);
        this.lookup = builder.lookup;
    }

    public Set<Key<?>> keys() {
        return services.keySet();
    }

    public int parameterCount() {
        return parameterTypes.size();
    }

    public List<Class<?>> parameterTypes() {
        return parameterTypes;
    }

    private MethodHandle singleConstructor(Class<?> type, Class<?> returnType, Function<String, RuntimeException> errorMaker) {
        // First lets find a constructor
        Constructor<?> constructor = MemberScanner.getConstructor(type, false, errorMaker);

        OpenClass oc = OpenClass.of(lookup, type);
        MethodHandleBuilder mhb = MethodHandleBuilder.of(type, parameterTypes);
        mhb.add(this);
        MethodHandle mh = mhb.build(oc, constructor);

        // We need to adapt the method handle
        return mh.asType(mh.type().changeReturnType(returnType));
    }

    public static Builder builder(MethodHandles.Lookup caller, Class<?> clazz, Class<?>... parameterTypes) {
        return new Builder(caller, clazz, parameterTypes);
    }

    public static class Builder {
        private final Class<?> clazz;
        private final LinkedHashMap<Key<?>, Entry> services = new LinkedHashMap<>();
        private final Lookup lookup;
        private final List<Class<?>> parameterTypes;

        Builder(Lookup caller, Class<?> clazz, Class<?>... parameterTypes) {
            this.lookup = requireNonNull(caller, "caller is null");
            this.clazz = requireNonNull(clazz);
            this.parameterTypes = List.of(parameterTypes);
        }

        private void add(ServiceEntry builder, Entry entry) {
            services.put(builder.key, entry);
        }

        public MethodHandle findConstructor(Class<?> returnType, Function<String, RuntimeException> errorMaker) {
            Infuser infuser = new Infuser(this);
            return infuser.singleConstructor(clazz, returnType, errorMaker);
        }

        public ServiceEntry provide(Class<?> key) {
            return provide(Key.of(key));
        }

        public ServiceEntry provide(Key<?> key) {
            return new ServiceEntry(this, key, false);
        }

        // Altsaa vi goer det jo lidt pga classpath'en
        public ServiceEntry provideHidden(Class<?> key) {
            return provideHidden(Key.of(key));
        }

        public ServiceEntry provideHidden(Key<?> key) {
            return new ServiceEntry(this, key, true);
        }
        
        /** A builder for key based entry in the infuser. */
        public static class ServiceEntry {
            private final Builder builder;
            private final boolean hidden;
            private final Key<?> key;

            ServiceEntry(Builder builder, Key<?> key, boolean hide) {
                this.builder = builder;
                this.key = requireNonNull(key, "key is null");
                this.hidden = hide;
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
                builder.add(this, new Entry(this, null, checkIndex(index)));
            }

            private int checkIndex(int index) {
                return Objects.checkFromIndexSize(index, 0, builder.parameterTypes.size());
            }

            public ServiceEntry description(String description) {
                // Ideen er vi propper lidt descriptions paa igen, IDK
                // Ikke i foerst omgang taenker jeg
                return this;
            }

            public void invokeExact(MethodHandle methodHandle, int index) {
                requireNonNull(methodHandle, "methodHandle is null");
                Objects.checkFromIndexSize(index, 0, builder.parameterTypes.size());
                // Don't currently use it, we can add it again if we need it
//                for (int i = 0; i < additionalIndexes.length; i++) {
//                    Objects.checkFromIndexSize(additionalIndexes[i], 0, builder.parameterTypes.size());
//                }
                // System.out.println("Adding transfoer " + transformer);
                builder.add(this, new Entry(this, methodHandle, index));
            }
        }
    }



    record Entry(@Nullable MethodHandle transformer, boolean isHidden, int... indexes) {
        Entry(Builder.ServiceEntry b, @Nullable MethodHandle transformer, int... indexes) {
            this(transformer, b.hidden,  indexes);
        }
    }
}
//public void invokeMethod(String methodName, int index) {
//Class<?> cl = builder.parameterTypes.get(index);
//// We probably want to make our own call... This one throws java.lang.ExceptionInInitializerError
//MethodHandle mh = LookupUtil.lookupVirtualPrivate(builder.lookup, cl, methodName, key.rawType());
//invokeExact(mh, index);
//}
//
//public void invokePublicMethod(String methodName, int index) {
//Class<?> cl = builder.parameterTypes.get(index);
//// We probably want to make our own call... This one throws java.lang.ExceptionInInitializerError
//MethodHandle mh = LookupUtil.lookupVirtualPublic(cl, methodName, key.rawType());
//invokeExact(mh, index);
//}