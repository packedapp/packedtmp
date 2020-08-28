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
package packed.internal.sidecar.packlet;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import app.packed.base.Nullable;
import packed.internal.sidecar.old.Model;

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
        Annotation[] annotations = clazz.getAnnotations();
        // TOOD Find specializing annotations
        // ArrayList<Annotation> specializing = new ArrayList<>();

        ArrayList<AnnoClass> other = new ArrayList<>();
        for (Annotation a : annotations) {
            AnnoClass ac = pms.forClassAnnotation(clazz, a);
            if (ac != null) {
                other.add(ac);
            }
        }

        // If more than one we need to make sure they don't provide the same service...

        throw new UnsupportedOperationException();
    }

    static class Builder {

        final ArrayList<PackletMethod.Builder> methods = new ArrayList<>();

        MethodHandles.Lookup lookup; // A lookup object

        ArrayList<AnnoClass> other = new ArrayList<>();

        PackletSupportModel psm;

        // 1. Look for class annotations, that changes the packlet system model

        // 2. Look for packlet class annotations... Can change the "model"

        // 3. Constructor needs to take 1+2 into consideration

        // 3. Look for field and member annotations taking 1+2 into consideration

        //

        void process() {

        }

        void processMethod(Method method) {
            // We only support matching methods via annotations. Not names or anything else
            for (Annotation a : method.getAnnotations()) {

                System.out.println(a);
            }
        }

        void processField(Field field) {

        }
    }
}
// Examples

//// Altsaa
//public class PackletMethod {
//
//}
// Vi ignorer det her lige nu
// MinimumVisibility (What about abstract classes????)
