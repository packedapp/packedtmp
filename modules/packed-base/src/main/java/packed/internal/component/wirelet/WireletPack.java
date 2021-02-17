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
import java.util.IdentityHashMap;
import java.util.List;

import app.packed.base.Nullable;
import app.packed.component.InheritableWirelet;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import packed.internal.component.ComponentBuild;
import packed.internal.component.PackedArtifactDriver;
import packed.internal.component.PackedComponentDriver;

/** A holder of wirelets and wirelet pipelines. */
public final class WireletPack {

    // We might at some point, allow the setting of a default name...
    // In which we need to different between not-set and set to null
    String name; // kan komme i map... og saa saetter vi et flag istedet for...

    // Vil gerne have det i en liste da det er lettere at parse op i value chainen...
    private ArrayList<Ent> inherited = new ArrayList<>();

    private final ArrayList<Ent> list = new ArrayList<>();

    private final IdentityHashMap<Class<? extends Wirelet>, Object> wirelets = new IdentityHashMap<>();

    /** Creates a new pack. */
    private WireletPack() {}

    /**
     * @param w
     */
    private void create0(Wirelet w) {
        WireletModel m = WireletModel.of(w.getClass());
        Class<? extends Extension> extensionType = m.extension;

        if (w instanceof InheritableWirelet) {
            inherited.add(new Ent(w, extensionType));
        }

        if (w instanceof BaseWirelet) {
            ((BaseWirelet) w).process(this);
        } else if (w instanceof WireletList) {
            for (Wirelet ww : ((WireletList) w).wirelets) {
                create0(ww);
            }
        } else {
            list.add(new Ent(w, extensionType));
            if (m.stackBy == null) {
                wirelets.put(w.getClass(), new Ent(w, extensionType));
            } else {

            }
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
    private static WireletPack create(WireletPack parent, Wirelet... wirelets) {
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
    public static WireletPack ofChild(@Nullable WireletPack parent, PackedComponentDriver<?> driver, Wirelet... wirelets) {
        if (driver.modifiers().isBundle()) {
            return create(parent, wirelets);
        }
        return null;
    }

    @Nullable
    public static WireletPack ofRoot(PackedArtifactDriver<?> pac, PackedComponentDriver<?> pcd, Wirelet... wirelets) {
        Wirelet w = Wirelet.combine(wirelets);
        if (pac.wirelet != null) {
            w = pac.wirelet.andThen(w);
        }
        if (pcd.modifiers().isBundle()) {
            return create(null, wirelets);
        }
        
//        Wirelet w = pac.wirelet;
//        if (pcd.wirelet != null) {
//            w = pcd.wirelet.andThen(w);
//        }
//        Wirelet all = Wirelet.combine(wirelets);
//        w = all.beforeThis(w);
//        if (pcd.modifiers().isBundle()) {
//            return create(null, wirelets);
//        }
        return null;
    }

    @Nullable
    public static WireletPack ofImage(ComponentBuild cnc, Wirelet... wirelets) {
        return create(null, wirelets);
    }

    public static class Ent {
        public final Class<? extends Extension> extensionType;

        public boolean isReceived;

        public final Wirelet wirelet;

        Ent(Wirelet wirelet, @Nullable Class<? extends Extension> extensionType) {
            this.wirelet = requireNonNull(wirelet);
            this.extensionType = extensionType;
        }
    }
}
