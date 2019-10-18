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
package packed.internal.container.extension;

import java.util.ArrayList;
import java.util.List;

import app.packed.container.extension.Extension;
import app.packed.container.extension.InternalExtensionException;
import app.packed.container.extension.UseExtension;
import packed.internal.util.StringFormatter;

/**
 *
 */
public final class ExtensionUtil {

    private static final ClassValue<List<Class<? extends Extension>>> USE_DEPENDENCIES = new ClassValue<>() {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected List<Class<? extends Extension>> computeValue(Class<?> type) {
            List<Class<?>> list = USE_DEPENDENCIES_UNCHECKED.get(type);
            for (Class<?> c : list) {
                if (!Extension.class.isAssignableFrom(c)) {
                    throw new InternalExtensionException("@" + UseExtension.class.getSimpleName() + " " + StringFormatter.format(type)
                            + " specified an invalid extension " + StringFormatter.format(c));
                }
            }
            return (List) list;
        }
    };

    // Maaske en ClassValue der gemmer exceptionen...
    // Og saa laver en ny af samme type, med den samme fejlmeddelse
    /**
     * First we load any classes exactly once. In case they reference an 'illegal' extension we check that in
     * {@link #USE_DEPENDENCIES} instead.
     */
    private static final ClassValue<List<Class<?>>> USE_DEPENDENCIES_UNCHECKED = new ClassValue<>() {

        @Override
        protected List<Class<?>> computeValue(Class<?> type) {
            UseExtension ue = type.getAnnotation(UseExtension.class);
            if (ue == null) {
                return List.of();
            }
            ArrayList<Class<?>> list = new ArrayList<>();

            for (Class<?> c : ue.value()) {
                list.add(c);
            }

            String[] strings = ue.optional();
            if (strings.length > 0) {
                ClassLoader cl = type.getClassLoader(); // PrividligeAction???
                for (String s : strings) {
                    try {
                        Class<?> c = Class.forName(s, false, cl);
                        list.add(c);
                    } catch (ClassNotFoundException ignore) {}
                }
            }
            return List.copyOf(list);
        }
    };

    /**
     * Returns a list of extensions specified via {@link UseExtension}. This method will try to resolve any
     * {@link UseExtension#optional()} specified extensions. This will be only be done on the first call to this method and
     * returned cached.
     * 
     * @param c
     *            the class to
     * @return a list of extensions
     * @throws InternalExtensionException
     *             if some classes specified via {@link UseExtension#optional()} does not reference an extension type.
     */
    public static final List<Class<? extends Extension>> fromUseExtension(Class<?> c) {
        return USE_DEPENDENCIES.get(c);
    }
}
