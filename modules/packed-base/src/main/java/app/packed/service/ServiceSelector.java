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

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import app.packed.container.Extension;

/**
 *
 */
public interface ServiceSelector<T> extends Predicate<ServiceDescriptor> {

    // andQualifiedWith
    // andNamed() <--- Taenker man kan bruge wildcards??? nahhh, det maa folk
    // andNamed(Pattern p);

    // will ignore qualifiers....
    public static <T> ServiceSelector<T> assignableTo(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    // extractFromSetsOf(Class<? extends T> type)

    // Så kan vi klare intoSet
    public static <T> ServiceSelector<T> assignableToOrSetsOf(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("exports")
    static void main(S2 s2) {
        s2.setCapture(ServiceSelector.assignableTo(String.class));
        // s2.mapAdd(ServiceSelector.assignableTo(String.class), s -> ((Deprecated) s.key().qualifier().get()).forRemoval());
        //
    }
}

// Hvad med foer linking og efter linking???

//Order
//// Own
//// Linked Services
// capture/add operations in order...

// @PreLinkageOperation

// Altsaa et link target. Kan ikke baade exportere og importere den samme service...

// Det her set kan saa blive injected ind i en anden service.
// Som kan iterere over contents... Der kommer en dependency pil til brugeren...
class S2 extends Extension {

    // Det er ikke et set af T... Da vi har

    // Syntes vi skal have et ServiceSet, hvor vi kan faa nogler, descriptions, osv.
    public <T> ServiceConfiguration<Set<T>> setCapture(ServiceSelector<T> selector) {
        throw new UnsupportedOperationException();
    }

    // Kan ogsaa tag nogle options
    // OnlyAdd, ExtractFromSets,...
    // Saa behover vi maaske bare en metode
    // select(selection, Option.intoServiceSet());
    // Vi
    // ServiceSelector select(Class|TypeLiteral)
    // Og saa kan man styre det paa ServiceSelector...

    // Men hvis service selector ogsaa skal virke andre steder...
    public <T> ServiceConfiguration<Set<T>> setAdd(ServiceSelector<T> selector) {
        throw new UnsupportedOperationException();
    }

    // IDeen er at folk kan registere services som de plejer... Men f.eks med en custom qualifier
    // Og saa mapper de d
    // Folk maa injecte en ServiceSelector og lave deres eget map....

    // Vi har droppet den, fordi det er lettere hvis den klasse man injecter ind i laver mapning....
    // Kan lave ServiceSet.stream().collect -> ...

    // Fails with IllegalStateException,
    // or Map<K, Provider<S>> toProviderMap(Function<ServiceDescription, K> mapper);
    @Deprecated
    public <M, T> ServiceConfiguration<Map<M, T>> mapAdd(ServiceSelector<T> selector, Function<ServiceDescriptor, M> keyMapper) {
        throw new UnsupportedOperationException();
    }
}
/// De her set bliver koert i raekke folge

/// setCapture().asNull(); <---- removes services