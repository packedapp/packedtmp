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
package packed.internal.hook.model.testit;

import java.lang.invoke.MethodHandles;

import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import packed.internal.container.access.ClassProcessor;
import packed.internal.hook.model.OnHookSet;

/**
 *
 */
public class Stuff {

    @OnHook
    private void foo(MyHook h) {

    }

    public static void main(String[] args) {
        OnHookSet ohs = new OnHookSet(new ClassProcessor(MethodHandles.lookup(), Stuff.class, false));
        ohs.process();
    }

    static class MyHook implements Hook {

        @OnHook
        private void foo(MyHook h) {

        }

    }
}
