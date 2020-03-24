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
import app.packed.container.ExtensionWirelet;
import app.packed.container.ExtensionWireletPipeline;
import app.packed.container.Wirelet;
import packed.internal.container.ContainerWirelet.ComponentNameWirelet;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.util.StringFormatter;

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

    // Alternativt er at gemme noget per extension..... I 99% af tilfaeldene har vi en pipeline

    final IdentityHashMap<Class<? extends ExtensionWireletPipeline<?, ?, ?>>, List<ExtensionWirelet<?>>> pipelines = new IdentityHashMap<>();

    final IdentityHashMap<Class<? extends ExtensionWireletPipeline<?, ?, ?>>, ExtensionWireletPipeline<?, ?, ?>> actualpipelines = new IdentityHashMap<>();

    @Nullable
    final WireletContext parent;

    final PackedContainerConfiguration pcc;

    WireletContext(PackedContainerConfiguration pcc, @Nullable WireletContext parent) {
        this.pcc = requireNonNull(pcc);
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

    public String name() {
        WireletContext wc = this;
        while (wc != null) {
            if (wc.newName != null) {
                return newName.name;
            }
            wc = wc.parent;
        }
        return pcc.name;
    }

    public <T extends ExtensionWireletPipeline<?, ?, ?>> T getPipelin(Class<T> pipelineType) {
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

    @SuppressWarnings("unchecked")
    private void apply(FixedWireletList wirelets) {
        for (Wirelet w : wirelets.toArray()) {
            if (w instanceof ExtensionWirelet) {
                ExtensionWireletPipelineModel pm = ExtensionWireletModel.of((Class<? extends ExtensionWirelet<?>>) w.getClass());
                pipelines.computeIfAbsent(pm.type, k -> new ArrayList<>()).add((ExtensionWirelet<?>) w);
            } else if (w instanceof ContainerWirelet) {
                ((ContainerWirelet) w).process(this);
            } else {
                throw new IllegalArgumentException("Wirelets of type " + StringFormatter.format(w.getClass()) + " are not supported");
            }
        }
    }

    private void extensionFixed() {
        for (var e : pipelines.entrySet()) {
            Class<? extends Extension> etype = ExtensionWireletPipelineModel.of(e.getKey()).extensionType;
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
    public void initializex(PackedExtensionContext pec, Class<? extends ExtensionWireletPipeline<?, ?, ?>> etype) {
        List<ExtensionWirelet<?>> ewp = pipelines.get(etype);
        if (ewp != null) {
            ExtensionWireletPipelineModel m = ExtensionWireletModel.of((Class<? extends ExtensionWirelet<?>>) ewp.iterator().next().getClass());
            ExtensionWireletPipeline<?, ?, ?> pip = m.newPipeline(pec.extension());
            ModuleAccess.extension().pipelineInitialize(Optional.empty(), ewp, pip);
            actualpipelines.put(etype, pip);

        }
    }

    @SuppressWarnings("unchecked")
    public void initialize(PackedExtensionContext pec) {
        for (var p : pec.model().pipelines.keySet()) {
            List<ExtensionWirelet<?>> ewp = pipelines.get(p);
            if (ewp != null) {
                ExtensionWireletPipelineModel m = ExtensionWireletModel.of((Class<? extends ExtensionWirelet<?>>) ewp.iterator().next().getClass());

                ExtensionWireletPipeline<?, ?, ?> pip = m.newPipeline(pec.extension());
                ModuleAccess.extension().pipelineInitialize(Optional.empty(), ewp, pip);
                actualpipelines.put(p, pip);
            }
        }
    }

    public static WireletContext create(PackedContainerConfiguration pcc, @Nullable WireletContext existing, Wirelet... wirelets) {
        FixedWireletList wl = FixedWireletList.of(wirelets);
        if (wl.toArray().length == 0) {
            return existing;
        }
        WireletContext wc = new WireletContext(pcc, existing);
        wc.apply(wl);
        if (existing != null) {
            wc.extensionFixed();
        }
        return wc;
    }
}
