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

/**
 *
 */
public sealed interface LifetimeAccessor {

    Class<?> type();

    Object read(PackedExtensionContext ignore);

    void store(PackedExtensionContext pool, Object o);
    
    public record ConstantAccessor(Object constant, Class<?> type) implements LifetimeAccessor {

        public ConstantAccessor(Object constant) {
            this(constant, constant.getClass());
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> type() {
            return type;
        }

        public Object read(PackedExtensionContext ignore) {
            return constant;
        }

        /** {@inheritDoc} */
        @Override
        public void store(PackedExtensionContext pool, Object o) {
            throw new UnsupportedOperationException();
        }
    }

    public record DynamicAccessor(Class<?> type, int index) implements LifetimeAccessor {

        public Object read(PackedExtensionContext pool) {
            return pool.read(index);
        }

        public void store(PackedExtensionContext pool, Object o) {
            if (!type.isInstance(o)) {
                throw new Error("Expected " + type + ", was " + o.getClass());
            }
            pool.storeObject(index, o);
        }
    }
}
