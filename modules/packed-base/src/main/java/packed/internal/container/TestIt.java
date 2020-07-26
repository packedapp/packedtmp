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
package packed.internal.container;

import app.packed.artifact.App;
import app.packed.container.DefaultBundle;
import app.packed.service.Injector;

/**
 *
 */
public class TestIt extends DefaultBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        provideConstant("Hejhej");
    }

    public static void main(String[] args) {
        try (App app = App.of(new TestIt())) {
            System.out.println(app.use(Injector.class));
            System.out.println(app.use(String.class));
        }
    }
}