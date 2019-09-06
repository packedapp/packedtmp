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

public abstract class ExtensionPipeline<T extends ExtensionPipeline<T>> {

    // ExtensionPipeline er per instance. De bliver vel naermest smeder sammen.
    // Det betyder at man godt kan have en slags context... Idet vi kan have et index
    // til den nuvaerende wirelet der bliver processeret....
    // F.eks.
    boolean logAll() {
        return false;
    }

    boolean isStackCapturingEnable() {
        return false;
    }

    boolean isDetailedStackCapturingEnable() {
        return false;
    }

    // Kunne godt tage en boolean der sagde noget om hvordan den koerer...

    /**
     * Splits this pipeline into a new pipeline. This method is used by the runtime when a user uses wirelets to instantiate
     * an artifact image. Or tries to create a new artifact image from an existing image.
     * 
     * @return a new pipeline
     */
    // Two strategies. Either clone all the contents.
    // Or recursively call back into parent pipeline
    protected abstract T split();

    /**
     * This is invoked by the runtime when all wirelets have been successfully processed.
     * 
     * 
     * No more wirelets are being processed.... Split
     */
    protected void onFinishedProcessing() {

    }

    //
    public void buildArtifact() {
        // extension.buildBundle(null);
    }

    public void buildBundle() {}

    public void onParentConfigured(@Nullable Object extension) {

    }

}
