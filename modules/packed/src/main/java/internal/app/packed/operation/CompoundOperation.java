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
package internal.app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayDeque;

import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;

/**
 * An op
 */

// Ideen er at vi kan kalde flere operation med en enkelt MH.

// Fx
// Factory
// Inject
// Initialization


// Hvad er signature for Inject? BeanClass I guess (We are not reading from anywhere)

// Jeg tror faktisk vi koere inject/initialization fra constant pool til at starte med.


public final class CompoundOperation {

    public final PackedOperationTemplate template;

    public final ArrayDeque<OperationHandle<?>> operations = new ArrayDeque<>();

    public final ArrayDeque<MethodHandle> methodHandles = new ArrayDeque<>();

    public CompoundOperation(OperationTemplate template) {
        this.template = (PackedOperationTemplate) requireNonNull(template);
    }
//
//    public static List<CompoundOperation> of(@Nullable List<OperationTemplate> templates) {
//        if (templates == null) {
//            return List.of();
//        }
//        return templates.stream().map(CompoundOperation::new).toList();
//    }
}
