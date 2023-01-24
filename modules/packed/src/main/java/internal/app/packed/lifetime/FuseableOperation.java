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
package internal.app.packed.lifetime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayDeque;
import java.util.List;

import app.packed.framework.Nullable;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;

/**
 *
 */
public final class FuseableOperation {

    public final OperationTemplate template;

    public final ArrayDeque<OperationHandle> operations = new ArrayDeque<>();

    public final ArrayDeque<MethodHandle> methodHandles = new ArrayDeque<>();
    
    public FuseableOperation(OperationTemplate template) {
        this.template = requireNonNull(template);
    }

    static List<FuseableOperation> of(@Nullable List<OperationTemplate> templates) {
        if (templates == null) {
            return List.of();
        }
        return templates.stream().map(FuseableOperation::new).toList();
    }
}
