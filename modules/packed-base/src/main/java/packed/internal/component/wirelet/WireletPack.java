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
package packed.internal.component.wirelet;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.component.WireletHandle;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedApplicationDriver;
import packed.internal.component.PackedComponentDriver;

/** A holder of wirelets and wirelet pipelines. */
// Time to put this on component????
public /* primitive */ final class WireletPack {

    static final WireletPack EMPTY = new WireletPack(WireletList.EMPTY);

    // Tror faktisk vi laver det udpacked array i en wirelet list..
    // Fordi saa kan vi bare kopiere arrayet ind direkte her...
    final Wirelet[] wirelets;

    // We might at some point, allow the setting of a default name...
    // In which we need to different between not-set and set to null
    String name; // kan komme i map... og saa saetter vi et flag istedet for...

    int unconsumed;

    /** Creates a new pack. */
    WireletPack(Wirelet[] wirelets) {
        this.wirelets = wirelets;
        for (Wirelet w : wirelets) {
            if (w instanceof InternalWirelet bw) {
                bw.process(this);
            }
        }
    }

    public <T extends Wirelet> WireletHandle<T> handleOf(Module module, Class<? extends T> wireletClass) {
        // Maaske skal vi have en caller med ala "Must be in the same module as"
        if (module != wireletClass.getModule()) {
            throw new IllegalArgumentException("The specified wirelet must be in module " + module + ", was " + module.getName());
        }
        // We should probably check if we have any matches first and return empty?
        // Nah with primitive it should matter, we expect 1 run through anyway via
        // consume all
        return new PackedWireletHandle<>(this, wireletClass);
    }

    // That name wirelet.. should only be used by the top-container....
    @Nullable
    public String nameWirelet() {
        return name;
    }

    public static <T extends Wirelet> WireletHandle<T> extensionHandle(WireletPack containerWirelets, Class<? extends Extension> extensionClass,
            Class<? extends T> wireletClass) {
        requireNonNull(wireletClass, "wireletClass is null");
        if (extensionClass.getModule() != wireletClass.getModule()) {
            throw new InternalExtensionException("oops mismatching module");
        }
        if (containerWirelets == null) {
            return PackedWireletHandle.of();
        } else {
            return containerWirelets.handleOf(extensionClass.getModule(), wireletClass);
        }
    }

    @Nullable
    public static WireletPack ofChild(@Nullable WireletPack parent, PackedComponentDriver<?> driver, Wirelet... wirelets) {
        Wirelet[] ws = WireletList.flatten(wirelets);
        return new WireletPack(ws);
    }

    @Nullable
    public static WireletPack ofImage(ComponentSetup component, Wirelet... wirelets) {
        Wirelet[] ws = WireletList.flatten(wirelets);
        return new WireletPack(ws);
    }

    @Nullable
    public static WireletPack ofRoot(PackedApplicationDriver<?> pac, PackedComponentDriver<?> pcd, Wirelet... wirelets) {
        Wirelet[] ws;
        if (pac.wirelet != null) {
            requireNonNull(wirelets, "wirelets is null");
            ws = WireletList.flatten(pac.wirelet, Wirelet.combine(wirelets));
        } else {
            ws = WireletList.flatten(wirelets);
        }

        return new WireletPack(ws);
    }
}
