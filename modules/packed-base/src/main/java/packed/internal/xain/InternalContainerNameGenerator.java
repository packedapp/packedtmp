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
package packed.internal.xain;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import app.packed.container.Bundle;

/**
 *
 */
/** Small for utility class for generate a best effort unique name for containers. */
public class InternalContainerNameGenerator {

    /** Assigns unique IDs, starting with 1 when lazy naming containers. */
    private static final AtomicLong ANONYMOUS_ID = new AtomicLong();

    private static final ClassValue<Supplier<String>> BUNDLE_NAME_SUPPLIER = new ClassValue<>() {
        private final AtomicLong L = new AtomicLong();

        @Override
        protected Supplier<String> computeValue(Class<?> type) {
            String simpleName = type.getSimpleName();
            String s = simpleName.endsWith("Bundle") && simpleName.length() > 6 ? simpleName.substring(simpleName.length() - 6) : simpleName;
            return () -> s + L.incrementAndGet();
        }
    };

    static String fromBundleType(Class<? extends Bundle> cl) {
        return BUNDLE_NAME_SUPPLIER.get(cl).get();
    }

    static String next() {
        return "Container" + ANONYMOUS_ID.incrementAndGet();
    }
}
