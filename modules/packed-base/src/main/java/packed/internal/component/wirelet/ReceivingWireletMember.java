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
package packed.internal.component.wirelet;

import java.lang.invoke.VarHandle;

import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.container.Extension;

/**
 *
 */
public class ReceivingWireletMember {

    VarHandle mh;

    Class<? extends Wirelet> type;

    boolean isList;

    boolean isOptional;

    @Nullable
    Class<? extends Extension> extensionType;

    void injectIfField(WireletPack wirelets) {

    }
}
