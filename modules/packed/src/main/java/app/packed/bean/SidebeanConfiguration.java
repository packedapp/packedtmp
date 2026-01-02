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

import app.packed.binding.Key;
import internal.app.packed.bean.sidebean.PackedSidebeanBinding;
import internal.app.packed.bean.sidebean.SidebeanHandle;
import internal.app.packed.bean.sidebean.SidebeanInvokerModel;

/**
 * A configuration object for aside bean.
 */
public final class SidebeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    private final SidebeanHandle<?> handle;

    public SidebeanConfiguration(BeanHandle<?> handle) {
        super(handle);
        this.handle = (SidebeanHandle<?>) handle;
    }

    public void initOnly() {
        // Ideen er egentlig at vi ikke beholder en instans af sidebeanen
        // Men den kalder ind i en anden klasse med den som parameter
        // Problemet er lidt af vi aldrig kan afregistrer den.

        // Brugbart fx fra CLI
    }

//    /**
//     * Attaches this side bean to the specified bean (handle).
//     *
//     * @param beanHandle
//     *            the handle of the bean to attach the sidebean to
//     * @return
//     */
//    public SidebeanAttachment attachToBean(BeanHandle<?> beanHandle) {
//        return handle.attachTo(new PackedSidebeanAttachment.OfBean(BeanSetup.crack(handle), beanHandle));
//    }
//
//
//    // Hmm, fx for CurrentTime... Vil vi vil bare tilfoeje en til beanen
//    // Kunne man bruge den samme til flere beans?
//    // For example, InvocationCount
//    public SidebeanAttachment attachToVariable(OnVariable handle) {
//        throw new UnsupportedOperationException();
//    }

    private void sidebeanBind(Key<?> key, PackedSidebeanBinding binding) {
        checkIsConfigurable();
        if (handle.bindings.putIfAbsent(key, binding) != null) {
            throw new IllegalArgumentException(key + " has already been registered");
        }
    }

    public <K> void sidebeanBindComputedConstant(Class<K> key) {
        sidebeanBindComputedConstant(Key.of(key));
    }

    public <K> void sidebeanBindComputedConstant(Key<K> key) {
        checkIsConfigurable();
//        handle.bindCodeGenerator(key, supplier);
    }

    /**
     * @param <K>
     * @param key
     * @see SidebeanAttachment#bindConstant(Class, Object)
     */
    public <K> void sidebeanBindConstant(Class<K> key) {
        sidebeanBindConstant(Key.of(key));
    }

    public <K> void sidebeanBindConstant(Key<K> key) {
        sidebeanBind(key, new PackedSidebeanBinding.Constant());
    }

    // bindAbstractInvoker???
    public void sidebeanInvokeAs(Class<?> invokerClass) {
        Key<?> invokerKey = Key.of(invokerClass);
        if (handle.sim != null) {
            throw new IllegalStateException();
        }
        SidebeanInvokerModel sim = handle.sim = SidebeanInvokerModel.of(invokerClass);

        // Right now we always generate it, because we have a test in module-tests that needs to fail.
        // However, right now it does not use the sidebean, so code is never generated.
        sim.constructor();
        sidebeanBind(invokerKey, new PackedSidebeanBinding.Invoker(sim));
    }
}
