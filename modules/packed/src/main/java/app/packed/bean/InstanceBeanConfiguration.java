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
package app.packed.bean;

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.lifecycle.RunState;

/**
 * A bean that
 */
//// Does it always have a Lifecycle??? Do we have a seperate configuration for this?
public class InstanceBeanConfiguration<T> extends BeanConfiguration {

    /**
     * @param maker
     */
    public InstanceBeanConfiguration(BeanDriver<T> maker) {
        super(maker);
    }

    // Hmm, vi dekorere ikke fx ServiceLocator...
    // Maaske er det bedre at dekorere typer???
    //// InjectableVarSelector<T>
    // InjectableVarSelector.keyedOf()
    // What is the key doing here???
    public <E> InstanceBeanConfiguration<T> decorate(Key<E> key, Function<E, E> mapper) {
        /// Mnahhh
        throw new UnsupportedOperationException();
    }

    // Hmm det er jo mere provide end inject..
    // men provide(FooClass.class).provide(ddd.Class);
    // maybe provideTo()
    
    // inject, bind, provide
    //// Ikke provide... vi har allerede provideAs
    //// bind syntes jeg er fint at det er positionelt
    public <E> InstanceBeanConfiguration<T> inject(Class<E> key, E instance) {
        return inject(Key.of(key), instance);
    }

    // Taenker den overrider
    public <E> InstanceBeanConfiguration<T> inject(Key<E> key, E instance) {
        throw new UnsupportedOperationException();
    }

    // Kunne ogsaa vaere bind
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public InstanceBeanConfiguration<T> inject(Object instance) {
        return inject((Class) instance.getClass(), instance);
    }

    @Override
    public InstanceBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }

    /**
     * @param state
     *            the state at which to perform the specified action
     * @param action
     *            the action to perform
     * @return this configuration
     * @throws IllegalArgumentException
     *             if terminated and bean does not support it
     * @throws UnsupportedOperationException
     *             if the specified state is that is managed by the user
     */
    // Ved ikke om det kan vaere problematisk, hvis instanserne ikke er styret af packed
    ///
    // Det der er farligt her er at vi capture Assemblien. Som capture extensionen
    // Som capture alt andet
    ///// fx Validator beans vil ikke virke her...
    public InstanceBeanConfiguration<T> on(RunState state, Consumer<T> action) {
        // Maybe throw UOE instead of IAE
        return this;
    }
}
