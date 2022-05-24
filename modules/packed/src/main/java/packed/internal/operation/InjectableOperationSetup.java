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

import java.lang.invoke.MethodType;

import app.packed.base.Key;
import app.packed.inject.FactoryType;
import app.packed.operation.InjectableOperationHandle;
import app.packed.operation.OperationPack;
import packed.internal.bean.ExtensionBeanSetup;
import packed.internal.bean.hooks.PackedBeanMember;

/**
 *
 */
public final class InjectableOperationSetup extends OperationSetup implements InjectableOperationHandle {

    /** The bean that invokes the operation. */
    final ExtensionBeanSetup operator;

    Key<OperationPack> operationPackKey;

    int packId = -1;

    /**
     * @param member
     */
    public InjectableOperationSetup(PackedBeanMember member, ExtensionBeanSetup operator) {
        super(member.bean, member, member.operator);
        this.operator = operator;
    }

    /** {@inheritDoc} */
    @Override
    public MethodType invocationType() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int pack() {
        int p = packId;
        if (p == -1) {
            p = packId = operator.operationPack(null).next();
        }
        return p;
    }

    /** {@inheritDoc} */
    @Override
    public int pack(Key<OperationPack> key) {
        int p = packId;
        if (p == -1) {
            p = packId = operator.operationPack(key).next();
        }
        return p;
    }

    /** {@inheritDoc} */
    @Override
    public FactoryType type() {
        return null;
    }
}
