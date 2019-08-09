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
package packed.internal.invoke;

import app.packed.util.Nullable;
import app.packed.util.TypeLiteral;

/**
 *
 */
// Taenker vi extender InternalFactoryOfExecutable. I foerste omgang har vi kun
public class BindableFunctionHandle<T> extends FunctionHandle<T> {

    FunctionHandle<T> wrapping;

    public BindableFunctionHandle(TypeLiteral<T> typeLiteral) {
        super(typeLiteral);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public T invoke(Object[] params) {
        return null;
    }
}
