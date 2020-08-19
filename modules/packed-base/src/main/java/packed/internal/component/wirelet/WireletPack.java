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

import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.container.PackedContainerRole;

/** A holder of wirelets and wirelet pipelines. */
public final class WireletPack {

    // We might at some point, allow the setting of a default name...
    // In which we need to different between not-set and set to null
    String newName; // kan komme i map... og saa saetter vi et flag istedet for...

    private ArrayList<Ent> list = new ArrayList<>();

    /**
     * Creates a new pack.
     * 
     */
    private WireletPack() {}

    /**
     * @param w
     */
    private void create0(Wirelet w) {
        Class<? extends Extension> extensionType = WireletModel.of(w.getClass()).extension;
        if (w instanceof InternalWirelet) {
            // Hmm skulle vi vente til alle wirelets er succesfuld processeret???
            // Altsaa hvad hvis den fejler.... Altsaa taenker ikke den maa lavere aendringer i containeren.. kun i wirelet context
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

    // That name wirelet.. should only be used by the top-container....
    @Nullable
    public String nameWirelet() {
        return newName;
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

    @Nullable
    public static WireletPack from(ComponentNodeConfiguration node, Wirelet... wirelets) {
        if (node.driver().isContainer()) {
            return create(wirelets);
        }
        return null;
    }

    @Nullable
    public static WireletPack fromImage(PackedContainerRole pcc, Wirelet... wirelets) {
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
