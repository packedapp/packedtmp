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
import java.util.Map.Entry;

import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.wirelet.InternalWirelet.ComponentNameWirelet;
import packed.internal.container.PackedContainerRole;
import packed.internal.container.PackedExtensionConfiguration;

/**
 * A container of wirelets and wirelet pipelines.
 */
public final class WireletPack {

    // Could put them in wirelets. And then have an int countdown instead... every time an extension is removed.
    private final IdentityHashMap<Class<? extends Extension>, Object> extensions = new IdentityHashMap<>();

    /** A map of wirelets and wirelet pipelines. */
    private final IdentityHashMap<Class<?>, Object> map = new IdentityHashMap<>();

    // We might at some point, allow the setting of a default name...
    // In which we need to different between not-set and set to null
    ComponentNameWirelet newName; // kan komme i map... og saa saetter vi et flag istedet for...

    /** An optional parent. */
    @Nullable
    final WireletPack parent;

    /**
     * Creates a new pack.
     * 
     * @param parent
     *            any parent
     */
    private WireletPack(@Nullable WireletPack parent) {
        this.parent = parent;
    }

    /**
     * This method checks that no wirelets have been specified that requires an extensions that have not been used.
     */
    public void checkAllExtensionsAvailable(PackedContainerRole pcc) {
        assert (parent == null);
        if (!extensions.isEmpty()) {
            extensionFailed(pcc);
        }
    }

    /**
     * @param w
     */
    private void create0(Wirelet w) {
        WireletModel m = WireletModel.of(w.getClass());
        WireletPipelineModel pipeline = m.pipeline();
        if (pipeline != null) {
            WireletPipelineContext context = (WireletPipelineContext) map.computeIfAbsent(pipeline.type(), k -> {
                WireletPipelineContext pc = parent == null ? null : (WireletPipelineContext) parent.getWireletOrPipeline(pipeline.type());
                WireletPipelineContext wpc = new WireletPipelineContext(pipeline, pc);
                Class<? extends Extension> extensionType = pipeline.extension();
                if (extensionType != null) {
                    extensions.put(extensionType, wpc);// We need to add it as a list if we have more than one wirelet context
                }
                return wpc;
            });

            context.wirelets.add(w);

        } else if (w instanceof InternalWirelet) {
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
        for (Entry<Class<? extends Extension>, Object> c : extensions.entrySet()) {
            Class<? extends Extension> k = c.getKey();
            if (pcc.getExtensionContext(k) == null) {

            }
        }
//      throw new IllegalArgumentException("In order to use the wirelet(s) " + wpc.wirelets.get(0) + ", " + extensionType.getSimpleName()
//      + " is required to be installed.");
        System.out.println(m);
    }

    public void extensionInitialized(PackedExtensionConfiguration pec) {
        // See if we have installed a pipeline
        WireletPipelineContext wpc = (WireletPipelineContext) extensions.get(pec.extensionType());
        if (wpc != null) {
            wpc.instantiate(pec.instance());
        }
    }

    @Nullable
    public Object getWireletOrPipeline(Class<?> type) {
        WireletPack wc = this;
        do {
            Object o = wc.map.get(type);
            if (o != null) {
                return o;
            }
            wc = wc.parent;
        } while (wc != null);
        return null;
    }

    public String name(ComponentNodeConfiguration pcc) {
        WireletPack wc = this;
        while (wc != null) {
            if (wc.newName != null) {
                return newName.name;
            }
            wc = wc.parent;
        }
        return pcc.getName();
    }

    // That name wirelet.. should only be used by the top-container....
    @Nullable
    public ComponentNameWirelet nameWirelet() {
        WireletPack wc = this;
        while (wc != null) {
            if (wc.newName != null) {
                return newName;
            }
            wc = wc.parent;
        }
        return null;
    }

    /**
     * Creates a new wirelet pack or returns existing if the array of wirelets is empty.
     * 
     * @param pcc
     *            the container configuration
     * @param existing
     *            an existing pack of wirelets
     * @param wirelets
     *            the wirelets
     * @return stuff
     */
    @Nullable
    private static WireletPack create(PackedContainerRole pcc, @Nullable WireletPack existing, Wirelet... wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        if (wirelets.length == 0) {
            return existing;
        }

        WireletPack wc = new WireletPack(existing);
        for (Wirelet w : wirelets) {
            requireNonNull(w, "wirelets contained a null");
            wc.create0(w);
        }

        // initialize all pipelines except for extension pipelines when existing == null
        for (Object o : wc.map.values()) {
            if (o instanceof WireletPipelineContext) {
                WireletPipelineContext wpc = (WireletPipelineContext) o;
                Class<? extends Extension> extension = wpc.extension();
                if (extension == null) {
                    wpc.instantiate(null);
                } else if (existing != null) {
                    PackedExtensionConfiguration pec = pcc.getExtensionContext(extension);
                    if (pec == null) {
                        wc.extensionFailed(pcc);
                    } else {
                        wpc.instantiate(pec.instance());
                    }
                }
            }
        }
        return wc;
    }

    @Nullable
    public static WireletPack fromImage(PackedContainerRole pcc, @Nullable WireletPack existing, Wirelet... wirelets) {
        return create(pcc, existing, wirelets);
    }

    @Nullable
    public static WireletPack from(ComponentNodeConfiguration pcc, Wirelet... wirelets) {
        if (pcc.isContainer()) {
            return fromLink(pcc.container, wirelets);
        }
        return null;
    }

    @Nullable
    public static WireletPack fromLink(PackedContainerRole pcc, Wirelet... wirelets) {
        return create(pcc, null, wirelets);
    }

    @Nullable
    public static WireletPack fromRoot(PackedContainerRole pcc, Wirelet... wirelets) {
        return create(pcc, null, wirelets);
    }
}