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
package app.packed.moduletests.notopen;

import app.packed.bean.SidehandleTargetKind;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.moduletests.isopen.IsOpenSidebean;

/**
 *
 */
public final class NotOpenExtension extends Extension<NotOpenExtension>{

    /**
     * @param handle
     */
    protected NotOpenExtension(ExtensionHandle<NotOpenExtension> handle) {
        super(handle);
    }

    public void addSidebean() {
        base().installSidebeanIfAbsent(IsOpenSidebean.class, SidehandleTargetKind.OPERATION, _ -> {});
    }
}
