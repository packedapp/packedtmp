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

import java.lang.invoke.MethodHandles;

import app.packed.base.Nullable;
import packed.internal.sidecar.model.Model;

/**
 *
 */

// Vi er ligeglade med klasse annoteringer?? Nej da...
// Men kun members...
public class PackletClass extends Model {

    /**
     * Creates a new packlet class.
     * 
     * @param type
     *            the type
     */
    protected PackletClass(Class<?> type) {
        super(type);
    }

    static PackletClass of(PackletSupportModel pms, Class<?> clazz, @Nullable MethodHandles.Lookup lookup) {
        // TOOD Find specializing annotations
        // ArrayList<Annotation> specializing = new ArrayList<>();

        // If more than one we need to make sure they don't provide the same service...

        throw new UnsupportedOperationException();
    }
}
// Examples

//// Altsaa
//public class PackletMethod {
//
//}
// Vi ignorer det her lige nu
// MinimumVisibility (What about abstract classes????)
