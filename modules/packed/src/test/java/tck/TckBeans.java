/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package tck;

import java.lang.reflect.Method;

import app.packed.application.Main;
import app.packed.lifecycle.Initialize;
import testutil.MemberFinder;
import testutil.stubs.Letters.A;

/**
 * Common beans that can be used for testing.
 */
public class TckBeans {

    public static class HelloMainBean {
        public static final Method METHOD = MemberFinder.findMethod("hello");

        @Main
        public void hello() {
            //IO.println("HelloWorld");
        }
    }

    public static class NeedsOnInitializeABean {
        public static final Method METHOD = MemberFinder.findMethod("needsA");

        @Initialize
        public void needsA(A a) {}
    }
}
