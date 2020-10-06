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
package packed.internal.inject.sidecar;

import app.packed.component.App;
import app.packed.container.BaseBundle;
import app.packed.inject.Provide2;

/**
 *
 */
public class NewProvides extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        install(F.class);
        export(String.class);
    }

    public static void main(String[] args) {
        App app = App.of(new NewProvides());
        System.out.println(app.use(String.class));
        System.out.println(app.use(String.class));
    }

    public static class F {

        @Provide2(constant = true)
        public String s() {
            return "f " + System.nanoTime();
        }
    }
}
