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

import app.packed.binding.Key;
import app.packed.operation.Op;
import app.packed.runtime.RunState;
import internal.app.packed.bean.BeanSetup;

/**
 * The configuration of bean that have instances at runtime. This is all beans except for {@link BeanKind#STATIC beans}.
 *
 * @param <T>
 *            the type of the bean instance
 */
public class InstanceBeanConfiguration<T> extends BeanConfiguration {

    /**
     * @param handle
     */
    public InstanceBeanConfiguration(BeanHandle<?> handle) {
        super(handle);
    }

    /** {@inheritDoc} */
    @Override
    public InstanceBeanConfiguration<T> allowMultiClass() {
        super.allowMultiClass();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public InstanceBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <K> InstanceBeanConfiguration<T> bindInstance(Class<K> key, K instance) {
        return bindInstance(Key.of(key), instance);
    }

    /** {@inheritDoc} */
    @Override
    public <K> InstanceBeanConfiguration<T> bindInstance(Key<K> key, K instance) {
        super.bindInstance(key, instance);
        return this;
    }
}

//Skal bruges paa params som skal "bootstrappes" med...
//Maaske bare @Bootstrap

//beforeIntrospec();
//ignoreAnnotation.
//addAnnotation
//resolve();
class InstanceBeanConfigurationSandbox<T> {
    // @SpecFix({ "Da vi altid laver alle bindings naar vi installere... Fungere det ikke super godt med initializeWith.",
    // " Det er jo ihvertfald ikke en service binding laengere" })
// Hvad hvis man bruger servicen i baade constructoren og i @OnStop...
// Saa fungere det her initialize jo ikke...

// @Initializer -> Kan man ikke bruge det til at over
// Tror ikke man baade kan initialize en Key, og saa bruge den som en service andet steds.
// Tjah det er jo en build time instans... Saa det kan jo bare vaere -> inject()
// bindKey?
// overrideService(); takeService();

// Overrides all occurenses of the service. and binds it to the instance
// Tager ikke null... Masske vi kan have en overrideServiceAsMissing

// Det betyder vi skal have et map med key
// Og vi skal ikke laengere eagerly resolve services
// Saa naar man resolver en service for en bean. Skal man foerst kigge i mappet..
// Does not override hoooks I think
// Problemet er vi allerede har installeret ServiceExtension'en

// installer().overrideService(asdasd).install(Key, instance);
//// Hvordan vil den supportere WebExtension.install()???
//// installer() fungere bare ikke

// initializeWith

    // How do we handle null?

//  public <K> InstanceBeanConfiguration<T> initializeWith(Class<K> key, Op<K> operation) {
//      throw new UnsupportedOperationException();
//  }
//

    // Maaske har vi neglet semantikken
    // Fungere kun som initialization...
    // Hvis samme key bliver resolvet som en rigtig service andre steder er det en fejl.

    // Override service -> Bliver et specielt mirror... Ikke et ServiceMirror
    //
    // Tror vi fejler haardt hvis den ikke er bundet til en potentiel service...

    // Teanker ogsaa det er en fejl. Hvis den ikke overrider noget

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
        throw new UnsupportedOperationException();
    }

    <K> InstanceBeanConfiguration<T> initializeWith(Op<?> op) {
        throw new UnsupportedOperationException();
    }

    // Peek when???? Maybe wrap a factory for now
    //// Do we want to peek on instances???? Maybe fail if used on source=instances
    // peekAfterCreate
    /**
     * Immediately after a bean instance has been created. But before any lifecycle operations are called.
     *
     * @param consumer
     *            the consumer to call
     * @return this configuration
     * @throws UnsupportedOperationException
     *             if the bean has bean created with an instance (beanSourceKind = BeanSourceKind#INSTANCE)
     */
    // Nogen vil man vel gerne fx vaere injected
    InstanceBeanConfiguration<T> peek(BeanSetup bean, Consumer<? super T> consumer) {
        if (bean.beanSourceKind == BeanSourceKind.SOURCELESS) {
            throw new UnsupportedOperationException("Operation not supported for beans that have no source");
        } else if (bean.beanSourceKind == BeanSourceKind.INSTANCE) {
            throw new UnsupportedOperationException("Operation not supported for beans that have been registered with an instance");
        }

        // peek at constr
        // handle().peekInstance(consumer);
        throw new UnsupportedOperationException();
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

//
//// Jeg syntes den er forfaerdelig... isaer navngivningsmaessigt.
//// Vi maa have en special ExtensionBeanConfiguration...
//// Eller endnu bedre en speciel klasse.
//@Deprecated
//public <K> InstanceBeanConfiguration<T> overrideServiceDelayed(Class<K> key, Supplier<K> supplier) {
//  return overrideServiceDelayed(Key.of(key), supplier);
//}
//
//@Deprecated
//public <K> InstanceBeanConfiguration<T> overrideServiceDelayed(Key<K> key, Supplier<K> supplier) {
//  // delayedInitiatedWith
//  // Taenker vi har et filled array som er available when initiating the lifetime
//
//  // Her skal vi ogsaa taenke ind at det skal vaere en application singleton vi injecter ind i.
//  // Altsaa vi vil helst ikke initiere en Session extension bean med MHs hver gang
//
//  return this;
//}
// inject, bind, provide
//// Ikke provide... vi har allerede provideAs
//// bind syntes jeg er fint at det er positionelt

//Den bliver koert foerend vi returnere BeanConfiguration.
//Og som giver ejeren af beanen stor control...

//beanExtension.wrapNext(Consumer<?> action);
//beanExtension.wrapAll(Consumer<?> action);
//? Er en der har fuld kontrol over hvad der sker

//Alternativ resolver vi kun til en service. Hvis vi ikke bliver initializet
//Mit eneste problem er, at det kun virker for factory/initailizatio operations.
//Ellers bliver de resolved som en service. Maaske det kan skabe lidt problemer
//Tror nu det er bedre... Maa jeg indroemme.. Den foeles ikke helt rigtig den her annotering...

// ServiceLocator sl = ServiceLocator.of(xxx) <-- return InternalServiceLocator
// Syntes man skal angive key.. for let at goere provideInstance(serviceLocator);
//// Kunne ogsaa vaere bind
//@SuppressWarnings({ "unchecked", "rawtypes" })
//public InstanceBeanConfiguration<T> provideInstance(Object instance) {
//    return provideInstance((Class) instance.getClass(), instance);
//}
