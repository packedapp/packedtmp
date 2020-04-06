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

    final IdentityHashMap<Class<? extends WireletPipeline<?, ?, ?>>, WireletPipeline<?, ?, ?>> actualpipelines = new IdentityHashMap<>();

    final IdentityHashMap<Class<? extends Extension>, Object> extensions = new IdentityHashMap<>();

    ComponentNameWirelet newName;

    /** Any parent this context might have */
    @Nullable
    final WireletContext parent;

    /** Pipelines for the various extensions. */
    final IdentityHashMap<Class<? extends WireletPipeline<?, ?, ?>>, List<PipelineWirelet<?>>> pipelineElements = new IdentityHashMap<>();

    private final IdentityHashMap<Class<?>, Object> wirelets = new IdentityHashMap<>();

    /**
     * Creates a new context
     * 
     * @param parent
     *            any parent
     */
    private WireletContext(@Nullable WireletContext parent) {
        this.parent = parent;
    }

    private void addWirelet(PackedContainerConfiguration pcc, Wirelet w) {
        requireNonNull(w, "wirelets contain a null");
        if (w instanceof PipelineWirelet) {
            @SuppressWarnings("unchecked")
            WireletPipelineModel model = PipelineWireletModel.of((Class<? extends PipelineWirelet<?>>) w.getClass());

            pipelineElements.computeIfAbsent(model.type, k -> new ArrayList<>()).add((PipelineWirelet<?>) w);
            ((WireletPipelineContext) wirelets.computeIfAbsent(model.type, k -> {
                WireletPipelineContext pc = parent == null ? null : (WireletPipelineContext) parent.getIt(model.type);
                WireletPipelineContext wpc = new WireletPipelineContext(model, pc);
                if (model.extensionType != null) {
                    extensions.put(model.extensionType, wpc);// We need to add it as a list if we have more than one wirelet context
                }
                return wpc;
            })).wirelets.add((PipelineWirelet<?>) w);
        } else if (w instanceof ContainerWirelet) {
            ((ContainerWirelet) w).process(pcc, this);
        } else if (w instanceof FixedWireletList) {
            for (Wirelet ww : ((FixedWireletList) w).wirelets) {
                addWirelet(pcc, ww);
            }
        } else {
            wirelets.put(w.getClass(), w);
        }
    }

    @Nullable
    public Object getIt(Class<?> type) {
        WireletContext wc = this;
        do {
            Object o = wc.wirelets.get(type);
            if (o != null) {
                return o;
            }
            wc = wc.parent;
        } while (wc != null);
        return null;
    }

    public void extensionInitialized(PackedExtensionContext pec) {
        Object o = extensions.get(pec.model().extensionType());
        if (o != null) {
            WireletPipelineContext c = (WireletPipelineContext) o;
            c.instance = c.model.newPipeline(pec.extension());
            ModuleAccess.extension().pipelineInitialize(c);
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

    public void extensionsConfigured() {
        assert (parent == null);
        if (!extensions.isEmpty()) {
            throw new Error();
        }
    }

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

        for (Object o : wc.wirelets.values()) {
            if (o instanceof WireletPipelineContext) {
                WireletPipelineContext c = (WireletPipelineContext) o;
                Class<? extends Extension> et = c.extension();
                if (et == null || existing != null) {
                    Extension extension = null;
                    if (et != null) {
                        PackedExtensionContext pec = pcc.getExtension(et);
                        if (pec == null) {
                            throw new IllegalArgumentException(
                                    "In order to use the wirelet(s) " + c.wirelets.get(0) + ", " + et.getSimpleName() + " is required to be installed.");
                        }
                        extension = pec.extension();
                    }
                    c.instance = c.model.newPipeline(extension);
                    ModuleAccess.extension().pipelineInitialize(c);
                }
            }
        }

        return wc;
    }
}
// Typer wirelets
// Standalone -> Kan injectes... (ved ikke om vi skal holde styr paa om de bliver brugt...
// PipelineWirelets -> Pipeline... holder styr paa om den bliver brugt
// Extension Pipeline -> Check Extension er installeret...

// Internal Wirelets -> Kan kalde dem med en PCC, WC

//private void extensionFixed(PackedContainerConfiguration pcc) {
//  for (var e : pipelineElements.entrySet()) {
//      Class<? extends Extension> etype = WireletPipelineModel.of(e.getKey()).extensionType;
//      PackedExtensionContext c = pcc.getExtension(etype);
//      if (c == null) {
//          // Call ExtensionWirelet#extensionNotAvailable
//          throw new IllegalArgumentException(
//                  "In order to use the wirelet(s) " + e.getValue() + ", " + etype.getSimpleName() + " is required to be installed.");
//      }
//      initializex(c, e.getKey());
//  }
//}
////
//@SuppressWarnings("unchecked")
//private void extensionInitialized(PackedExtensionContext pec) {
//  for (var p : pec.model().pipelines.keySet()) {
//      List<PipelineWirelet<?>> ewp = pipelineElements.get(p);
//      if (ewp != null) {
//          WireletPipelineModel m = PipelineWireletModel.of((Class<? extends PipelineWirelet<?>>) ewp.iterator().next().getClass());
//
//          WireletPipeline<?, ?, ?> pip = m.newPipeline(pec.extension());
//          ModuleAccess.extension().pipelineInitialize(Optional.empty(), ewp, pip);
//          actualpipelines.put(p, pip);
//      }
//  }
//}
//
//@SuppressWarnings("unchecked")
//private void initializex(PackedExtensionContext pec, Class<? extends WireletPipeline<?, ?, ?>> etype) {
//List<PipelineWirelet<?>> ewp = pipelineElements.get(etype);
//if (ewp != null) {
//    WireletPipelineModel m = PipelineWireletModel.of((Class<? extends PipelineWirelet<?>>) ewp.iterator().next().getClass());
//    WireletPipeline<?, ?, ?> pip = m.newPipeline(pec.extension());
//    ModuleAccess.extension().pipelineInitialize(Optional.empty(), ewp, pip);
//    actualpipelines.put(etype, pip);
//}
//}