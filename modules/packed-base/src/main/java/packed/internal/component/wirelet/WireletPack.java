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

import java.util.ArrayList;
import java.util.List;

import app.packed.base.Nullable;
import app.packed.block.Extension;
import app.packed.component.Wirelet;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedComponentDriver;

/** A holder of wirelets and wirelet pipelines. */
public final class WireletPack {

    // We might at some point, allow the setting of a default name...
    // In which we need to different between not-set and set to null
    String name; // kan komme i map... og saa saetter vi et flag istedet for...

    private ArrayList<Ent> list = new ArrayList<>();

    /** Creates a new pack. */
    private WireletPack() {}

    /**
     * @param w
     */
    private void create0(Wirelet w) {
        Class<? extends Extension> extensionType = WireletModel.of(w.getClass()).extension;
        if (w instanceof InternalWirelet) {
            ((InternalWirelet) w).process(this);
        } else if (w instanceof WireletList) {
            for (Wirelet ww : ((WireletList) w).wirelets) {
                create0(ww);
            }
        } else {
            list.add(new Ent(w, extensionType));
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <W extends Wirelet> W receiveLast(Class<W> type) {
        for (int i = list.size() - 1; i >= 0; i--) {
            Ent e = list.get(i);
            if (type.isAssignableFrom(e.wirelet.getClass())) {
                e.isReceived = true;
                return (W) e.wirelet;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <W extends Wirelet> List<W> receiveAll(Class<W> type) {
        ArrayList<W> result = null;
        for (Ent e : list) {
            if (type.isAssignableFrom(e.wirelet.getClass())) {
                e.isReceived = true;
                if (result == null) {
                    result = new ArrayList<>(1);
                }
                result.add((W) e.wirelet);
            }
        }
        return result == null ? List.of() : List.copyOf(result);
    }

    // That name wirelet.. should only be used by the top-container....
    @Nullable
    public String nameWirelet() {
        return name;
    }

    /**
     * Creates a new wirelet pack or returns existing if the array of wirelets is empty.
     * 
     * @param wirelets
     *            the wirelets
     * @return stuff
     */
    @Nullable
    private static WireletPack create(Wirelet... wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        if (wirelets.length == 0) {
            return null;
        }

        WireletPack wc = new WireletPack();
        for (Wirelet w : wirelets) {
            requireNonNull(w, "wirelets contained a null");
            wc.create0(w);
        }
        return wc;
    }

    // //Is Initializaing in one -> NotAnImage and Not analyzing...

    @Nullable
    public static WireletPack from(PackedComponentDriver<?> driver, Wirelet... wirelets) {
        if (driver.modifiers().isContainer()) {
            return create(wirelets);
        }
        return null;
    }

    @Nullable
    public static WireletPack forImage(ComponentNodeConfiguration cnc, Wirelet... wirelets) {
        return create(wirelets);
    }

    public static class Ent {
        @Nullable
        public final Class<? extends Extension> extensionType;

        public boolean isReceived;

        public final Wirelet wirelet;

        Ent(Wirelet wirelet, @Nullable Class<? extends Extension> extensionType) {
            this.wirelet = requireNonNull(wirelet);
            this.extensionType = extensionType;
        }
    }

}
