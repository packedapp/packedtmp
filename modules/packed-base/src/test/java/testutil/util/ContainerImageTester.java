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
package testutil.util;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import app.packed.artifact.App;
import app.packed.artifact.Image;
import app.packed.component.Wirelet;
import app.packed.container.BaseBundle;

/**
 *
 */
public class ContainerImageTester {

    private final Image<App> image;

    public ContainerImageTester(BaseBundle source, Wirelet... wirelets) {
        this(App.newImage(source, wirelets));
    }

    public AppTester newApp(Wirelet... wirelets) {
        return new AppTester(image, wirelets);
    }

    public ContainerImageTester(Image<App> image) {
        this.image = requireNonNull(image);
    }

    public ContainerImageTester nameIs(String expected) {
        assertThat(image.component().name()).isEqualTo(expected);
        return this;
    }
}
