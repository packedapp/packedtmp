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
package packed.internal.sidecar.model;

import java.util.EnumSet;
import java.util.List;

import app.packed.container.InternalExtensionException;
import app.packed.sidecar.ActivateSidecar;
import app.packed.sidecar.SidecarActivationType;

/**
 *
 */
public final class ActivateSidecarModel {

    final EnumSet<SidecarActivationType> activationTypes;

    private ActivateSidecarModel(EnumSet<SidecarActivationType> activationTypes) {
        this.activationTypes = activationTypes;
    }

    /** A cache of any extensions a particular annotation activates. */
    public static final ClassValue<ActivateSidecarModel> CACHE = new ClassValue<>() {

        @Override
        protected ActivateSidecarModel computeValue(Class<?> type) {
            ActivateSidecar as = type.getAnnotation(ActivateSidecar.class);
            if (as == null) {
                return null;
            }

            SidecarActivationType[] activations = as.activation();
            EnumSet<SidecarActivationType> activationTypes = EnumSet.copyOf(List.of(activations));

            if (activations.length == 0) {
                throw new InternalExtensionException("The ann");
            }

            Class<?>[] sidecar = as.sidecar();
            System.out.println(sidecar);
            return new ActivateSidecarModel(activationTypes);
        }
    };
}
