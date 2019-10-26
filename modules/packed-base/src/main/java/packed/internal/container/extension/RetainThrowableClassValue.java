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
package packed.internal.container.extension;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import app.packed.container.InternalExtensionException;

/**
 *
 */

// Well almost easier to embed...Then we can better control the exceptions we throw...
public abstract class RetainThrowableClassValue<T> {

    private final Map<Class<?>, Throwable> m = Collections.synchronizedMap(new WeakHashMap<>());

    private final ClassValue<T> actual = new ClassValue<>() {

        @Override
        protected T computeValue(Class<?> type) {
            Throwable t = m.get(type);
            if (t != null) {
                throw new InternalExtensionException("Extension failed to initialize previously", t);
            }
            // TODO Problem with storing exception here, is that that what ever classloader it belongs to it cannot be unloaded...
            try {
                return RetainThrowableClassValue.this.computeValue(type);
            } catch (RuntimeException | Error e) {
                m.put(type, e);
                throw e;
            }
        }
    };

    protected abstract T computeValue(Class<?> type);

    public T get(Class<?> type) {
        return actual.get(type);
    }
}
