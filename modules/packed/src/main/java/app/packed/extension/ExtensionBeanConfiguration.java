/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import app.packed.bean.BeanHandle;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.binding.Key;
import app.packed.framework.Nullable;
import app.packed.operation.OperationHandle;

/**
 *
 */
// Her siger vi at extension beans er instance beans... Det behoever de vel ikke at vaere
public class ExtensionBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    static final Key<MethodHandle[]> KEY_MH_ARRAY = Key.of(MethodHandle[].class);

    @Nullable
    NewX<?> node;

    /**
     * @param handle
     */
    public ExtensionBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
    }

    @SuppressWarnings("unchecked")
    <X extends NewX<?>> X getOrCreate(Class<X> type, Key<?> key, MethodType mt, BiFunction<MethodType, Key<?>, X> supplier) {
        NewX<?> n = node;
        while (n != null) {
            if (n.key.equals(key)) {
                // check same type
                // check same Method Type
            }
            n = n.next;
        }
        n = supplier.apply(mt, key);
        n.next = node;
        return (X) n;
    }

    // Problemet 
    public <K> ExtensionBeanConfiguration<T> initializeWith(Class<K> key, Supplier<? extends K> supplier) {
        return initializeWith(Key.of(key), supplier);
    }

    public <K> ExtensionBeanConfiguration<T> initializeWith(Key<K> key, Supplier<? extends K> supplier) {
        return this;
    }

    public <C> void initializeWithClassifiedMethodHandle(OperationHandle handle, Class<C> classifierType, C value) {}

    public <C> void initializeWithClassifiedMethodHandle(OperationHandle handle, Class<C> classifierType, C value, Key<MethodHandle> key) {}

    public <K> void initializeWithMethodHandle(OperationHandle handle) {
        initializeWithMethodHandle(handle, Key.of(MethodHandle.class));
    }

    public <K> void initializeWithMethodHandle(OperationHandle handle, Key<MethodHandle> key) {
        // Tror det er en god ide saa kan jeg skjule invokeFrom...
        // Og saa maaske bare have en relativizeTo
        initializeWith(key, () -> handle.generateMethodHandle());
    }

    public int initializeWithIntClassifierMethodHandle(OperationHandle handle) {
        // First argument of the MethodHandle will now take an int...

        // Tror det er en god ide saa kan jeg skjule invokeFrom...
        // Og saa maaske bare have en relativizeTo
        return 0;
    }

    public int initializeWithIntClassifierMethodHandle(OperationHandle handle, Key<MethodHandle> key) {
        requireNonNull(key, "key is null");
        AutoXClassifier axc = getOrCreate(AutoXClassifier.class, key, handle.invocationType(), AutoXClassifier::new);
        axc.operations.add(handle);
        return axc.operations.size() - 1;
    }

    static final class AutoXClassifier extends NewX<MethodHandle[]> {

        private final ArrayList<OperationHandle> operations = new ArrayList<>();

        /**
         * @param methodType
         * @param key
         */
        AutoXClassifier(MethodType methodType, Key<?> key) {
            super(methodType, key);
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle[] get() {
            MethodHandle[] mhs = new MethodHandle[operations.size()];
            for (int i = 0; i < mhs.length; i++) {
                mhs[i] = operations.get(i).generateMethodHandle();
            }
            return mhs;
        }
    }

    enum Foo {
        
        // Adds a <T> param to the operation, which uses a predestioned key
        CLASSIFIED,
        
        // Custom generated
        CUSTOM,
        
        
        INT_CLASSIFIED, // Injects a single MethodHandle
        
        SINGLE;
    }
    
    static final class MapLookup<T> extends NewX<MethodHandle> {

        private final LinkedHashMap<T, OperationHandle> operations = new LinkedHashMap<>();

        Class<?> selectorType;

        /**
         * @param methodType
         * @param key
         */
        MapLookup(MethodType methodType, Key<?> key) {
            super(methodType, key);
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle get() {
            Map<T, Integer> m = new HashMap<>();
            MethodHandle[] mhs = new MethodHandle[operations.size()];
            int counter = 0;
            for (Map.Entry<T, OperationHandle> entry : operations.entrySet()) {
                MethodHandle mh = entry.getValue().generateMethodHandle();
                mh = MethodHandles.dropArguments(mh, 0, selectorType);
                mhs[counter] = mh;
                m.put(entry.getKey(), counter++);
            }
            m = Map.copyOf(m);
            throw new UnsupportedOperationException();
        }

        // Vi laver en table switch goer vi ikke her???
        static final MethodHandle get(Object o, Map<?, Integer> map, MethodHandle[] mh) {
            Integer integer = map.get(o);
            System.out.println(integer);
            throw new UnsupportedOperationException();
        }
    }

    abstract static class NewX<T> implements Supplier<T> {
        final Key<?> key;
        final MethodType methodType;

        NewX<?> next;

        NewX(MethodType methodType, Key<?> key) {
            this.methodType = requireNonNull(methodType);
            this.key = requireNonNull(key);
        }
    }

}
