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
package packed.internal.sidecar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import app.packed.base.Nullable;
import app.packed.container.InternalExtensionException;
import app.packed.sidecar.ActivateSidecar;
import app.packed.sidecar.FieldSidecar;
import app.packed.sidecar.MethodSidecar;
import app.packed.sidecar.Sidecar;
import app.packed.sidecar.SidecarActivationType;

/**
 *
 */
final class ActivateSidecarModel {

    static final Map<SidecarActivationType, Class<? extends Sidecar>> MAPPER = Map.of(SidecarActivationType.ANNOTATED_METHOD, MethodSidecar.class,
            SidecarActivationType.ANNOTATED_FIELD, FieldSidecar.class);

    final Map<SidecarActivationType, Class<? extends Sidecar>> sidecars;

    private ActivateSidecarModel(Map<SidecarActivationType, Class<? extends Sidecar>> sidecars) {
        this.sidecars = Map.copyOf(sidecars);
    }

    @Nullable
    public Class<? extends Sidecar> get(SidecarActivationType type) {
        return sidecars.get(type);
    }

    /** A cache of any extensions a particular annotation activates. */
    public static final ClassValue<ActivateSidecarModel> CACHE = new ClassValue<>() {

        // What a load of balony code...
        @Override
        protected ActivateSidecarModel computeValue(Class<?> type) {
            ActivateSidecar as = type.getAnnotation(ActivateSidecar.class);
            if (as == null) {
                return null;
            }

            Map<SidecarActivationType, Class<? extends Sidecar>> actualResult = new HashMap<>();

            SidecarActivationType[] activations = as.activation();
            if (activations.length == 0) {
                throw new InternalExtensionException("The ann");
            }
            Class<? extends Sidecar>[] sidecars = as.sidecar();
            HashSet<Class<? extends Sidecar>> sidecarsSet = new HashSet<>(List.of(sidecars));

//            HashMap<SidecarActivationType, Class<? extends Sidecar>> map = new HashMap<>();
            HashSet<SidecarActivationType> s = new HashSet<>(List.of(activations));
            for (SidecarActivationType t : s) {
//                Class<? extends Sidecar> result;
                Class<? extends Sidecar> expected = MAPPER.get(t);
                for (Iterator<Class<? extends Sidecar>> iterator = sidecarsSet.iterator(); iterator.hasNext();) {
                    Class<? extends Sidecar> sidecarActivationType = iterator.next();
                    if (expected.isAssignableFrom(sidecarActivationType)) {
                        actualResult.put(t, sidecarActivationType);
                    }
                }
            }
            return new ActivateSidecarModel(actualResult);
        }
    };

}
