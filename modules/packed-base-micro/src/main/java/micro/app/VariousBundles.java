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
import app.packed.component.ComponentExtension;
import app.packed.container.Bundle;

/**
 *
 */
public class VariousBundles {

    public static final ArtifactImage EMPTY_IMAGE = ArtifactImage.build(empty());
    public static final ArtifactImage ONE_COMPONENT_IMAGE = ArtifactImage.build(oneComponent());
    public static final ArtifactImage FIVE_CONTAINER_IMAGE = ArtifactImage.build(fiveComponents());
    public static final ArtifactImage ONE_CONTAINER_IMAGE = ArtifactImage.build(oneContainer());

    public static Bundle empty() {
        return new Bundle() {
            @Override
            protected void configure() {}
        };
    }

    public static Bundle oneComponent() {
        return new Bundle() {

            @Override
            public void configure() {
                use(ComponentExtension.class).installInstance("foo");
            }
        };
    }

    public static Bundle fiveComponents() {
        return new Bundle() {

            @Override
            public void configure() {
                use(ComponentExtension.class).installInstance("foo").setName("1");
                use(ComponentExtension.class).installInstance("foo").setName("2");
                use(ComponentExtension.class).installInstance("foo").setName("3");
                use(ComponentExtension.class).installInstance("foo").setName("4");
                use(ComponentExtension.class).installInstance("foo").setName("5");
            }
        };
    }

    public static Bundle oneContainer() {
        return new Bundle() {

            @Override
            public void configure() {
                link(new Bundle() {
                    @Override
                    protected void configure() {}
                });
            }
        };
    }
}
