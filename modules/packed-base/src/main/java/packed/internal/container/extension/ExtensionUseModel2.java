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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import app.packed.container.Extension;
import app.packed.container.ExtensionProps;
import app.packed.container.InternalExtensionException;
import app.packed.container.UseExtension;
import packed.internal.util.StringFormatter;

/**
 *
 */
public class ExtensionUseModel2 {

    @SuppressWarnings("unchecked")
    private static void loadOptional(Collection<Class<? extends Extension>> addTo, Class<?> declaringClass, String[] optionals) {
        if (optionals.length > 0) {
            ClassLoader cl = declaringClass.getClassLoader(); // PrividligeAction???
            for (String s : optionals) {
                Class<?> c = null;
                try {
                    c = Class.forName(s, false, cl);
                } catch (ClassNotFoundException ignore) {}
                if (c != null) {
                    if (Extension.class == c) {
                        throw new InternalExtensionException("@" + UseExtension.class.getSimpleName() + " " + StringFormatter.format(declaringClass)
                                + " cannot specify Extension as an optional dependency, for " + StringFormatter.format(c));
                    } else if (!Extension.class.isAssignableFrom(c)) {
                        throw new InternalExtensionException("@" + UseExtension.class.getSimpleName() + " " + StringFormatter.format(declaringClass)
                                + " specified an invalid extension " + StringFormatter.format(c));
                    }

                    addTo.add((Class<? extends Extension>) c);
                }
            }
        }
    }

    // Well this should consistently fail bar security exceptions...
    // So no need to cache the exception I think
    private static final RetainThrowableClassValue<List<Class<? extends Extension>>> EXTENSION_DIRECT_DEPENDENCY_CACHE = new RetainThrowableClassValue<>() {

        @Override
        protected List<Class<? extends Extension>> computeValue(Class<?> type) {
            LinkedHashSet<Class<? extends Extension>> list = new LinkedHashSet<>();

            UseExtension ue = type.getAnnotation(UseExtension.class);
            if (ue != null) {
                for (Class<? extends Extension> c : ue.value()) {
                    list.add(c);
                }
                loadOptional(list, type, ue.optional());
            }
            if (Extension.class.isAssignableFrom(type)) {
                ExtensionProps ep = type.getAnnotation(ExtensionProps.class);
                if (ep != null) {
                    for (Class<? extends Extension> c : ep.dependencies()) {
                        list.add(c);
                    }
                    loadOptional(list, type, ep.dependenciesOptional());
                }
            }
            return List.copyOf(new ArrayList<>(list));
        }
    };

    public static List<Class<? extends Extension>> directDependenciesOf(Class<? extends Extension> extensionType) {
        return EXTENSION_DIRECT_DEPENDENCY_CACHE.get(extensionType);
    }

    public static List<Class<? extends Extension>> totalOrder(Class<?> type) {
        LinkedHashSet<Class<? extends Extension>> result = new LinkedHashSet<>();
        ArrayDeque<Class<? extends Extension>> deq = new ArrayDeque<>();
        calc(deq, result, type);
        // TODO Auto-generated method stub
        return List.copyOf(new ArrayList<>(result));
    }

    public static List<Class<? extends Extension>> totalOrderOfExtensionReversed(Collection<Class<? extends Extension>> extensions) {
        LinkedHashSet<Class<? extends Extension>> result = new LinkedHashSet<>();
        ArrayDeque<Class<? extends Extension>> deq = new ArrayDeque<>();
        for (Class<? extends Extension> type : extensions) {
            calc(deq, result, type);
            result.add(type);
        }
        // TODO Auto-generated method stub
        ArrayList<Class<? extends Extension>> list = new ArrayList<>(result);
        Collections.reverse(list);
        return List.copyOf(list);
    }

    private static void calc(ArrayDeque<Class<? extends Extension>> stack, LinkedHashSet<Class<? extends Extension>> ordered, Class<?> cl) {
        for (Class<? extends Extension> dep : EXTENSION_DIRECT_DEPENDENCY_CACHE.get(cl)) {
            if (stack.contains(dep)) {
                throw new IllegalStateException("Dep cyc");
            }
            stack.push(dep);
            calc(stack, ordered, dep);
            ordered.add(dep);
            stack.pop();
        }
    }
}
