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

import app.packed.container.Wirelet;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionWirelet;
import packed.internal.container.extension.ExtensionWireletPipelineModel;
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
public class WireletContext2 {

    // We might at some point, allow the setting of a default name...
    // In which we need to different between not-set and set to null

    final PackedContainerConfiguration pcc;

    WireletContext2(PackedContainerConfiguration pcc) {
        this.pcc = requireNonNull(pcc);
    }

    ContainerWirelet.ComponentNameWirelet newName;

    final IdentityHashMap<Class<? extends Extension>, List<ExtensionWirelet<?>>> pipelines = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public void apply(Wirelet... wirelets) {
        for (Wirelet w : wirelets) {
            if (w instanceof ExtensionWirelet) {
                ExtensionWireletPipelineModel pm = ExtensionWireletPipelineModel.ofWirelet((Class<? extends ExtensionWirelet<?>>) w.getClass());
                pipelines.computeIfAbsent(pm.extension.extensionType, k -> new ArrayList<>()).add((ExtensionWirelet<?>) w);
            } else if (w instanceof ContainerWirelet) {
                // ((ContainerWirelet) w).process(this);
            } else {
                throw new IllegalArgumentException("Wirelets of type " + StringFormatter.format(w.getClass()) + " are not supported");
            }
        }
    }

    public void spawn(Wirelet... wirelets) {

    }

    public static WireletContext2 create(PackedContainerConfiguration pcc, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}
