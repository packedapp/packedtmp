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
package internal.app.packed.container;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.stream.Stream;

import app.packed.container.Operative;
import internal.app.packed.bean.BeanSetup;

/**
 * Stores beans for a single container.
 */
public final class ContainerBeanStore implements Iterable<BeanSetup> {

    private static final int CLASS_COUNT_MASK = (1 << 31) - 1;

    /** A map of all non-void bean classes. Used for controlling non-multi-install beans. */

    // When we have built the container we need to run through all of them
    // and fail if any are >0 (count bigger than zero and multi install bit not set)
    public final HashMap<BeanClassKey, BeanSetup> beanClasses = new HashMap<>();

    /** All beans installed in the container. */
    public final LinkedHashMap<String, BeanSetup> beans = new LinkedHashMap<>();

    /** {@inheritDoc} */
    @Override
    public Iterator<BeanSetup> iterator() {
        return beans.values().iterator();
    }

    public Stream<BeanSetup> stream() {
        return beans.values().stream();
    }

    public static boolean isMultiInstall(BeanSetup bean) {
        return false;
    }

    public static int multiInstallCounter(BeanSetup bean) {
        return bean.multiInstall & CLASS_COUNT_MASK;
    }

    public /* primitive */ record BeanClassKey(Operative realm, Class<?> beanClass) {}
}
