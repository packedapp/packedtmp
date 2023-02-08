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
package internal.app.packed.bean.hooks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;

import app.packed.extension.Extension;
import app.packed.extension.InternalExtensionException;
import internal.app.packed.util.types.ClassUtil;

/**
 *
 */
class Utils {

    static void checkMemberAnnotation(Class<?> clazz, ElementType elementType) {

        Target target = clazz.getAnnotation(Target.class);
        if (target == null) {
            throw new InternalExtensionException("@ "  + clazz.getSimpleName());
        }

        List<ElementType> of = List.of(target.value());
        if (!of.contains(ElementType.FIELD)) {
            throw new InternalExtensionException("");
        }
        if (of.contains(ElementType.TYPE_USE) || of.contains(ElementType.PARAMETER)) {
            throw new InternalExtensionException("");
        }

    }
    static void checkExtensionClass(Class<?> annotationType, Class<? extends Extension<?>> extensionType) {
        ClassUtil.checkProperSubclass(Extension.class, extensionType, s -> new InternalExtensionException(s));
        if (extensionType.getModule() != annotationType.getModule()) {
            throw new InternalExtensionException(
                    "The annotation " + annotationType + " and the extension " + extensionType + " must be declared in the same module");
        }
    }
}
