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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;

/**
 * A bean handle represents a private configuration installed bean.
 * <p>
 * Instances of {@code BeanHandle} are normally never exposed directly to end-users. Instead they are returned wrapped
 * in {@link BeanConfiguration} or a subclass hereof.
 * 
 * 
 */
public final /* primitive */ class BeanHandle<T> {

    final BeanSetup bean;

    BeanHandle(BeanSetup bean) {
        this.bean = requireNonNull(bean);
    }

    // We need a extension bean
    public OperationHandle addFunctionalOperation(ExtensionBeanConfiguration<?> operator, Class<?> functionalInterface, OperationType type,
            Object functionInstance) {
        // Function, OpType.of(void.class, HttpRequest.class, HttpResponse.class), someFunc)
        throw new UnsupportedOperationException();
    }

    public OperationHandle addOperation(ExtensionBeanConfiguration<?> operator, MethodHandle methodHandle) {
        return addOperation(operator, Op.ofMethodHandle(methodHandle));
    }

    public OperationHandle addOperation(ExtensionBeanConfiguration<?> operator, Op<?> operation) {
        throw new UnsupportedOperationException();
    }

    public Class<?> beanClass() {
        return bean.beanClass();
    }

    public BeanKind beanKind() {
        return bean.beanKind;
    }

    /** {@inheritDoc} */
    public void decorateInstance(Function<? super T, ? extends T> decorator) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public Key<?> defaultKey() {
        if (beanClass() == void.class) {
            throw new UnsupportedOperationException("Keys are not support for void bean classes");
        }
        return Key.of(beanClass());
    }

    /** {@inheritDoc} */
    public boolean isConfigurable() {
        return !bean.realm.isClosed();
    }

    public boolean isCurrent() {
        return false;
    }

    /**
     * If the bean is registered with its own lifetime. This method returns a list of the lifetime operations of the bean.
     * <p>
     * The operations in the returned list must be computed exactly once. For example, via
     * {@link OperationHandle#computeMethodHandleInvoker()}. Otherwise a build exception will be thrown. Maybe this goes for
     * all operation customizers.
     * 
     * @return
     */
    public List<OperationHandle> lifetimeOperations() {
        return List.of();
    }

    /** {@return a new mirror.} */
    // Interessant...
    public BeanMirror mirror() {
        // Create a new BeanMirror
        BeanMirror mirror;
        if (bean.mirrorSupplier == null) {
            mirror = new BeanMirror();
        } else {
            mirror = bean.mirrorSupplier.get();
            if (mirror == null) {
                throw new NullPointerException(bean.mirrorSupplier + " returned a null instead of an " + BeanMirror.class.getSimpleName() + " instance");
            }
        }
        mirror.initialize(bean);
        return mirror;
    }

    /** {@inheritDoc} */
    public BeanHandle<T> onWireRun(Runnable action) {
        requireNonNull(action, "action is null");
        Runnable w = bean.onWiringAction;
        if (w == null) {
            bean.onWiringAction = action;
        } else {
            bean.onWiringAction = () -> {
                w.run();
                action.run();
            };
        }
        return this;
    }

    /** {@inheritDoc} */
    public void peekInstance(Consumer<? super T> consumer) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public void specializeMirror(Supplier<? extends BeanMirror> mirrorFactory) {
        bean.mirrorSupplier = mirrorFactory;
    }

    // Tjah, skal vel ogsaa bruges for containere
    public interface LifetimeConf {
        LifetimeConf ALL = null;
        LifetimeConf START_ONLY = null;
        LifetimeConf STOP = null;

    }

    // Lad os sige vi koere suspend... saa skal vi ogsaa kunne koere resume?

}

//
//interface BeanHandle<T> {
//        /**
//     * @param decorator
//     */
//    // Usacase?? Typically T is not accessible to the extension
//    // Right now this method is only here for InstanceBeanConfiguration#decorate
//    // Maybe
//    void decorateInstance(Function<? super T, ? extends T> decorator);
//
//    /**
//     * @return
//     * 
//     * @throws UnsupportedOperationException
//     *             if called on a bean with void.class beanKind
//     */
//    Key<?> defaultKey();
//
////    // Kan man tilfoeje en function til alle beans?
////    // funktioner er jo stateless...
////    // Er ikke sikker paa jeg syntes staten skal ligge hos operationen.
////    // Det skal den heller ikke.
////    default <F> OperationCustomizer newFunctionalOperation(Class<F> tt, F function) {
////        throw new UnsupportedOperationException();
////    }
////    
////    // Problemet er her at det virker meget underligt lige pludselig at skulle tilfoeje lanes
////    // Og hvordan HttpRequest, Response er 2 separate lanes... det er lidt sort magi
////    // Skal vi bruge et hint???
////    default OperationCustomizer newFunctionalOperation2(Object functionInstance, Class<?> functionType, Class<?>... typeVariables) {
////        throw new UnsupportedOperationException();
////    }
////    default OperationCustomizer newFunctionalOperation(TypeToken<?> tt, Object functionInstance) {
////        throw new UnsupportedOperationException();
////    }
//
//    boolean isConfigurable();
//
//    /**
//     * Registers a wiring action to run when the bean becomes fully wired.
//     * 
//     * @param action
//     *            a {@code Runnable} to invoke when the bean is wired
//     */
//    // ->onWire
//    BeanHandle<T> onWireRun(Runnable action);
//
//    /**
//     * @param consumer
//     */
//    // giver den plus decorate mening?
//    
//    // Er det naar vi instantiere???
//    
//    void peekInstance(Consumer<? super T> consumer);
//
//    // Hvis vi aabner op for specialized bean mirrors
//    // maybe just name it mirror?
//    void specializeMirror(Supplier<? extends BeanMirror> mirrorFactory);
//
//
//}
