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

import app.packed.artifact.ArtifactImage;
import app.packed.container.Bundle;
import app.packed.container.AnyBundle;

/**
 *
 */
public class VariousBundles {

    public static final ArtifactImage EMPTY_IMAGE = ArtifactImage.of(empty());
    public static final ArtifactImage ONE_COMPONENT_IMAGE = ArtifactImage.of(oneComponent());
    public static final ArtifactImage FIVE_CONTAINER_IMAGE = ArtifactImage.of(fiveComponents());
    public static final ArtifactImage ONE_CONTAINER_IMAGE = ArtifactImage.of(oneContainer());

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
