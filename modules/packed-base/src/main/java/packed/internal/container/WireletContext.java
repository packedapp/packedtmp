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

import java.util.IdentityHashMap;

import app.packed.container.Wirelet;
import app.packed.container.extension.ExtensionWirelet;
import app.packed.container.extension.ExtensionWireletPipeline;
import app.packed.util.Nullable;
import packed.internal.access.SharedSecrets;
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

    final IdentityHashMap<ExtensionWireletPipelineModel, ExtensionWireletPipeline<?>> pipelines = new IdentityHashMap<>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void apply(PackedContainerConfiguration pcc, Wirelet... wirelets) {
        for (Wirelet w : wirelets) {
            if (w instanceof ExtensionWirelet) {
                ExtensionWireletPipelineModel pm = ExtensionWireletPipelineModel.ofWirelet((Class<? extends ExtensionWirelet<?>>) w.getClass());
                ExtensionWireletPipeline p = pipelines.computeIfAbsent(pm, k -> {
                    @Nullable
                    PackedExtensionContext e = pcc.getContext(pm.node.extension.extensionType);
                    if (e == null) {
                        throw new IllegalStateException(
                                "The wirelet " + w + " requires the extension " + pm.node.extension.extensionType.getSimpleName() + " to be installed.");
                    }
                    return pm.newPipeline(e.extensionNode());
                });
                SharedSecrets.extension().wireletProcess(p, (ExtensionWirelet) w);
            } else if (w instanceof ContainerWirelet) {
                ((ContainerWirelet) w).process(this);
            } else {
                throw new IllegalArgumentException("Wirelets of type " + StringFormatter.format(w.getClass()) + " are not supported");
            }
        }
    }

    public static WireletContext create(PackedContainerConfiguration pcc, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}
