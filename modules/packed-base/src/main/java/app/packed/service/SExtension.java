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

import java.util.function.Function;

import app.packed.component.BeanConfiguration;
import app.packed.container.BaseBundle;
import app.packed.container.Extension;
import app.packed.inject.ServiceLocator;
import app.packed.inject.ServiceSelection;

/**
 *
 */

// Sets
// Locator
// Injector

// En maade at binde alle services der bliver importeret...
class SExtension extends Extension {

    void bindUnused() {

    }

    public <S> BeanConfiguration<ServiceSelection<S>> addSet(Function<ServiceSelector<Object>, ServiceSelector<S>> selector) {
        throw new UnsupportedOperationException();
    }

    public <S> BeanConfiguration<ServiceSelection<S>> addSetAssignableTo(Class<S> assignableTo) {
        return addSet(s -> s.assignableTo(assignableTo));
    }

    BeanConfiguration<ServiceLocator> newLocatox(Function<ServiceSelector<Object>, ServiceSelector<?>> selector) {
        throw new UnsupportedOperationException();
    }
}

class Usage extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {

        use(SExtension.class).addSet(s -> s.assignableTo(String.class));

        use(SExtension.class).newLocatox(s -> s.assignableTo(String.class));
    }

}

//
//public <S> BeanConfiguration<ServiceSet<S>> addSetOld(OldServiceSelector<S> selector) {
//  throw new UnsupportedOperationException();
//}