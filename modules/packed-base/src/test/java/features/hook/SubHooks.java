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
package features.hook;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.junit.jupiter.api.Test;

import app.packed.hook.AssignableToHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import features.hook.HookStubs.Aggregate;
import testutil.stubs.annotation.Left;

/**
 *
 */
public class SubHooks {

    @Left
    String s = "foo";

    @Left
    String ss = "foo";

    @Left
    String s1 = "foo";

    @Left
    String s2 = "foo";

    @Test
    public void foo() {
        Aggregate a = Hook.Builder.test(MethodHandles.lookup(), HookStubs.Aggregate.class, SubHooks.class);

        assertThat(a.laf.fields.stream().mapToInt(e -> e.field().getName().length()).sum()).isSameAs(7);
    }

    @OnHook
    void food(AssignableToHook<? extends List<?>> hook) {

    }
}
