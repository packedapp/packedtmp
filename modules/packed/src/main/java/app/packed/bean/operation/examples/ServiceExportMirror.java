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
package app.packed.bean.operation.examples;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.bean.mirror.BeanOperationMirror;
import app.packed.bean.operation.BeanOperationMirrorSelection;
import app.packed.container.Assembly;

/**
 *
 */
public abstract class ServiceExportMirror extends BeanOperationMirror {

    /** {@return the key that the service is exported with.} */
    public abstract Key<?> key();

    // Hvad goer vi omvendt??? Returnere en liste??
    // Kun allower en? IDK
    public abstract Optional<ServiceProvideMirror> service(); // Kan ikke fange alle dog

    // find usage of the exported service

    public static BeanOperationMirrorSelection<ServiceExportMirror> selectAll(Assembly assembly) {
        throw new UnsupportedOperationException();
    }
}
