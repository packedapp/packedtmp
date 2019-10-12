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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import app.packed.container.Wirelet;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionWirelet;
import app.packed.container.extension.ExtensionWireletList;
import app.packed.container.extension.ExtensionWireletPipeline;
import packed.internal.container.extension.ExtensionWireletPipelineModel;
import packed.internal.container.extension.PackedExtensionContext;
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
public class WireletContext {

    // We might at some point, allow the setting of a default name...
    // In which we need to different between not-set and set to null

    ContainerWirelet.ComponentNameWirelet newName;

    final IdentityHashMap<ExtensionWireletPipelineModel, ExtensionWireletPipeline<?, ?, ?>> pipelines = new IdentityHashMap<>();

    final IdentityHashMap<Class<? extends Extension>, List<ExtensionWirelet<?>>> pipelines2 = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public void apply(PackedContainerConfiguration pcc, WireletList wirelets) {
        IdentityHashMap<ExtensionWireletPipelineModel, List<ExtensionWirelet<?>>> ews = new IdentityHashMap<>();

        for (Wirelet w : wirelets.toArray()) {
            if (w instanceof ExtensionWirelet) {
                ExtensionWireletPipelineModel pm = ExtensionWireletPipelineModel.ofWirelet((Class<? extends ExtensionWirelet<?>>) w.getClass());
                ews.computeIfAbsent(pm, k -> new ArrayList<>()).add((ExtensionWirelet<?>) w);
            } else if (w instanceof ContainerWirelet) {
                ((ContainerWirelet) w).process(this);
            } else {
                throw new IllegalArgumentException("Wirelets of type " + StringFormatter.format(w.getClass()) + " are not supported");
            }
        }

        for (var entry : ews.entrySet()) {
            var pm = entry.getKey();
            PackedExtensionContext e = pcc.getExtension(pm.extension.extensionType);
            if (e == null) {
                throw new IllegalStateException(
                        "The wirelets " + entry.getValue() + " requires the extension " + pm.extension.extensionType.getSimpleName() + " to be installed.");
            }
            var pip = pm.newPipeline(e.extension(), new ExtensionWireletList<>(entry.getValue()));
            pip.onInitialize();
            pipelines.put(pm, pip);
        }
    }

    @SuppressWarnings("unchecked")
    public void apply(WireletList wirelets) {
        for (Wirelet w : wirelets.toArray()) {
            if (w instanceof ExtensionWirelet) {
                ExtensionWireletPipelineModel pm = ExtensionWireletPipelineModel.ofWirelet((Class<? extends ExtensionWirelet<?>>) w.getClass());
                pipelines2.computeIfAbsent(pm.extension.extensionType, k -> new ArrayList<>()).add((ExtensionWirelet<?>) w);
            } else if (w instanceof ContainerWirelet) {
                ((ContainerWirelet) w).process(this);
            } else {
                throw new IllegalArgumentException("Wirelets of type " + StringFormatter.format(w.getClass()) + " are not supported");
            }
        }
    }

    public static WireletContext create(PackedContainerConfiguration pcc, WireletList wirelets) {
        if (wirelets.toArray().length > 0) {
            WireletContext wc = new WireletContext();
            wc.apply(wirelets);
            return wc;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void initialize(PackedExtensionContext pec) {
        List<ExtensionWirelet<?>> ewp = pipelines2.remove(pec.extension().getClass());
        ExtensionWireletPipelineModel m = ExtensionWireletPipelineModel.ofWirelet((Class<? extends ExtensionWirelet<?>>) ewp.iterator().next().getClass());
        ExtensionWireletPipeline<?, ?, ?> pip = m.newPipeline(pec.extension(), new ExtensionWireletList<>(ewp));
        pip.onInitialize();
    }
}
