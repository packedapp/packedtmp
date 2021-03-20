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

import app.packed.component.PreviousKnownAsApp;
import app.packed.component.ApplicationImage;
import app.packed.container.BaseAssembly;

/**
 *
 */
public class VariousImages {

    public static final ApplicationImage<PreviousKnownAsApp> EMPTY_IMAGE = PreviousKnownAsApp.buildImage(empty());
    public static final ApplicationImage<PreviousKnownAsApp> ONE_COMPONENT_IMAGE = PreviousKnownAsApp.buildImage(oneComponent());
    public static final ApplicationImage<PreviousKnownAsApp> FIVE_CONTAINER_IMAGE = PreviousKnownAsApp.buildImage(fiveComponents());
    public static final ApplicationImage<PreviousKnownAsApp> ONE_CONTAINER_IMAGE = PreviousKnownAsApp.buildImage(oneContainer());

    public static BaseAssembly empty() {
        return new BaseAssembly() {
            @Override
            protected void build() {}
        };
    }

    public static BaseAssembly oneComponent() {
        return new BaseAssembly() {

            @Override
            public void build() {
                installInstance("foo");
            }
        };
    }

    public static BaseAssembly fiveComponents() {
        return new BaseAssembly() {

            @Override
            public void build() {
                installInstance("foo").setName("1");
                installInstance("foo").setName("2");
                installInstance("foo").setName("3");
                installInstance("foo").setName("4");
                installInstance("foo").setName("5");
            }
        };
    }

    public static BaseAssembly oneContainer() {
        return new BaseAssembly() {

            @Override
            public void build() {
                link(new BaseAssembly() {
                    @Override
                    protected void build() {}
                });
            }
        };
    }
}
