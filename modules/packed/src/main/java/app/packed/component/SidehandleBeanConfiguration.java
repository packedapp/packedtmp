/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.component;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;

/**
 * A configuration object for aside bean.
 */
public final class SidehandleBeanConfiguration<T> extends BeanConfiguration<T> {

    public SidehandleBeanConfiguration(BeanHandle<?> handle) {
        super(handle);
    }

    public void initOnly() {
        // Ideen er egentlig at vi ikke beholder en instans af sidebeanen
        // Men den kalder ind i en anden klasse med den som parameter
        // Problemet er lidt af vi aldrig kan afregistrer den.

        // Brugbart fx fra CLI
    }

}
