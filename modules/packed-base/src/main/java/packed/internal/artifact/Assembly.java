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
package packed.internal.artifact;

import app.packed.component.Bundle;
import app.packed.component.Wirelet;
import packed.internal.container.PackedContainerRole;

/**
 *
 */
public class Assembly {

    /**
     * Creates an artifact image using the specified source.
     *
     * @param bundle
     *            the source of the image
     * @param wirelets
     *            any wirelets to use when construction the image. The wirelets will also be available when instantiating an
     *            actual artifact
     * @return the image that was built
     * @throws RuntimeException
     *             if the image could not be constructed
     */
    public static PackedArtifactImage newImage(Bundle<?> bundle, Wirelet... wirelets) {
        PackedContainerRole pcc = PackedContainerRole.assemble(PackedAccemblyContext.image(), bundle, wirelets);
        return new PackedArtifactImage(pcc, pcc.component.wireletContext);
    }

}
