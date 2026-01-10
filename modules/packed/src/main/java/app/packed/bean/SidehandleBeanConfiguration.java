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

import internal.app.packed.bean.sidehandle.SidehandleBeanHandle;

/**
 * A configuration object for aside bean.
 */
public final class SidehandleBeanConfiguration<T> extends BeanConfiguration<T> {

    private final SidehandleBeanHandle<?> handle;

    public SidehandleBeanConfiguration(BeanHandle<?> handle) {
        super(handle);
        this.handle = (SidehandleBeanHandle<?>) handle;
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
}
