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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanDependency;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.inject.Factory0;
import app.packed.operation.OperationPack;
import packed.internal.container.RealmSetup;
import packed.internal.operation.PackedOperationPackSetup;
import packed.internal.util.LookupUtil;

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

    public void provideOperationPack(BeanDependency provider) {
        Key<?> key = provider.readKey();
        @SuppressWarnings("unchecked")
        PackedOperationPackSetup s = operationPack((Key<OperationPack>) key);
        provider.provide(new Factory0<OperationPack>(() -> s.build()) {});
    }

    /** A handle that can access #configuration. */
    private static final VarHandle VH_HANDLE = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), BeanConfiguration.class, "beanHandle", PackedBeanHandler.class);

    public static ExtensionBeanSetup from(ExtensionBeanConfiguration<?> configuration) {
        PackedBeanHandler<?> bh = (PackedBeanHandler<?>) VH_HANDLE.get((BeanConfiguration) configuration);
        return (ExtensionBeanSetup) bh.bean();
    }
}
