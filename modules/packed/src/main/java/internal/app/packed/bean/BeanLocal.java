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
package internal.app.packed.bean;

import java.util.concurrent.atomic.AtomicLong;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;

/**
 *
 */
// Alternativ til attachments

// Min eneste anke er an man maaske gerne vil kunne bruge navnet for noget
// Der fungere paa runtime

public final class BeanLocal<T> {

    static final AtomicLong KEYS = new AtomicLong();
    
    // IDK maybe a new Object is just easier?
    // Also it does not need to have super performance
    private final long key = KEYS.get();

    
    public int hashCode() {
        return Long.hashCode(key);
    }
    
    
    T get(BeanConfiguration configuration) {
        return get(BeanSetup.crack(configuration));
    }

    T get(BeanHandle<?> handle) {
        return get(BeanSetup.crack(handle));
    }

    private T get(BeanSetup bean) {
//        bean.map.get(key);
        throw new UnsupportedOperationException();
    }

    // Er istedet for attachments
    static <T> BeanLocal<T> of(Class<T> type) {
        throw new UnsupportedOperationException();
    }
}
