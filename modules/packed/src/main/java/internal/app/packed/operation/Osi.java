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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.stream.IntStream;

import internal.app.packed.binding.BindingSetup;

/**
 *
 */
public class Osi {

    MethodHandle mh;
    int index;

    // Man kunne starte med at indsaette alle argumenter.
    // Og saa tage dem ind af gangen og reducere
    int nextIndex = 0;

    void process(OperationSetup os) {
        BindingSetup[] bindings = os.bindings;
        // System.out.println("----------------------");
        // System.out.println("Start " + mh.type());
        for (int i = 0; i < bindings.length; i++) {
            index = i;
            // System.out.println("BT " + bindings[i].getClass());

            if (bindings[i] == null) {
                System.out.println(os.type);
                System.out.println(i);
            }
            if (bindings[i].provider != null) {
                mh = bindings[i].provider.bindIntoOperation(this);
            } else {
//                throw new UnsupportedOperationException();
                mh = bindings[i].bindIntoOperation(this, mh);
            }
        }

        // System.out.println("Before touchup " + mh.type());
        // System.out.println("Number of bindings " + bindings.length);
        MethodType mt = os.template.invocationType();
        // Den her virker fordi vi kun har en parameter type
        // System.out.println("PERM");
        // System.out.println(Arrays.toString(is.toArray()));
        mh = MethodHandles.permuteArguments(mh, mt, is.toArray());

        // System.out.println("Finished " + mh.type());

    }

    public final IntStack is = new IntStack();

    /**
     * @param argumentIndex
     */
    public MethodHandle bindArgument(int argumentIndex) {
        // System.out.println("!@# " + mh.type());
        // throw new UnsupportedOperationException();
        is.push(argumentIndex);
        return mh;
    }

    public MethodHandle bindConstant(Object value) {
        return MethodHandles.insertArguments(mh, nextIndex, value);
        // nextIndex is the same
    }

    public MethodHandle bindOperation(OperationSetup operation) {
        MethodHandle methodHandle = operation.generateMethodHandle();
        // System.out.println("Current: " + mh.type());
        // System.out.println("X Generated " + methodHandle.type());
        MethodHandle m = MethodHandles.collectArguments(mh, nextIndex, methodHandle);
        // System.out.println("Rest" + m.type());
        nextIndex += methodHandle.type().parameterCount();
        is.push(IntStream.range(0, methodHandle.type().parameterCount()).toArray());
        return m;
    }
}
