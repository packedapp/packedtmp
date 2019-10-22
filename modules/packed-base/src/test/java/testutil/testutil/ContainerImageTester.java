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
package testutil.testutil;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import app.packed.artifact.ArtifactImage;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;

/**
 *
 */
public class ContainerImageTester {

    private final ArtifactImage image;

    public ContainerImageTester(ContainerSource source, Wirelet... wirelets) {
        this(ArtifactImage.of(source, wirelets));
    }

    public AppTester newApp(Wirelet... wirelets) {
        return new AppTester(image, wirelets);
    }

    public ContainerImageTester(ArtifactImage image) {
        this.image = requireNonNull(image);
    }

    public ContainerImageTester nameIs(String expected) {
        assertThat(image.name()).isEqualTo(expected);
        return this;
    }
}
