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

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

import app.packed.base.AttributeProvide;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.invoke.OpenClass;

/**
 *
 */
public class ProvidableAttributeModel {

    public final Map<PackedAttribute<?>, MethodHandle> attributeTypes;

    ProvidableAttributeModel(Map<PackedAttribute<?>, MethodHandle> attributeTypes) {
        this.attributeTypes = attributeTypes;
    }

    public static ProvidableAttributeModel analyse(OpenClass oc) {
        // OpenClass oc = new OpenClass(MethodHandles.lookup(), c, true);
        HashMap<PackedAttribute<?>, MethodHandle> types = new HashMap<>();
        oc.findMethods(m -> {
            AttributeProvide ap = m.getAnnotation(AttributeProvide.class);
            if (ap != null) {
                PackedAttribute<?> pa = ClassAttributes.find(ap);
                MethodHandle mh = oc.unreflect(m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                types.put(pa, mh);
            }
        });
        if (types.isEmpty()) {
            return null;
        }
        return new ProvidableAttributeModel(types);
    }
}
