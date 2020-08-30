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
package packed.internal.base.attribute;

import java.util.HashSet;
import java.util.Set;

import app.packed.base.AttributeProvide;
import packed.internal.invoke.OpenClass;

/**
 *
 */
public class ProvidableAttributeModel {

    final Set<PackedAttribute<?>> attributeTypes;

    ProvidableAttributeModel(Set<PackedAttribute<?>> attributeTypes) {
        this.attributeTypes = attributeTypes;
    }

    public static ProvidableAttributeModel analyse(OpenClass oc) {
        // OpenClass oc = new OpenClass(MethodHandles.lookup(), c, true);
        HashSet<PackedAttribute<?>> types = new HashSet<>();
        oc.findMethods(m -> {
            AttributeProvide ap = m.getAnnotation(AttributeProvide.class);
            if (ap != null) {
                PackedAttribute<?> pa = ClassAttributes.find(ap);
                types.add(pa);
            }
        });
        if (types.isEmpty()) {
            return null;
        }
        return new ProvidableAttributeModel(types);
    }
}
