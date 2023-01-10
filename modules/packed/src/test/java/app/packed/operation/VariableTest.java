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

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import app.packed.binding.Variable;

/**
 *
 */
@SuppressWarnings("unused")
public class VariableTest {

    @Test
    public void testField() throws NoSuchFieldException {
        class XX {
            public int foo;
        }
        for (Field f : XX.class.getDeclaredFields()) {
            Variable v = Variable.ofField(f);
            assertThat(v.getRawType()).isEqualTo(f.getType());
            assertThat(v.getRawType()).isEqualTo(f.getGenericType());    
        }
        
    }

    public void assertFieldEquals(Field field) {
        Variable v = Variable.ofField(field);
    }

    public void assertAnnotatedElementEquals(AnnotatedElement expected, AnnotatedElement actual) {

    }
}
