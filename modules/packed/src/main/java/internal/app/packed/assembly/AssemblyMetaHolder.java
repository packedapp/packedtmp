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
package internal.app.packed.assembly;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import app.packed.bean.scanning.ForeignBeanTrigger;
import app.packed.bean.scanning.ForeignBeanTrigger.CustomBindingHook;

/**
 *
 */
public class AssemblyMetaHolder {

    public final Class<? extends Annotation> annotationType;
    public final Set<String> bindings;

    public AssemblyMetaHolder(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;

        // Find Bindings Hooks
        Set<String> bs = new HashSet<>();
        CustomBindingHook[] cbh = annotationType.getAnnotationsByType(ForeignBeanTrigger.CustomBindingHook.class);
        for (CustomBindingHook h : cbh) {
            bs.add(h.className());
        }
        this.bindings = Set.copyOf(bs);
    }
}
