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
package app.packed.bean.sidebean;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanIntrospector.OnVariable;
import app.packed.bean.BeanLifetime;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.binding.Key;
import app.packed.operation.OperationHandle;
import internal.app.packed.bean.sidebean.PackedSidebeanAttachment;
import internal.app.packed.bean.sidebean.SidebeanHandle;

/**
 * A configuration object for aside bean.
 */
public final class SidebeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    private final SidebeanHandle<?> handle;

    public SidebeanConfiguration(BeanHandle<?> handle) {
        this.handle = (SidebeanHandle<?>) requireNonNull(handle);
        super(handle);
    }

    public <K> void attachmentBindConstant(Class<K> key, K constant) {
        attachmentConstant(Key.of(key), constant);
    }

    public <K> void attachmentConstant(Key<K> key, K constant) {
        checkIsConfigurable();
//        handle.bindCodeGenerator(key, supplier);
    }

    public <K> void attachmentBindCodeGenerator(Class<K> key, Supplier<? extends K> supplier) {
        attachmentBindCodeGenerator(Key.of(key), supplier);
    }

    public <K> void attachmentBindCodeGenerator(Key<K> key, Supplier<? extends K> supplier) {
        checkIsConfigurable();
//        handle.bindCodeGenerator(key, supplier);
    }

    public <K> void attachmentBindInvoker(Class<K> key) {
        attachmentBindInvoker(Key.of(key));
    }

    public <K> void attachmentBindInvoker(Key<K> key) {
        checkIsConfigurable();
//        handle.bindCodeGenerator(key, supplier);
    }


    private SidebeanAttachment attachTo(PackedSidebeanAttachment usage) {
        // For example, for a cron
        usage.bean.sideBeanAttachments.add(usage);

        // Im guessing we need to make room for it no matter what
        if (usage.bean.beanKind == BeanLifetime.SINGLETON) {
            usage.lifetimeStoreIndex = usage.bean.container.lifetime.store.add(usage);
        }
        handle.addAttachment(usage);

        return usage;
    }

    /**
     * Attaches this side bean to the specified bean (handle).
     *
     * @param beanHandle
     *            the handle of the bean to attach the sidebean to
     * @return
     */
    public SidebeanAttachment attachToBean(BeanHandle<?> beanHandle) {
        return attachTo(new PackedSidebeanAttachment.OfBean(handle, beanHandle));
    }

    public SidebeanAttachment attachToOperation(OperationHandle<?> operationHandle) {
        return attachTo(new PackedSidebeanAttachment.OfOperation(handle, operationHandle));
    }

    public SidebeanAttachment attachToParameter(OperationHandle<?> operationHandle) {
        return attachTo(new PackedSidebeanAttachment.OfOperation(handle, operationHandle));
    }


    // Hmm, fx for CurrentTime... Vil vi vil bare tilfoeje en til beanen
    // Kunne man bruge den samme til flere beans?
    // For example, InvocationCount
    public SidebeanAttachment attachToVariable(OnVariable handle) {
        throw new UnsupportedOperationException();
    }
}
