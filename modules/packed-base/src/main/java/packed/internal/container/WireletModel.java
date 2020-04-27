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

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.container.WireletPipeline;
import app.packed.sidecar.WireletSidecar;
import packed.internal.sidecar.Model;

/**
 *
 */
public class WireletModel extends Model {

    /** Any extension this pipeline is a member of. */
    @Nullable
    private final Class<? extends Extension> memberOfExtension;

    @Nullable
    private final WireletPipelineModel pipeline;

    public final boolean requireAssemblyTime;

    /**
     * Create a new model.
     * 
     * @param type
     *            the type of pipeline
     */
    private WireletModel(Class<? extends Wirelet> type) {
        super(type);
        this.memberOfExtension = ExtensionSidecarModel.findIfMember(type);

        WireletPipelineModel wpm = null;
        WireletSidecar ws = type.getAnnotation(WireletSidecar.class);
        if (ws != null) {
            Class<? extends WireletPipeline<?, ?>> p = ws.pipeline();
            if (p != NoWireletPipeline.class) {
                wpm = WireletPipelineModel.of(p);
                // XXX must be assignable to YY to be a part of the pipeline
            }

            this.requireAssemblyTime = ws.requireAssemblyTime();
        } else {
            this.requireAssemblyTime = false;
        }

        this.pipeline = wpm;
    }

    @Nullable
    public WireletPipelineModel pipeline() {
        return pipeline;
    }

    public static class NoWireletPipeline extends WireletPipeline<NoWireletPipeline, Wirelet> {}
}
