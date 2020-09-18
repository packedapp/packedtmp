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
package packed.internal.sidecar;

import java.time.LocalDateTime;

import app.packed.inject.Provide;
import app.packed.sidecar.Invoker;
import app.packed.sidecar.MethodSidecar;
import app.packed.statemachine.OnInitialize;

/**
 *
 */
public class TestIt extends MethodSidecar {

    @Override
    protected void configure() {
        debug();
    }

    @OnInitialize
    public static void foo(Invoker i) throws Throwable {
        i.invoke();
        i.invoke();
    }

    @Provide
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
