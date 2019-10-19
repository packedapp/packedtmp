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
package packed.internal.ztests;

import app.packed.artifact.App;
import app.packed.artifact.ArtifactImage;
import app.packed.container.BaseBundle;
import app.packed.service.ServiceWirelets;

/**
 *
 */
public class Sssss extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        export(provideInstance("foo"));
        export(provideInstance(123));

    }

    public static void main(String[] args) {
        ArtifactImage ai = ArtifactImage.of(Sssss.class, ServiceWirelets.peekDownstream(e -> System.out.println("Exporting " + e.key())));
        App a = App.of(ai, ServiceWirelets.peekDownstream(e -> System.out.println("ExportingX " + e.key())));
        System.out.println(a);
    }
}
