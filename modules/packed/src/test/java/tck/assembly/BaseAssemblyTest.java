/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package tck.assembly;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;
import tck.AppAppTest;
import tck.TckAssemblies.HelloWorldAssembly;

/** Various Assembly tests. */
public class BaseAssemblyTest extends AppAppTest {

    /** Tests that a assembly cannot be reused. */
    @Test
    public void notReusable() {
        HelloWorldAssembly hwa = new HelloWorldAssembly();
        App.run(hwa);
        assertThatThrownBy(() -> App.run(hwa)).isExactlyInstanceOf(IllegalStateException.class);
    }

    /** Tests that a assembly cannot link a used assembly. */
    @Test
    public void notResuableLink() {
        HelloWorldAssembly hwa = new HelloWorldAssembly();
        App.run(hwa);
        assertThatThrownBy(() -> link("child", hwa)).isExactlyInstanceOf(IllegalStateException.class);
    }

    /** Tests that a assembly cannot link itself. */
    @Test
    public void cannotLinkSelf() {
        BaseAssembly b = new BaseAssembly() {
            @Override
            protected void build() {
                link(this, "child");
            }
        };
        assertThatThrownBy(() -> App.run(b)).isExactlyInstanceOf(IllegalStateException.class);
    }
}
