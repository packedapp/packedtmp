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

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;

import packed.internal.lifecycle.LifecycleDefinition;

/**
 *
 */
public final class SidecarTypeMeta {

    public final LifecycleDefinition ld;

    final Class<? extends Annotation> sidecarAnnotation;

    public SidecarTypeMeta(Class<? extends Annotation> sidecarAnnotation, LifecycleDefinition ld) {
        this.sidecarAnnotation = requireNonNull(sidecarAnnotation);
        this.ld = requireNonNull(ld);
    }

//    int indexOfState(String state) {
//        for (int i = 0; i < lifecycleStates.length; i++) {
//            if (lifecycleStates[i].equals(state)) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    int numberOfLifecycleStates() {
//        return lifecycleStates.length;
//    }
//
//    public String[] toArray() {
//        return lifecycleStates.clone();
//    }
}
