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

import app.packed.container.AnyBundle;
import app.packed.container.Bundle;
import app.packed.container.ContainerImage;

/**
 *
 */
public class VariousBundles {

    public static final ContainerImage EMPTY_IMAGE = ContainerImage.of(empty());
    public static final ContainerImage ONE_COMPONENT_IMAGE = ContainerImage.of(oneComponent());
    public static final ContainerImage FIVE_CONTAINER_IMAGE = ContainerImage.of(fiveComponents());
    public static final ContainerImage ONE_CONTAINER_IMAGE = ContainerImage.of(oneContainer());

    public static AnyBundle empty() {
        return new Bundle() {};
    }

    public static AnyBundle oneComponent() {
        return new Bundle() {

            @Override
            public void configure() {
                install("foo");
            }
        };
    }

    public static AnyBundle fiveComponents() {
        return new Bundle() {

            @Override
            public void configure() {
                install("foo").setName("1");
                install("foo").setName("2");
                install("foo").setName("3");
                install("foo").setName("4");
                install("foo").setName("5");
            }
        };
    }

    public static AnyBundle oneContainer() {
        return new Bundle() {

            @Override
            public void configure() {
                link(new Bundle() {});
            }
        };
    }
}
