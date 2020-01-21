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
package app.packed.artifact;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import app.packed.artifact.App;
import app.packed.container.BaseBundle;
import app.packed.container.ContainerConfiguration;
import packed.internal.artifact.PackedApp;

/** Tests {@link App#start(app.packed.container.Bundle, app.packed.container.Wirelet...)} */
public class AppOfTest {

    /** Tests an empty app. */
    @Test
    public void emptyApp() {
        AtomicReference<ContainerConfiguration> ar = new AtomicReference<>();
        App app = App.start(new BaseBundle() {
            @Override
            public void configure() {
                ar.set(configuration());
            }
        });

        assertThat(app).isInstanceOf(PackedApp.class);

        assertThat(app).isNotNull();
        assertThat(app.description()).isEmpty();
        assertThat(app.name()).isNotNull();
        assertThat(app.configSite()).isNotNull();
        assertThat(app.stream()).size().isEqualTo(1);

        ContainerConfiguration cc = requireNonNull(ar.get());

        // Checks that no extensions are installed by default
        assertThat(cc.extensions()).isEmpty();

        // Checks the config site of the app is the same as the config site of the configuration
        assertThat(app.configSite()).isSameAs(cc.configSite());
    }
}
