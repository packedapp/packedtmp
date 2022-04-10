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
package packed.internal.bean.hooks.usesite;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import app.packed.base.Nullable;
import app.packed.bean.operation.OperationConfiguration;
import app.packed.bean.operation.OperationMirror;

/**
 *
 */
public class PackedHookOperationConfiguration implements OperationConfiguration {

    @Nullable
    Supplier<? extends OperationMirror> supplier;

    /** {@inheritDoc} */
    @Override
    public void useMirror(Supplier<? extends OperationMirror> supplier) {
        this.supplier = requireNonNull(supplier, "supplier is null");
    }
}