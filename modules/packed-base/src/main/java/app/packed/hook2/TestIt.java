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
package app.packed.hook2;

import java.util.function.Consumer;

import app.packed.component.ComponentConfiguration;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.OnHook;
import app.packed.lifecycle.Main;

/**
 *
 */
public class TestIt {

    @OnHook
    public void foo(AnnotatedFieldHook<Main> h, ComponentConfiguration cc) {
        Consumer<? super Number> a = h.newSetAccessor(cc, Number.class);

        a.accept(23);
    }

    @OnHook
    public void foo(Consumer<Number> c2, Consumer<? super Number> c3) {
        Integer s = 123;
        c2.accept(s);
        c3.accept(s);
    }
}
