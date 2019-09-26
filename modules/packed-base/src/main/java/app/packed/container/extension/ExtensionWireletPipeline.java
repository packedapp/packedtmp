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
package app.packed.container.extension;

import app.packed.util.Nullable;

/**
 * Extension wirelet pipelines
 * 
 * If a given extension allows specific wirelets to late configure it after. You need to use a pipeline.
 */
// Kan sagtens lave alle metoder public....

// Er en pipeline mere end en pipeline!!!
// Er den ogsaa noden...???

// Drop <T> + split()...

// Vi skal mirror alle metoder paa node....

// Kan only define one pipeline per extension...
// Fordi, vi kalder build paa den...
// ExtensionOrExtensionNode for T

// Vi bliver noedt til at have en tilknyttning til en extension....
// Fordi vi skal vide hvilken extension vi skal kigge i for at finde
// en WireletPipelineFactory....
public abstract class ExtensionWireletPipeline<T extends ExtensionWireletPipeline<T, N>, N extends Extension> {

    //
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

    public abstract T spawn();
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

    /**
     * Splits this pipeline into a new pipeline. This method is used by the runtime when a user uses wirelets to instantiate
     * an artifact image. Or tries to create a new artifact image from an existing image.
     */
    // Pipelines maa vaere registreret i et Map.... Som man saa kan traekke paaa..
    // F.eks. i forbindelse med BuildContract...

    // Two strategies. Either clone all the contents.
    // Or recursively call back into parent pipeline
    // protected abstract T split();

    // Ideen er lidt at tage en props.addPipeline(MyPipeline.class, e-> new MyPip(e.mode), Order.SECOND);
    // Vi skal ihvertfald draenes af alle annoteringer. Kun paa selve wireletten...
    // Alternativet, hvis vi kun har 2-3 vaerdier bare at have 3 metoder i props
    // addPipelineLast, addPipelineFirst, ...
    public enum Order {
        FIRST, SECOND, LAST;
    }
}
