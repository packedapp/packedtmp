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
package micro.app;

import app.packed.application.ApplicationImage;
import app.packed.application.programs.Program;
import app.packed.container.BaseBundle;

/**
 *
 */
public class VariousImages {

    public static final ApplicationImage<Program> EMPTY_IMAGE = Program.imageOf(empty());
    public static final ApplicationImage<Program> ONE_COMPONENT_IMAGE = Program.imageOf(oneComponent());
    public static final ApplicationImage<Program> FIVE_CONTAINER_IMAGE = Program.imageOf(fiveComponents());
    public static final ApplicationImage<Program> ONE_CONTAINER_IMAGE = Program.imageOf(oneContainer());

    public static BaseBundle empty() {
        return new BaseBundle() {
            @Override
            protected void build() {}
        };
    }

    public static BaseBundle oneComponent() {
        return new BaseBundle() {

            @Override
            public void build() {
                installInstance("foo");
            }
        };
    }

    public static BaseBundle fiveComponents() {
        return new BaseBundle() {

            @Override
            public void build() {
                installInstance("foo").named("1");
                installInstance("foo").named("2");
                installInstance("foo").named("3");
                installInstance("foo").named("4");
                installInstance("foo").named("5");
            }
        };
    }

    public static BaseBundle oneContainer() {
        return new BaseBundle() {

            @Override
            public void build() {
                link(new BaseBundle() {
                    @Override
                    protected void build() {}
                });
            }
        };
    }
}
