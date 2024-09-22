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
package tck.bean.hooks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.util.AnnotationList;
import tck.AppAppTest;
import tck.HookTestingExtension.FieldHook.FieldPrivateInstanceString;

/**
 * This class this tests the various elements on {@link app.packed.extension.BeanIntrospector} that does not create
 * operations or bindings.
 */
public class BeanIntrospectorElementTest extends AppAppTest {

    /**
     *
     * @see app.packed.extension.BeanElement.BeanField#
     */
    @Test
    public void onAnnotatedField() {
        Field f = FieldPrivateInstanceString.FOO_FIELD;
        hooks().onAnnotatedField((l, b) -> {
            assertEquals(AnnotationList.fromField(f), l);
            assertEquals(AnnotationList.fromField(f), b.annotations());
            assertEquals(f, b.field());
            assertEquals(f.getModifiers(), b.modifiers());
            assertEquals(Key.of(String.class), b.toKey());
            assertEquals(Variable.of(String.class, f.getAnnotations()), b.variable());
            trigger();
        });
        install(FieldPrivateInstanceString.class);
        assertTriggered();
    }
}
