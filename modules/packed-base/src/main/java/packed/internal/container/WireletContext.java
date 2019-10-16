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

import app.packed.container.MutableWireletList;
import app.packed.container.Wirelet;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionWirelet;
import app.packed.util.Nullable;
import packed.internal.container.ContainerWirelet.ComponentNameWirelet;
import packed.internal.container.extension.ExtensionWireletPipelineModel;
import packed.internal.container.extension.PackedExtensionContext;
import packed.internal.module.ModuleAccess;
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

    final IdentityHashMap<Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>>, List<ExtensionWirelet<?>>> pipelines = new IdentityHashMap<>();

    final IdentityHashMap<Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>>, ExtensionWirelet.Pipeline<?, ?, ?>> actualpipelines = new IdentityHashMap<>();

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

    public <T extends ExtensionWirelet.Pipeline<?, ?, ?>> T getPipelin(Class<T> pipelineType) {
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
    private void apply(WireletList wirelets) {
        for (Wirelet w : wirelets.toArray()) {
            if (w instanceof ExtensionWirelet) {
                ExtensionWireletPipelineModel pm = ExtensionWireletPipelineModel.ofWireletType((Class<? extends ExtensionWirelet<?>>) w.getClass());
                pipelines.computeIfAbsent(pm.type(), k -> new ArrayList<>()).add((ExtensionWirelet<?>) w);
            } else if (w instanceof ContainerWirelet) {
                ((ContainerWirelet) w).process(this);
            } else {
                throw new IllegalArgumentException("Wirelets of type " + StringFormatter.format(w.getClass()) + " are not supported");
            }
        }
    }

    private void extensionFixed() {
        for (var e : pipelines.entrySet()) {
            Class<? extends Extension> etype = ExtensionWireletPipelineModel.of(e.getKey()).extension().extensionType;
            PackedExtensionContext c = pcc.getExtension(etype);
            if (c == null) {
                throw new IllegalArgumentException(
                        "In order to use the wirelet(s) " + e.getValue() + ", " + etype.getSimpleName() + " is required to be installed.");
            }
            initializex(c, e.getKey());
        }
    }

    @SuppressWarnings("unchecked")
    public void initializex(PackedExtensionContext pec, Class<? extends ExtensionWirelet.Pipeline<?, ?, ?>> etype) {
        List<ExtensionWirelet<?>> ewp = pipelines.get(etype);
        if (ewp != null) {
            ExtensionWireletPipelineModel m = ExtensionWireletPipelineModel.ofWireletType((Class<? extends ExtensionWirelet<?>>) ewp.iterator().next().getClass());
            ExtensionWirelet.Pipeline<?, ?, ?> pip = m.newPipeline(pec.extension(), new MutableWireletList<>(ewp));
            ModuleAccess.extension().pipelineInitialize(pip);
            actualpipelines.put(etype, pip);

        }
    }

    @SuppressWarnings("unchecked")
    public void initialize(PackedExtensionContext pec) {
        for (var p : pec.model.pipelines.keySet()) {
            List<ExtensionWirelet<?>> ewp = pipelines.get(p);
            if (ewp != null) {
                ExtensionWireletPipelineModel m = ExtensionWireletPipelineModel
                        .ofWireletType((Class<? extends ExtensionWirelet<?>>) ewp.iterator().next().getClass());
                ExtensionWirelet.Pipeline<?, ?, ?> pip = m.newPipeline(pec.extension(), new MutableWireletList<>(ewp));
                ModuleAccess.extension().pipelineInitialize(pip);
                actualpipelines.put(p, pip);
            }
        }
    }

    public static WireletContext create(PackedContainerConfiguration pcc, @Nullable WireletContext existing, Wirelet... wirelets) {
        WireletList wl = WireletList.of(wirelets);
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
