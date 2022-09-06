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
package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.bean.BeanConfiguration;
import app.packed.container.ExtensionBeanConfiguration;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.RealmSetup;
import internal.app.packed.util.LookupUtil;

/**
 * A special version of bean setup for extension beans.
 */
public final class ExtensionBeanSetup extends BeanSetup {

    /** A handle that can access BeanConfiguration#beanHandle. */
    private static final VarHandle VH_HANDLE = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), BeanConfiguration.class, "beanHandle",
            PackedBeanCustomizer.class);

    /** The extension the bean is a part of. */
    public final ExtensionSetup extension;

    /**
     * @param builder
     * @param owner
     */
    public ExtensionBeanSetup(ExtensionSetup extension, PackedBeanHandleBuilder<?> builder, RealmSetup owner) {
        super(builder, owner);
        this.extension = requireNonNull(extension);
    }

    public static ExtensionBeanSetup from(ExtensionBeanConfiguration<?> configuration) {
        PackedBeanCustomizer<?> bh = (PackedBeanCustomizer<?>) VH_HANDLE.get((BeanConfiguration) configuration);
        return (ExtensionBeanSetup) bh.bean();
    }
}

// public static final Key<OperationPack> OPERATION_PACK_KEY = Key.of(OperationPack.class);

//
//public PackedOperationPackSetup operationPack(@Nullable Key<OperationPack> key) {
//  PackedOperationPackSetup p = operationPack;
//  if (p == null) {
//      p = operationPack = new PackedOperationPackSetup();
//  }
//  return p;
//}
//
//public void provideOperationPack(BeanDependency provider) {
//  Key<?> key = provider.readKey();
//  @SuppressWarnings("unchecked")
//  PackedOperationPackSetup s = operationPack((Key<OperationPack>) key);
//  provider.provide(new Factory0<OperationPack>(() -> s.build()) {});
//}