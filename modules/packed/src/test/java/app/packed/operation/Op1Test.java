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
package app.packed.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.binding.Variable;
import testutil.stubs.Qualifiers.IntQualifier;

/** Tests {@link Op1}. */
public class Op1Test {

    /**
     * Tests that we can capture information about a simple factory producing {@link Integer} instances.
     */
    @Test
    public void simple() {
        Op1<String, Integer> f = new Op1<>(Integer::valueOf) {};
        assertEquals(OperationType.of(Integer.class, String.class), f.type());

    }

    @Test
    @Disabled
    public void qaulifier() {
        Op1<String, Integer> ff = new Op1<@IntQualifier(123) String, Integer>(Integer::valueOf) {};

        // Capturing annotations is whacked...
        Variable v = Variable.of(String.class, new IntQualifier() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return IntQualifier.class;
            }

            @Override
            public int value() {
                return 123;
            }
        });
        System.out.println(ff.type());
        assertEquals(OperationType.of(Variable.of(Integer.class), v), ff.type());

    }
}
