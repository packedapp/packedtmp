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
package app.packed.bundle.x;

import java.util.function.Consumer;

import app.packed.bundle.Bundle;
import app.packed.bundle.Layer;

/**
 *
 */
public abstract class Bundle2 extends Bundle {

    public void use(Layer layer) {}

    // Er det samme som wire???
    public Layer useLayer(Bundle b, Layer... parents) {
        return null;
    }

    // Er det samme som wire???
    // maybe tmp layer...? Nej, fordi vi vil jo stadig gerne definere containeren....
    public Layer layer(Bundle b, Layer... parents) {
        return null;
    }

    public Layer layer(Consumer<BundleLayerConfiguration> c, Layer... parents) {
        return null;
    }
}
