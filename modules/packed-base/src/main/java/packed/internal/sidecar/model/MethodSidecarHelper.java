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

import java.lang.annotation.Annotation;

import app.packed.base.Nullable;
import app.packed.sidecar.ActivateSidecar;
import packed.internal.sidecar.MethodSidecarModel;
import packed.internal.sidecar.SidecarModel;

/**
 *
 */
public final class MethodSidecarHelper {

    @Nullable
    public static MethodSidecarModel tryGet(Class<? extends Annotation> c) {
        return METHOD_SIDECARS.get(c);
    }

    /** A cache of any extensions a particular annotation activates. */
    public static final ClassValue<MethodSidecarModel> METHOD_SIDECARS = new ClassValue<>() {

        @Override
        protected MethodSidecarModel computeValue(Class<?> type) {
            ActivateSidecar ae = type.getAnnotation(ActivateSidecar.class);
            return ae == null ? null : SidecarModel.ofMethod(ae.value());
        }
    };
}
