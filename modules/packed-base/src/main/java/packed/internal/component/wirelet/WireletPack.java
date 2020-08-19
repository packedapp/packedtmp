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
import java.util.Map;

import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.container.PackedContainerRole;

/** A holder of wirelets and wirelet pipelines. */
public final class WireletPack {

    // Could put them in wirelets. And then have an int countdown instead... every time an extension is removed.
    private final IdentityHashMap<Class<? extends Extension>, Object> extensions = new IdentityHashMap<>();

    /** A map of wirelets and wirelet pipelines. */
    private final IdentityHashMap<Class<?>, Object> map = new IdentityHashMap<>();

    // We might at some point, allow the setting of a default name...
    // In which we need to different between not-set and set to null
    String newName; // kan komme i map... og saa saetter vi et flag istedet for...

    /**
     * Creates a new pack.
     * 
     */
    private WireletPack() {}

    /**
     * This method checks that no wirelets have been specified that requires an extensions that have not been used.
     */
    public void checkAllExtensionsAvailable(PackedContainerRole pcc) {
        if (!extensions.isEmpty()) {
            extensionFailed(pcc);
        }
    }

    public static class E {
        public final Wirelet wirelet;

        @Nullable
        public final Class<? extends Extension> extensionType;

        E(Wirelet wirelet, @Nullable Class<? extends Extension> extensionType) {
            this.wirelet = requireNonNull(wirelet);
            this.extensionType = extensionType;
        }

        public boolean isReceived;
    }

    /**
     * @param w
     */
    private void create0(Wirelet w) {
        // Class<? extends Extension> cl = WireletModel.of(w.getClass()).extension;
        if (w instanceof InternalWirelet) {
            // Hmm skulle vi vente til alle wirelets er succesfuld processeret???
            // Altsaa hvad hvis den fejler.... Altsaa taenker ikke den maa lavere aendringer i containeren.. kun i wirelet context
            ((InternalWirelet) w).process(this);
        } else if (w instanceof WireletList) {
            for (Wirelet ww : ((WireletList) w).wirelets) {
                create0(ww);
            }
        } else {
            map.put(w.getClass(), w); // override any existing wirelet
        }
    }

    private void extensionFailed(PackedContainerRole pcc) {
        IdentityHashMap<Class<? extends Extension>, ArrayList<Wirelet>> m = new IdentityHashMap<>();
        for (Map.Entry<Class<? extends Extension>, Object> c : extensions.entrySet()) {
            Class<? extends Extension> k = c.getKey();
            if (pcc.getContext(k) == null) {

            }
        }
//      throw new IllegalArgumentException("In order to use the wirelet(s) " + wpc.wirelets.get(0) + ", " + extensionType.getSimpleName()
//      + " is required to be installed.");
        System.out.println(m);
    }

    @Nullable
    public Object getWireletOrPipeline(Class<?> type) {
        return map.get(type);
    }

    // That name wirelet.. should only be used by the top-container....
    @Nullable
    public String nameWirelet() {
        return newName;
    }

    /**
     * Creates a new wirelet pack or returns existing if the array of wirelets is empty.
     * 
     * @param pcc
     *            the container configuration
     * @param wirelets
     *            the wirelets
     * @return stuff
     */
    @Nullable
    private static WireletPack create(PackedContainerRole pcc, Wirelet... wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        if (wirelets.length == 0) {
            return null;
        }

        WireletPack wc = new WireletPack();
        for (Wirelet w : wirelets) {
            requireNonNull(w, "wirelets contained a null");
            wc.create0(w);
        }

        // initialize all pipelines except for extension pipelines when existing == null
        return wc;
    }

    @Nullable
    public static WireletPack from(ComponentNodeConfiguration node, Wirelet... wirelets) {
        if (node.driver().isContainer()) {
            return create(node.container(), wirelets);
        }
        return null;
    }

    @Nullable
    public static WireletPack fromImage(PackedContainerRole pcc, Wirelet... wirelets) {
        return create(pcc, wirelets);
    }

}
