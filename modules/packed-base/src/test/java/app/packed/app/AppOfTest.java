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
package app.packed.app;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import app.packed.container.Bundle;
import app.packed.container.ContainerConfiguration;

/** Tests {@link App#of(app.packed.container.AnyBundle, app.packed.container.Wirelet...)} */
public class AppOfTest {

    /** Tests the empty app with no components. */
    @Test
    public void emptyApp() {
        AtomicReference<ContainerConfiguration> ar = new AtomicReference<>();
        App empty = App.of(new Bundle() {
            @Override
            public void configure() {
                ar.set(configuration());
            }
        });

        assertThat(empty).isNotNull();
        assertThat(empty.description()).isEmpty();
        assertThat(empty.name()).isNotNull();
        assertThat(empty.configurationSite()).isNotNull();
        assertThat(empty.stream()).size().isEqualTo(1);

        ContainerConfiguration cc = requireNonNull(ar.get());

        // Checks that no extensions are installed by default
        assertThat(cc.extensions()).isEmpty();

        // Check that configuration site
        assertThat(empty.configurationSite()).isSameAs(cc.configurationSite());
    }
}
