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
package packed.internal.bean;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bean.operation.OperationPack;
import packed.internal.bean.operation.PackedOperationPackSetup;
import packed.internal.container.RealmSetup;

/**
 * A special version of bean setup for extension beans.
 */
public final class ExtensionBeanSetup extends BeanSetup {

    public static final Key<OperationPack> OPERATION_PACK_KEY = Key.of(OperationPack.class);

    @Nullable
    PackedOperationPackSetup operationPack;

    /**
     * @param builder
     * @param owner
     */
    public ExtensionBeanSetup(PackedBeanHandleBuilder<?> builder, RealmSetup owner) {
        super(builder, owner);
    }

    public PackedOperationPackSetup operationPack(@Nullable Key<OperationPack> key) {
        PackedOperationPackSetup p = operationPack;
        if (p == null) {
            p = operationPack = new PackedOperationPackSetup();
        }
        return p;
    }
}
