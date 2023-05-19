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
package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import app.packed.application.BuildGoal;
import app.packed.application.DeploymentMirror;

/**
 *
 */
public final class DeploymentSetup {

    // Har vi en strong reference her??
    // Hvad hvis vi launcher? Kan vi foerst smide setups vaek naar vi har
    // launched alt? Altsaa det betyder jo hvis vi har lazy kan vi
    // aldrig smide noget vaek.
    // Maaske smider vi som udgangspunkt ikke noget vaek
    public final ApplicationSetup root;

    /** The build goal. */
    public final BuildGoal goal;

    public final PackedLocalMap locales = new PackedLocalMap();

    DeploymentSetup(ApplicationSetup root, PackedContainerBuilder containerBuilder) {
        this.root = requireNonNull(root);
        this.goal = containerBuilder.goal();
    }

    /**
     * @return
     */
    public DeploymentMirror mirror() {
        throw new UnsupportedOperationException();
    }
}
