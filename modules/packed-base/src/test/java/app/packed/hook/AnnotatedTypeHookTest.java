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
package app.packed.hook;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import packed.internal.hook.UnreflectGate;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.UncheckedThrowableFactory;
import testutil.stubs.annotation.AnnotationInstances;
import testutil.stubs.annotation.Left;

/** Tests {@link AnnotatedTypeHook}. */
public class AnnotatedTypeHookTest {

    /**
     * Tests the basics.
     * 
     * @see AnnotatedTypeHook#annotation()
     * @see AnnotatedTypeHook#type()
     */
    @Test
    public void basics() {
        AnnotatedTypeHook<Left> h = new AnnotatedTypeHook<>(newHookController(), Object.class, AnnotationInstances.LEFT);
        assertThat(h.annotation()).isSameAs(AnnotationInstances.LEFT);
        assertThat(h.type()).isSameAs(Object.class);
    }

    private static UnreflectGate newHookController() {
        ClassProcessor cp = new ClassProcessor(MethodHandles.lookup(), AnnotatedTypeHookTest.class, false);
        return new UnreflectGate(cp, UncheckedThrowableFactory.ASSERTION_ERROR);
    }
}
