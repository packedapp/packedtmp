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
package app.packed.cli.usage;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.BaseAssembly;
import app.packed.cli.CliExtension;
import app.packed.cli.CliOverviewMirror;

/**
 *
 */
public class Usage extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        CliExtension e = use(CliExtension.class);

       // e.namespace().noPropagation();

//        ServiceableBeanConfiguration<String> i = install(String.class);

        e.addCliCommand(_ -> {}).names("asdsad");

    }

    public static void main(String[] args) {
        ApplicationMirror m = App.mirrorOf(new Usage());
        m.overview(CliOverviewMirror.class);
    }

}
