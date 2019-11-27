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
package packed.internal.container.extension.newgraph;

import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.lang.Nullable;

/**
 *
 */
// Skal vi tage extension context istedet for????
// Ikke saa let at teste. Men vi kan hive informationer ud om containeren..
// Alternativt, en special version af WireletListNew... som embedder ting...

abstract class XtensionPipeline<E extends Extension, P extends XtensionPipeline<E, P, W>, W extends Wirelet> {

    public void buildArtifact() {
        // extension.buildBundle(null);
    }

    public void buildBundle() {}

    boolean isDetailedStackCapturingEnable() {
        return false;
    }

    boolean isStackCapturingEnable() {
        return false;
    }

    // Kunne godt tage en boolean der sagde noget om hvordan den koerer...

    // ExtensionPipeline er per instance. De bliver vel naermest smeder sammen.
    // Det betyder at man godt kan have en slags context... Idet vi kan have et index
    // til den nuvaerende wirelet der bliver processeret....
    // F.eks.
    boolean logAll() {
        return false;
    }

    /**
     * This is invoked by the runtime when all wirelets have been successfully processed.
     * 
     * 
     * No more wirelets are being processed.... Split
     */
    protected void onFinishedProcessing() {

    }

    public void onParentConfigured(@Nullable Object extension) {

    }
}

// removeAll(CannotBeUsedOnImageWirelet.class,
// if (!previous.isEmpty()) <- on image