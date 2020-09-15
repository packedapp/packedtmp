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
package app.packed.base;

import app.packed.component.App;
import app.packed.container.BaseBundle;

/**
 *
 */
public class USage extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        installInstance("World").export();
    }

    public static void main(String[] args) {
        try (App app = App.of(new USage())) {
            System.out.println("Hello " + app.use(String.class));
        }
    }
}
