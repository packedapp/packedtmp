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
import java.util.List;
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.PipelineWirelet;
import app.packed.container.Wirelet;
import app.packed.container.WireletPipeline;
import packed.internal.container.ContainerWirelet.ComponentNameWirelet;
import packed.internal.moduleaccess.ModuleAccess;

/**
 *
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

    ComponentNameWirelet newName;

    /** Pipelines for the various extensions. */
    final IdentityHashMap<Class<? extends WireletPipeline<?, ?, ?>>, List<PipelineWirelet<?>>> pipelines = new IdentityHashMap<>();

    final IdentityHashMap<Class<? extends WireletPipeline<?, ?, ?>>, WireletPipeline<?, ?, ?>> actualpipelines = new IdentityHashMap<>();

    @Nullable
    final WireletContext parent;

    WireletContext(@Nullable WireletContext parent) {
        this.parent = parent;
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nullable
    public Object get(Class<?> type) {
        if (Wirelet.class.isAssignableFrom(type)) {
            return wirelets.get(type);
        } else {
            return getPipelin((Class) type);
        }
    }

    public <T extends WireletPipeline<?, ?, ?>> T getPipelin(Class<T> pipelineType) {
        WireletContext wc = this;
        while (wc != null) {
            @SuppressWarnings("unchecked")
            T pip = (T) actualpipelines.get(pipelineType);
            if (pip != null) {
                return pip;
            }
            wc = wc.parent;
        }
        return null;
    }

    private final IdentityHashMap<Class<? extends Wirelet>, Wirelet> wirelets = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    private void apply(FixedWireletList wirelets) {
        for (Wirelet w : wirelets.toArray()) {
            if (w instanceof PipelineWirelet) {
                WireletPipelineModel pm = PipelineWireletModel.of((Class<? extends PipelineWirelet<?>>) w.getClass());
                pipelines.computeIfAbsent(pm.type, k -> new ArrayList<>()).add((PipelineWirelet<?>) w);
            } else if (w instanceof ContainerWirelet) {
                ((ContainerWirelet) w).process(this);
            } else {
                this.wirelets.put(w.getClass(), w);
//                throw new IllegalArgumentException("Wirelets of type " + StringFormatter.format(w.getClass()) + " are not supported");
            }
        }
    }

    private void extensionFixed(PackedContainerConfiguration pcc) {
        for (var e : pipelines.entrySet()) {
            Class<? extends Extension> etype = WireletPipelineModel.of(e.getKey()).extensionType;
            PackedExtensionContext c = pcc.getExtension(etype);
            if (c == null) {
                // Call ExtensionWirelet#extensionNotAvailable
                throw new IllegalArgumentException(
                        "In order to use the wirelet(s) " + e.getValue() + ", " + etype.getSimpleName() + " is required to be installed.");
            }
            initializex(c, e.getKey());
        }
    }

    @SuppressWarnings("unchecked")
    public void initializex(PackedExtensionContext pec, Class<? extends WireletPipeline<?, ?, ?>> etype) {
        List<PipelineWirelet<?>> ewp = pipelines.get(etype);
        if (ewp != null) {
            WireletPipelineModel m = PipelineWireletModel.of((Class<? extends PipelineWirelet<?>>) ewp.iterator().next().getClass());
            WireletPipeline<?, ?, ?> pip = m.newPipeline(pec.extension());
            ModuleAccess.extension().pipelineInitialize(Optional.empty(), ewp, pip);
            actualpipelines.put(etype, pip);

        }
    }

    @SuppressWarnings("unchecked")
    public void initialize(PackedExtensionContext pec) {
        for (var p : pec.model().pipelines.keySet()) {
            List<PipelineWirelet<?>> ewp = pipelines.get(p);
            if (ewp != null) {
                WireletPipelineModel m = PipelineWireletModel.of((Class<? extends PipelineWirelet<?>>) ewp.iterator().next().getClass());

                WireletPipeline<?, ?, ?> pip = m.newPipeline(pec.extension());
                ModuleAccess.extension().pipelineInitialize(Optional.empty(), ewp, pip);
                actualpipelines.put(p, pip);
            }
        }
    }

    @Nullable
    public static WireletContext create(PackedContainerConfiguration pcc, @Nullable WireletContext existing, Wirelet... wirelets) {

        // Taenker ogsaa vi har et enkelt map. Kan sagtens smide wirelets og pipelines i det samme....

        // Taenker vi kan droppe FixedWireletList. Og lave en context direkte istedet for

        requireNonNull(wirelets, "wirelets is null");
        if (wirelets.length == 0) {
            return existing;
        }
        WireletContext wc = new WireletContext(existing);
        FixedWireletList wl = FixedWireletList.of(wirelets);
        wc.apply(wl);
        if (existing != null) {
            wc.extensionFixed(pcc);
        }
        return wc;
    }

    public <T extends Wirelet> T getSingle(Class<T> wireletType) {
        return null;
    }
}
