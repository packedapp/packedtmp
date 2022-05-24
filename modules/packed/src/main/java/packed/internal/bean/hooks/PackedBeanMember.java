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
package packed.internal.bean.hooks;

import packed.internal.bean.BeanSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.operation.PackedOperationTarget;
import packed.internal.util.OpenClass;

/**
 *
 */
// We create one instance of a member per extension...
// So we multiple extensions have annotated the same method we create multiple PBM
public abstract sealed class PackedBeanMember implements PackedOperationTarget permits PackedBeanField, PackedBeanMethod {

    final OpenClass openClass;

    public final BeanSetup bean;

    public final ExtensionSetup operator;

    PackedBeanMember(BeanScanner scanner, ExtensionSetup operator) {
        this.openClass = scanner.oc;
        this.bean = scanner.bean;
        this.operator = operator;
    }
}
