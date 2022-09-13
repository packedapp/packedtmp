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
import app.packed.lifetime.managed.RunState;

/**
 * A base configuration class for beans that creates instances of {@link BeanConfiguration#beanClass()} at runtime.
 * 
 * @param <T>
 *            the type of bean instances that will be created at runtime
 */
public class InstanceBeanConfiguration<T> extends BeanConfiguration {

    /**
     * Creates a new InstanceBeanConfiguration
     * 
     * @param handle
     *            the bean handle
     */
    public InstanceBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
    }

    // Understoetter vi altid DependencyInjection???
    // bindServiceInstance????
    <K> InstanceBeanConfiguration<T> bindInstance(Class<K> key, K instance) {
        return bindInstance(Key.of(key), instance);
    }

    // Taenker den overrider
    <K> InstanceBeanConfiguration<T> bindInstance(Key<K> key, K instance) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * The decorator must return a non-null bean instance that is assignable to {@link #beanClass()}. Failure do to so will
     * fail with a BeanDecorationException being thrown at runtime.
     * <p>
     * Notice: If you return a subclass of {@link #beanClass()} and the subclass uses hooks, for example, these will be
     * ignored. hooks are always resolved against {@link #beanClass()}
     * 
     * @param decorator
     *            the decorator to apply to newly constructed bean instances
     * @return this configuration
     */
    // Kan vi finde en eneste usecase?
    // Hvornaar bliver den kaldt??? Igen er det ikke bare paa factorien???
    InstanceBeanConfiguration<T> decorate(Function<? super T, ? extends T> decorator) {
        handle().decorateInstance(decorator);
        return this;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    protected BeanHandle<T> handle() {
        return (BeanHandle<T>) super.handle();
    }

    /** {@inheritDoc} */
    @Override
    public InstanceBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }

    // Peek when???? Maybe wrap a factory for now
    InstanceBeanConfiguration<T> peek(Consumer<? super T> consumer) {
        // peek at constr
        handle().peekInstance(consumer);
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
    // Det der er farligt her er at vi capture Assemblien. Som capture extensionen
    // Som capture alt andet
    ///// fx Validator beans vil ikke virke her...
    //// Does it always have a Lifecycle??? Do we have a seperate configuration for this?
    // Maaske drop den????
    InstanceBeanConfiguration<T> peekAt(RunState state, Consumer<T> action) {
        throw new UnsupportedOperationException();
    }
}

// inject, bind, provide
//// Ikke provide... vi har allerede provideAs
//// bind syntes jeg er fint at det er positionelt

// ServiceLocator sl = ServiceLocator.of(xxx) <-- return InternalServiceLocator
// Syntes man skal angive key.. for let at goere provideInstance(serviceLocator);
//// Kunne ogsaa vaere bind
//@SuppressWarnings({ "unchecked", "rawtypes" })
//public InstanceBeanConfiguration<T> provideInstance(Object instance) {
//    return provideInstance((Class) instance.getClass(), instance);
//}
