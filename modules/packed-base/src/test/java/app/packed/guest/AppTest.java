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
package app.packed.guest;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import app.packed.application.Program;
import app.packed.container.BaseAssembly;
import app.packed.container.BaseContainerConfiguration;

/** Tests {@link Program} (PackedApp). */
public class AppTest {

    /** Tests an empty app. */
    @Test
    public void emptyApp() {
        AtomicReference<BaseContainerConfiguration> ar = new AtomicReference<>();
        Program app = Program.start(new BaseAssembly() {
            @Override
            public void build() {
                ar.set(configuration());
            }
        });

        assertThat(app).isNotNull();
        assertThat(app.name()).isNotNull();

        BaseContainerConfiguration cc = requireNonNull(ar.get());

        // Checks that no extensions are installed by default
        assertThat(cc.extensions()).isEmpty();

        // Checks the config site of the app is the same as the config site of the configuration
    }
}
