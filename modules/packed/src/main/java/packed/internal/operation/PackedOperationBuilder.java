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
package packed.internal.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.operation.OperationBuilder;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationPack;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.ExtensionBeanSetup;
import packed.internal.bean.hooks.PackedBeanMember;
import packed.internal.container.ExtensionSetup;

/**
 *
 */
public final class PackedOperationBuilder implements OperationBuilder {

    /** The bean the operation is a part of. */
    final BeanSetup bean;

    final ExtensionSetup extensionSetup;

    MethodHandle methodHandle;

    /** Supplies a mirror for the operation */
    Supplier<? extends OperationMirror> mirrorSupplier = OperationMirror::new;

    Key<OperationPack> operationPackKey;

    /** The bean that invokes the operation. */
    final ExtensionBeanSetup operatorBean;

    int packId = -1;

    final PackedOperationTarget target;

    /**
     * @param member
     */
    public PackedOperationBuilder(PackedBeanMember<?> member, ExtensionBeanSetup operatorBean, MethodHandle methodHandle) {
        this.bean = member.bean;
        this.extensionSetup = member.operator;
        this.target = member;
        this.operatorBean = operatorBean;
        this.methodHandle = methodHandle;
    }

    /** {@inheritDoc} */
    @Override
    public int build() {
        int p = packId;
        if (p == -1) {
            p = packId = operatorBean.operationPack(null).next();
        }
        build0();
        return p;
    }

    /** {@inheritDoc} */
    @Override
    public int build(Key<OperationPack> key) {
        int p = packId;
        if (p == -1) {
            p = packId = operatorBean.operationPack(key).next();
        }
        build0();
        return p;
    }

    private void build0() {
        OperationSetup os = new OperationSetup(this);
        bean.addOperation(os);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle buildNow() {
        build0();
        return methodHandle;
    }

    public OperationBuilder specializeMirror(Supplier<? extends OperationMirror> supplier) {
        requireNonNull(supplier, "supplier is null");
        this.mirrorSupplier = supplier;
        return (OperationBuilder) this;
    }
}
