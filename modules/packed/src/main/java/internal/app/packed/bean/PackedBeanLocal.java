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

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanIntrospector;

/**
 *
 */
// Alternativ til attachments

// Min eneste anke er an man maaske gerne vil kunne bruge navnet for noget
// Der fungere paa runtime

// Maybe have a BeanLocalMap as well
public final class PackedBeanLocal<T> {

    private PackedBeanLocal() {

    }

    public T get(BeanConfiguration configuration) {
        return get(BeanSetup.crack(configuration));
    }

    public T get(PackedBeanHandle<?> handle) {
        return get(handle.bean());
    }

    public T get(BeanIntrospector bi) {
        throw new UnsupportedOperationException();
    }

    public T get(BeanSetup bean) {
//        bean.map.get(key);
        throw new UnsupportedOperationException();
    }

    // Er istedet for attachments
    public static <T> PackedBeanLocal<T> of() {
        return new PackedBeanLocal<>();
    }
}
