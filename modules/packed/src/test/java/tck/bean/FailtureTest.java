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
package tck.bean;

import org.junit.jupiter.api.Test;

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;

/**
 *
 */
public class FailtureTest {

    @Test
    public void test() {
        // We had some issues with provideAs not working properly when generating code.
        App.run(new Hmm2());
    }

    public static class Hmm2 extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            install(RAR.class).provideAs(AR.class);
            install(MyBean.class);
        }

        public sealed interface AR permits RAR {}

        public static final class RAR implements AR {}

        public static class MyBean {

            public MyBean(AR repo) {
                IO.println(repo);
            }
        }
    }

}
