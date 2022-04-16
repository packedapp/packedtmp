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
package packed.internal.bean.operation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;

import app.packed.bean.operation.InjectableOperation;
import app.packed.inject.FactoryType;
import packed.internal.bean.hooks.PackedBeanMember;

/**
 *
 */
public final class InjectableOperationSetup extends OperationSetup implements InjectableOperation {

    /**
     * @param member
     */
    public InjectableOperationSetup(PackedBeanMember member) {
        super(member.bean, member, member.extension);
    }

    /** {@inheritDoc} */
    @Override
    public void onReady(Consumer<MethodHandle> action) {}

    /** {@inheritDoc} */
    @Override
    public MethodType invocationType() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public FactoryType type() {
        return null;
    }
}