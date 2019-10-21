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
package packed.internal.reflect;

import app.packed.container.InternalExtensionException;
import packed.internal.util.StringFormatter;

/**
 *
 */
public class ClassFinder {

    // Could have name = type.getSipleName?
    public static Class<?> findDeclaredClass(Class<?> declaringClass, String name, Class<?> type) {
        Class<?> groupType = null;

        for (Class<?> c : declaringClass.getDeclaredClasses()) {
            if (c.getSimpleName().equals(name)) {
                if (!type.isAssignableFrom(c)) {
                    throw new InternalExtensionException(c.getCanonicalName() + " must extend " + StringFormatter.format(type));
                }
                groupType = c;
            }
        }
        if (groupType == null) {
            throw new IllegalArgumentException("Could not find declared class named " + name);
        }

        return groupType;
    }
}
