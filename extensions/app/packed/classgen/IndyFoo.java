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
package app.packed.classgen;

import java.lang.invoke.MethodHandles;

import app.packed.container.BaseAssembly;

/**
 *
 */
// extends Componendriger

// Ideen er lidt at vi laver en klasse som generere en klasse per subklasse.
// Her er specifikt taenkt paa hvordan Spring supportere Repository

// Puha hvis vi laver abstract klasse. Har vi godt nok lyst til at extracte parameterene.
// fra en ThreadLocalContext...
// Saa slipper vi for have aandsvage constructere. Der tager ting som vi ikke skal bruge...
// Med mindre vi har noget a.la. TemplateContext som enhver template bliver instantieret med
public abstract class IndyFoo<T> /* implements ClassSourcedDriver<BeanConfiguration<T>, T> */ {

    // Use rforax InvocationLinker
    public static <T> IndyFoo<T> create(MethodHandles.Lookup lookup, Object linker) {
        throw new UnsupportedOperationException();
    }

    // Ideen er lidt at denne abstracte klasse can holde alle felter

    // Ahh okay, nu er det knapt saa simpelt...
    public static <T> IndyFoo<T> create(MethodHandles.Lookup lookup, Class<?> abstractClass, Object linker) {
        throw new UnsupportedOperationException();
    }
}

interface Repository {

}

class JPAExtension {
    static final IndyFoo<Repository> RDRIVER = IndyFoo.create(MethodHandles.lookup(), null);

//    @SuppressWarnings({ "rawtypes", "unchecked" })
//    public static <T extends Repository> ClassSourcedDriver<BeanConfiguration<T>, T> repository() {
//        return (ClassSourcedDriver) RDRIVER;
//    }
}

interface MyRepo extends Repository {
    void foo();
}

class Usage extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
       // wire(JPAExtension.repository(), MyRepo.class).provide();
    }
}
