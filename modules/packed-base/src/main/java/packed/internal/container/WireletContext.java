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

import java.lang.reflect.Type;
import java.util.IdentityHashMap;

import app.packed.container.Wirelet;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionWirelet;
import app.packed.container.extension.ExtensionPipeline;
import packed.internal.support.AppPackedExtensionSupport;
import packed.internal.util.StringFormatter;
import packed.internal.util.TypeVariableExtractorUtil;

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

    private static final ClassValue<Class<? extends Extension>> WIRELET_TO_EXTENSION = new ClassValue<>() {

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        protected Class<? extends Extension> computeValue(Class<?> type) {
            Type t = TypeVariableExtractorUtil.findTypeParameterUnsafe((Class) type, ExtensionWirelet.class, 0);
            if (!(t instanceof Class)) {
                throw new IllegalStateException();
            }
            Class<? extends Extension> extensionType = (Class<? extends Extension>) t;
            if (extensionType.getModule() != type.getModule()) {
                throw new IllegalArgumentException("The wirelet and the extension must be defined in the same module, however extension "
                        + StringFormatter.format(extensionType) + " was defined in " + extensionType.getModule() + ", and this wirelet type "
                        + StringFormatter.format(getClass()) + " was defined in module " + getClass().getModule());
            }
            return extensionType;
        }
    };

    // We might at some point, allow the setting of a default name...
    // In which we need to different between not-set and set to null

    ContainerWirelet.ComponentNameWirelet newName;

    final IdentityHashMap<Class<? extends Extension>, ExtensionPipeline<?>> pipelines = new IdentityHashMap<>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void apply(PackedContainerConfiguration pcc, Wirelet... wirelets) {
        for (Wirelet w : wirelets) {
            if (w instanceof ExtensionWirelet) {
                ExtensionWirelet ew = (ExtensionWirelet) w;
                Class<? extends Extension> cl = WIRELET_TO_EXTENSION.get(w.getClass());
                ExtensionPipeline p = pipelines.computeIfAbsent(cl, k -> {
                    Extension e = pcc.getExtension(cl);
                    if (e == null) {
                        throw new IllegalStateException();// Extension was never instaleld
                    }
                    return AppPackedExtensionSupport.invoke().wireletNewPipeline(null, null);

                });
                AppPackedExtensionSupport.invoke().wireletProcess(p, ew);
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
