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

import app.packed.component.App;
import app.packed.component.Image;
import app.packed.container.BaseBundle;

/**
 *
 */
public class VariousBundles {

    public static final Image<App> EMPTY_IMAGE = App.imageOf(empty());
    public static final Image<App> ONE_COMPONENT_IMAGE = App.imageOf(oneComponent());
    public static final Image<App> FIVE_CONTAINER_IMAGE = App.imageOf(fiveComponents());
    public static final Image<App> ONE_CONTAINER_IMAGE = App.imageOf(oneContainer());

    public static BaseBundle empty() {
        return new BaseBundle() {
            @Override
            protected void configure() {}
        };
    }

    public static BaseBundle oneComponent() {
        return new BaseBundle() {

            @Override
            public void configure() {
                installInstance("foo");
            }
        };
    }

    public static BaseBundle fiveComponents() {
        return new BaseBundle() {

            @Override
            public void configure() {
                installInstance("foo").setName("1");
                installInstance("foo").setName("2");
                installInstance("foo").setName("3");
                installInstance("foo").setName("4");
                installInstance("foo").setName("5");
            }
        };
    }

    public static BaseBundle oneContainer() {
        return new BaseBundle() {

            @Override
            public void configure() {
                link(new BaseBundle() {
                    @Override
                    protected void configure() {}
                });
            }
        };
    }
}
