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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Optional;

import app.packed.base.Key;
import packed.internal.bean.BeanSetup;
import packed.internal.inject.service.InternalServiceUtil;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 * The configuration of a container bean.
 * <p>
 * that can serve as basis for actual component configuration types.
 * <p>
 * Component configuration classes do not need to extend this class.
 */
public non-sealed class ContainerBeanConfiguration<T> extends BeanConfiguration<T> {

    /** A var handle that can update the {@link #configuration()} field in this class. */
    private static final VarHandle VH_BEAN_SETUP = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), BeanConfiguration.class, "bean", BeanSetup.class);

    Key<?> export;

    Key<?> provide;

    /**
     * @param maker
     */
    public ContainerBeanConfiguration(BeanMaker<T> maker) {
        super(maker);
    }

    /** {@return the container setup instance that we are wrapping.} */
    private BeanSetup bean() {
        try {
            return (BeanSetup) VH_BEAN_SETUP.get((BeanConfiguration<?>) this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    Key<?> defaultKey() {
        return bean().defaultKey();
    }

    public void export() {
        bean().sourceExport();
    }

    /** {@inheritDoc} */
    @Override
    public final BeanKind kind() {
        return BeanKind.CONTAINER;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }

    public ContainerBeanConfiguration<T> provide() {
        provide = InternalServiceUtil.checkKey(bean().hookModel.clazz, bean().defaultKey());
        bean().sourceProvide();
        return this;
    }

    public ContainerBeanConfiguration<T> provideAs(Class<?> key) {
        provide = InternalServiceUtil.checkKey(bean().hookModel.clazz, key);
        bean().sourceProvideAs(provide);
        return this;
    }

    public ContainerBeanConfiguration<T> provideAs(Key<?> key) {
        provide = InternalServiceUtil.checkKey(bean().hookModel.clazz, key);

        bean().sourceProvideAs(key);
        return this;
    }
// Ser dum ud naar man laver completion
    public Optional<Key<?>> providedAs() {
        return Optional.ofNullable(provide);
    }
}
//
//public <X extends Runnable & Callable<String>> X foo() {
//  return null;
//}