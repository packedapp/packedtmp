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
package packed.internal.attribute;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

import app.packed.attribute.ExposeAttribute;
import app.packed.base.Nullable;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.invoke.ClassMemberAccessor;

/**
 *
 */
public class PackedAttributeModel {

    public final Map<PackedAttribute<?>, Attt> attributeTypes;

    PackedAttributeModel(Map<PackedAttribute<?>, Attt> attributeTypes) {
        this.attributeTypes = attributeTypes;
    }

    public static PackedAttributeModel analyse(ClassMemberAccessor oc) {
        // OpenClass oc = new OpenClass(MethodHandles.lookup(), c, true);
        HashMap<PackedAttribute<?>, Attt> types = new HashMap<>();
        oc.findMethods(m -> {
            ExposeAttribute ap = m.getAnnotation(ExposeAttribute.class);
            if (ap != null) {
                PackedAttribute<?> pa = ClassAttributes.find(ap);
                requireNonNull(pa, "Unknown Attribute " + ap + " on " + oc.type());
                MethodHandle mh = oc.unreflect(m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                types.put(pa, new Attt(mh, m.isAnnotationPresent(Nullable.class)));
            }
        });
        if (types.isEmpty()) {
            return null;
        }
        return new PackedAttributeModel(types);
    }

    public static final class Attt {

        public final MethodHandle mh;
        public final boolean isNullable;

        Attt(MethodHandle mh, boolean isNullable) {
            this.mh = requireNonNull(mh);
            this.isNullable = isNullable;
        }
    }
}
