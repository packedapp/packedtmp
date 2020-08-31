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

import app.packed.component.Bundle;
import app.packed.component.Component;
import app.packed.component.Wirelet;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedAssemblyContext;
import packed.internal.component.PackedComponentModifierSet;
import packed.internal.component.PackedInitializationContext;
import packed.internal.component.wirelet.WireletPack;

/** The default implementation of {@link Image}. */
// Taenker vi maaske skal flytte den internt?
// Altsaa ved ikke lige hvordan det hosted image kommer til at fungere...
final class PackedImage<A> implements Image<A> {

    /** The driver used to create the any shell. */
    // We should use the driver when creating the actual root node...
    // I'm not sure we should need to store it.
    private final ShellDriver<A> driver;

    /** The assembled image node. */
    private final ComponentNodeConfiguration node;

    /**
     * Creates a new image from the specified configuration and wirelets.
     * 
     * @param driver
     *            the artifact driver
     * @param bundle
     *            the artifact source
     * @param wirelets
     *            any wirelet
     */
    PackedImage(ShellDriver<A> driver, Bundle<?> bundle, Wirelet... wirelets) {
        this.node = PackedAssemblyContext.assemble(PackedComponentModifierSet.I_IMAGE + driver.modifiers, bundle, driver, wirelets);
        this.driver = driver;
    }

    /** {@inheritDoc} */
    @Override
    public Component component() {
        return node.adaptToComponent();
    }

    /** {@inheritDoc} */
    @Override
    public A initialize(Wirelet... wirelets) {
        PackedInitializationContext context = PackedInitializationContext.initialize(node, WireletPack.forImage(node, wirelets));
        return driver.newShell(context);
    }

    /** {@inheritDoc} */
    @Override
    public A start(Wirelet... wirelets) {
        return initialize(wirelets);
    }
}
