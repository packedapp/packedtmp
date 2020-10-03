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
package app.packed.inject;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import app.packed.base.Attribute;
import app.packed.base.Key;

/**
 * A selection of service where each service provide instances of a similar type.
 */
public interface ServiceSelection<S> extends ServiceRegistry {

    void forEachInstance(BiConsumer<? super Service, ? super S> action);

    void forEachInstance(Consumer<? super S> action);

    void forEachProvider(BiConsumer<? super Service, ? super Provider<S>> action);

    void forEachProvider(Consumer<? super Provider<S>> action);

    <T> ServiceSelection<S> selectOnAttribute(Attribute<T> attribute, Predicate<? super T> filter);

    // Only those services which has the s
    ServiceSelection<S> selectOnQualifier(Class<? extends Annotation> qualifier);

    <T extends Annotation> ServiceSelection<S> selectOnQualifier(Class<? extends T> qualifier, Predicate<? super T> filter);

    // selectOnType();

    Map<Key<? extends S>, S> toInstanceKeyMap();

    /**
     * Returns an immutable list an instance for each service.
     * 
     * @return an immutable list an instance for each service
     */
    List<S> toInstanceList();

    Map<Service, S> toInstanceMap();

    Map<Key<? extends S>, Provider<S>> toProviderKeyMap();

    List<Provider<S>> toProviderList();

    Map<Service, Provider<S>> toProviderMap();
}
//It is not a set... Because we might have multiple instances of the same type...
//Det er hellere ikke rigtig en collection fordi vi laver maaske nye instanser hver gang...
