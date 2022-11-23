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
package internal.app.packed.lifetime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import internal.app.packed.util.MethodHandleUtil;

/**
 *
 */
public sealed interface LifetimeAccessor {

    Class<?> type();

    Object read(LifetimeObjectArena ignore);

    void store(LifetimeObjectArena pool, Object o);
    
    MethodHandle poolReader();
    
    // Det giver ikke mening at indsaette constants...
    // Vi binder dem direkte i MH
    // Forstaar ikke jeg fik den taabelige ide.
    public record ConstantAccessor(Object constant, Class<?> type) implements LifetimeAccessor {

        public ConstantAccessor(Object constant) {
            this(constant, constant.getClass());
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> type() {
            return type;
        }

        public Object read(LifetimeObjectArena ignore) {
            return constant;
        }

        /** {@inheritDoc} */
        @Override
        public void store(LifetimeObjectArena pool, Object o) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle poolReader() {
           throw new UnsupportedOperationException();
        }
    }

    public record DynamicAccessor(Class<?> type, int index) implements LifetimeAccessor {

        // Skal vi vide hvor vi bliver laest fra???
        public MethodHandle poolReader() {
            // (LifetimePool, int)Object -> (LifetimePool)Object
            MethodHandle mh = MethodHandles.insertArguments(LifetimeObjectArena.MH_CONSTANT_POOL_READER, 1, index);
            return MethodHandleUtil.castReturnType(mh, type); // (LifetimePool)Object -> (LifetimePool)clazz
        }

        public Object read(LifetimeObjectArena pool) {
            return pool.read(index);
        }

        public void store(LifetimeObjectArena pool, Object o) {
            if (!type.isInstance(o)) {
                throw new Error("Expected " + type + ", was " + o.getClass());
            }
            pool.storeObject(index, o);
        }
    }
}
