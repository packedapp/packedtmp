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
package packed.internal.component;

import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.component.WireletHandle;
import packed.internal.base.application.PackedApplicationDriver;

/** A holder of wirelets and wirelet pipelines. */
public final class WireletWrapper {

    /** An empty wirelet wrapper. */
    static final WireletWrapper EMPTY = new WireletWrapper(WireletArray.EMPTY);

    /** The number of unconsumed wirelets. */
    int unconsumed;

    /** The wirelets we are wrapping. This array is safe for internal use. But must be cloned if exposed to users. */
    public final Wirelet[] wirelets;

    /** Creates a new pack. */
    WireletWrapper(Wirelet[] wirelets) {
        this.wirelets = wirelets;
        this.unconsumed = wirelets.length;
    }

    // Hooks kan ogsaa faa i Wirelets...
    public <T extends Wirelet> WireletHandle<T> handleOf(Module module, Class<? extends T> wireletClass) {
        // Maaske skal vi have en caller med ala "Must be in the same module as"
        if (module != wireletClass.getModule()) {
            throw new IllegalArgumentException("The specified wirelet must be in module " + module + ", was " + module.getName());
        }
        return new PackedWireletHandle<>(this, wireletClass);
    }

    @Nullable
    public static WireletWrapper forApplication(PackedApplicationDriver<?> applicationDriver, PackedComponentDriver<?> componentDriver, Wirelet... wirelets) {
        Wirelet[] ws;
        if (applicationDriver.wirelet == null) {
            ws = WireletArray.flatten(wirelets);
        } else {
            ws = WireletArray.flatten(applicationDriver.wirelet, Wirelet.combine(wirelets));
        }
        return new WireletWrapper(ws);
    }

    @Nullable
    public static WireletWrapper forComponent(PackedComponentDriver<?> driver, Wirelet... wirelets) {
        Wirelet[] ws = WireletArray.flatten(wirelets);
        return new WireletWrapper(ws);
    }

    @Nullable
    public static WireletWrapper forImageInstantiate(ComponentSetup component, Wirelet... wirelets) {
        Wirelet[] ws = WireletArray.flatten(wirelets);
        return new WireletWrapper(ws);
    }
}