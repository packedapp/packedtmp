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

import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.inject.Factory;
import app.packed.operation.dependency.DependencyProvider;
import packed.internal.base.PackedVariable;
import packed.internal.bean.operation.OperationSetup;

/**
 *
 */
// Ligger der altid en operation bag her????
// Der maa ligge noget Dependency her

public record PackedDependencyProvider(OperationSetup operation, int variableIndex) implements DependencyProvider {

    /** {@inheritDoc} */
    @Override
    public Object annotations() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void provide(Factory<?> fac) {}

    /** {@inheritDoc} */
    @Override
    public void provide(MethodHandle methodHandle) {}

    /** {@inheritDoc} */
    @Override
    public void provideInstance(@Nullable Object obj) {}

    /** {@inheritDoc} */
    @Override
    public void provideMissing() {}

    /** {@inheritDoc} */
    @Override
    public void requireContext(Class<?> contextType) {}


    /** {@inheritDoc} */
    @Override
    public PackedVariable variable() {
        return operation.variable(variableIndex);
    }
}
