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

/**
 * A mutable factory where you can bind dependencies. For example,
 */
// FactoryBuilder...
//// kan man lave noget smart med MethodHandle????

// Alternativ er at den ikke extender Factory....
// Men at man koere
// Factory.of(Class).bindable(). build()
public final class BindableFactory<T> extends Factory<T> {

    /**
     * @param factory
     */
    BindableFactory(Factory<T> factory) {
        super(factory.factory);
    }

    public Factory<T> immutable() {
        // Vi bliver noedt til at lave en copy.... Ellers kan man jo binde videre...
        // Eller ogsaa skal man bare ikke kunne aendre i den oprindelige mere????

        // Vi skal
        return new Factory<>(factory);
    }

    // lidt diskussion om vi skal override nogle af de factory metoder. Men syntes bare man automatisk laver
    // en immutable factory, naar man starter med det....

    // rename
    // f.eks. String -> @Left String rename(2, new Key<@Left String>(){});

    // provideConstant(new Foot("Left").as(new Key<@Left Foot>(){};
    // provideConstant(new Foot("Right").as(new Key<@Right Foot>(){};

    // BindableFactory<Leg> f = BindableFactory.findInjectable(Leg.class);
    // provide(f.rekey(Foot.class, new Key<@Left Foot>(){})).as(new Key<@Left Leg>(){});
    // provide(f.rekey(Foot.class, new Key<@Left Foot>(){})).as(new Key<@Right Leg>(){});
    // ServiceDependency???. Maintains reference to field after rekeyingx?? I think so...
    // What about when we map?? I still think so... Maybe have a rekey'ed, mapped, ... marker
    // provide(Body.class);

    // rekey stuff = to provide (something) with a new key
    // Body(@Left Leg leftLeg, @Right rightLeg);

    // Maybe FactoryBuilder.... Og saa skip nogle af de metoder der er...
}
