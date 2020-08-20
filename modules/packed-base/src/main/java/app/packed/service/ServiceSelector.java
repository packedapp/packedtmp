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
package app.packed.service;

import java.util.function.Predicate;

/**
 *
 */
public interface ServiceSelector<T> extends Predicate<ServiceDescriptor> {

    /**
     * Returns a service selector that will select every service.
     * 
     * @return a service selector that will select every service
     */
    static ServiceSelector<Object> all() {
        throw new UnsupportedOperationException();
    }

    // will ignore qualifiers....
    public static <T> ServiceSelector<T> assignableTo(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    // andQualifiedWith
    // andNamed() <--- Taenker man kan bruge wildcards??? nahhh, det maa folk
    // andNamed(Pattern p);

    // extractFromSetsOf(Class<? extends T> type)

    // Så kan vi klare intoSet
    public static <T> ServiceSelector<T> assignableToOrSetsOf(Class<T> type) {
        throw new UnsupportedOperationException();
    }
}
