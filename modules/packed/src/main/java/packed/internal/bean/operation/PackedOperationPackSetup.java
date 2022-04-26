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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.operation.OperationPack;

/**
 *
 */
public class PackedOperationPackSetup {

    private MethodHandle[] array;

    private int slotCounter;

    public int next() {
        return slotCounter++;
    }
    
    public void onGenerated(int index, MethodHandle methodHandle) {
        MethodHandle[] a = array;
        if (a == null) {
            a = array = new MethodHandle[slotCounter];
        }
        array[index] = methodHandle;
    }

    public OperationPack build() {
        // FreezeArray();
        for (int i = 0; i < array.length; i++) {
            requireNonNull(array[i]);
        }
        return new PackedOperationPack(array);
    }

    public record PackedOperationPack(MethodHandle[] methodsHandles) implements OperationPack {}
}
