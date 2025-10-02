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
package app.packed.cli.usage;

import app.packed.application.ApplicationMirror;
import app.packed.cli.CliCommandMirror;
import app.packed.cli.CliNamespaceMirror;
import app.packed.service.ServiceNamespaceMirror;

/**
 *
 */
public class MirrorUsage {

    public static void main(ApplicationMirror m) {
        CliNamespaceMirror cm = m.namespace(CliNamespaceMirror.class).get();
        for (CliCommandMirror c : cm.commands().toList()) {
            IO.println(c);
        }

        cm.commands().map(e -> e.bean().container()).distinct().toList();

        ServiceNamespaceMirror sn = m.namespace(ServiceNamespaceMirror.class).get();

        IO.println(sn);
    }


}
