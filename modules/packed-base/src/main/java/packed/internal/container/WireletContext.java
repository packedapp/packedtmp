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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map.Entry;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.PipelineWirelet;
import app.packed.container.Wirelet;
import packed.internal.container.ContainerWirelet.ComponentNameWirelet;

/**
 * A container of one or more wirelets.
 */
// Det er kun late binding wirelets we kan bruge...
// Ikke f.eks. ConfigSite

// Har vi altid en af dem baar vi ...
// Eller koerer vi noget if

// Why this design.
//// Alternativ. Keep a list of wirelets that was eva

// Should we copy info into new context.. Or check recursively
public final class WireletContext {

    // We might at some point, allow the setting of a default name...
    // In which we need to different between not-set and set to null

    // Could put them in wirelets. And then have an int countdown instead... every time an extension is removed.
    final IdentityHashMap<Class<? extends Extension>, Object> extensions = new IdentityHashMap<>();

    ComponentNameWirelet newName; // kan komme i map... og saa saetter vi et flag istedet for...

    /** Any parent this context might have */
    @Nullable
    final WireletContext parent;

    private final IdentityHashMap<Class<?>, Object> map = new IdentityHashMap<>();

    /**
     * Creates a new context
     * 
     * @param parent
     *            an existing parent
     */
    private WireletContext(@Nullable WireletContext parent) {
        this.parent = parent;
    }

    /**
     * @param pcc
     * @param w
     */
    private void addWirelet(PackedContainerConfiguration pcc, Wirelet w) {
        requireNonNull(w, "wirelets contain a null");
        if (w instanceof PipelineWirelet) {
            @SuppressWarnings("unchecked")
            WireletPipelineModel model = WireletPipelineModel.ofWirelet((Class<? extends PipelineWirelet<?>>) w.getClass());

            ((WireletPipelineContext) map.computeIfAbsent(model.type, k -> {
                WireletPipelineContext pc = parent == null ? null : (WireletPipelineContext) parent.getIt(model.type);
                WireletPipelineContext wpc = new WireletPipelineContext(model, pc);
                if (model.extensionType() != null) {
                    extensions.put(model.extensionType(), wpc);// We need to add it as a list if we have more than one wirelet context
                }
                return wpc;
            })).wirelets.add((PipelineWirelet<?>) w);
        } else if (w instanceof ContainerWirelet) {
            // Hmm skulle vi vente til alle wirelets er succesfuld processeret???
            // Altsaa hvad hvis den fejler.... Altsaa taenker ikke den maa lavere aendringer i containeren.. kun i wirelet context
            ((ContainerWirelet) w).process(this);
        } else if (w instanceof WireletList) {
            for (Wirelet ww : ((WireletList) w).wirelets) {
                addWirelet(pcc, ww);
            }
        } else {
            // A standalone wirelet, just override any existing
            map.put(w.getClass(), w);
        }
    }

    @Nullable
    public Object getIt(Class<?> type) {
        WireletContext wc = this;
        do {
            Object o = wc.map.get(type);
            if (o != null) {
                return o;
            }
            wc = wc.parent;
        } while (wc != null);
        return null;
    }

    public void extensionInitialized(PackedExtensionContext pec) {
        // See if we have installed a pipeline
        WireletPipelineContext wpc = (WireletPipelineContext) extensions.get(pec.model().extensionType());
        if (wpc != null) {
            wpc.instantiate(pec.extension());
        }
    }

    public String name(PackedContainerConfiguration pcc) {
        WireletContext wc = this;
        while (wc != null) {
            if (wc.newName != null) {
                return newName.name;
            }
            wc = wc.parent;
        }
        return pcc.name;
    }

    public ComponentNameWirelet nameWirelet() {
        WireletContext wc = this;
        while (wc != null) {
            if (wc.newName != null) {
                return newName;
            }
            wc = wc.parent;
        }
        return null;
    }

    /**
     * This method checks that no wirelets have been specified requiring extensions that have not been used.
     */
    public void checkAllExtensionsAvailable(PackedContainerConfiguration pcc) {
        assert (parent == null);
        if (!extensions.isEmpty()) {
            extensionFailed(pcc);
        }
    }

    /**
     * Creates a new wirelet context or returns existing if the array of wirelets is empty.
     * 
     * @param pcc
     *            the container configuration
     * @param existing
     * @param wirelets
     *            the wirelets
     * @return stuff
     */
    @Nullable
    public static WireletContext create(PackedContainerConfiguration pcc, @Nullable WireletContext existing, Wirelet... wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        if (wirelets.length == 0) {
            return existing;
        }

        WireletContext wc = new WireletContext(existing);
        for (Wirelet w : wirelets) {
            wc.addWirelet(pcc, w);
        }

        // initialize all pipelines except for extension pipelines when existing == null
        for (Object o : wc.map.values()) {
            if (o instanceof WireletPipelineContext) {
                WireletPipelineContext wpc = (WireletPipelineContext) o;
                Class<? extends Extension> extensionType = wpc.extensionType();
                if (extensionType == null) {
                    wpc.instantiate(null);
                } else if (existing != null) {
                    PackedExtensionContext pec = pcc.getExtension(extensionType);
                    if (pec == null) {
                        wc.extensionFailed(pcc);
                    }
                    wpc.instantiate(pec.extension());
                }
            }
        }
        return wc;
    }

    private void extensionFailed(PackedContainerConfiguration pcc) {
        IdentityHashMap<Class<? extends Extension>, ArrayList<Wirelet>> m = new IdentityHashMap<>();
        for (Entry<Class<? extends Extension>, Object> c : extensions.entrySet()) {
            Class<? extends Extension> k = c.getKey();
            if (pcc.getExtension(k) == null) {

            }
        }
        System.out.println(m);
//        throw new IllegalArgumentException("In order to use the wirelet(s) " + wpc.wirelets.get(0) + ", " + extensionType.getSimpleName()
//        + " is required to be installed.");

        // ArrayList<Class<? extends ExtensionT>>
    }

}
