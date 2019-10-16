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
package packed.internal.service.build.dependencies;

import app.packed.artifact.app.App;
import app.packed.container.BaseBundle;
import app.packed.service.Provide;

/**
 *
 */
public class Stiff extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        provide(XXX.class);
    }

    public static void main(String[] args) {
        App.of(new Stiff());
    }

    public static class XXX {

        public XXX(String ss) {

        }

        @Provide
        public String fooo() {
            return "ddd";
        }
    }
}
