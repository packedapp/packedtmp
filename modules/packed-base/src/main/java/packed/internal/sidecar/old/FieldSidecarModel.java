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
package packed.internal.sidecar.old;

import packed.internal.lifecycle.old.LifecycleDefinition;

/** A model of {@link OldFieldSidecar}. */
public final class FieldSidecarModel extends OldSidecarModel {

    /** Meta data about the extension sidecar. */
    private static final SidecarTypeMeta STM = new SidecarTypeMeta(OldFieldSidecar.class, LifecycleDefinition.of(OldFieldSidecar.INSTANTIATION));

    /** The instance id into {@link ClassInstance#data}. */
    final int instanceId;

    // descriptorId.. enten det eller ogsaa binder vi den til methodhandle...

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder used to construct the model
     */
    protected FieldSidecarModel(Builder builder) {
        super(builder);
        this.instanceId = builder.instanceId;
    }

    public static class Builder extends OldSidecarModel.Builder {

        int instanceId;

        /**
         * @param sidecarType
         */
        public Builder(Class<?> sidecarType) {
            super(sidecarType, STM);
        }
    }
}
