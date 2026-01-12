/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.bean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.stream.Stream;

import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanSourceKind;
import app.packed.component.ComponentRealm;
import internal.app.packed.ValueBased;
import internal.app.packed.container.NameCheck;
import internal.app.packed.extension.ExtensionSetup;

/**
 * Stores all beans for a single container.
 * <p>
 * At some point when the logic has stabilised we might move it back into ContainerSetup.
 */
public final class ContainerBeanStore implements Iterable<BeanSetup> {

    private static final int CLASS_COUNT_MASK = (1 << 31) - 1;

    /** A map of all non-void bean classes. Used for controlling non-multi-install beans. */

    // When we have built the container we need to run through all of them
    // and fail if any are >0 (count bigger than zero and multi install bit not set)
    final HashMap<BeanClassKey, BeanSetup> beanClasses = new HashMap<>();

    /** All beans installed in the container. */
    final LinkedHashMap<String, BeanSetup> beans = new LinkedHashMap<>();

    void installAndSetBeanName(BeanSetup bean, String namePrefix) {
        String prefix = namePrefix;
        if (prefix == null) {
            prefix = "Functional";
            BeanModel beanModel = bean.bean.beanSourceKind == BeanSourceKind.SOURCELESS ? null : new BeanModel(bean.bean.beanClass);

            if (beanModel != null) {
                prefix = beanModel.simpleName();
            }
        }
        // TODO virker ikke med functional beans og naming
        String n = prefix;

        n = prefixExtension(bean, n);

        if (bean.bean.beanClass != void.class) {
            BeanClassKey key = new BeanClassKey(bean.owner.owner(), bean.bean.beanClass);

            BeanSetup existingBean = beanClasses.get(key);
            int counter = 0;
            if (existingBean != null) {
                if (!ContainerBeanStore.isMultiInstall(existingBean)) {
                    // throw new BeanInstallationException("A bean of type [" + bean.beanClass + "] has already been added to " +
                    // container.path());

                    throw new BeanInstallationException("oops");
                }
                counter = ContainerBeanStore.multiInstallCounter(existingBean);
            }

            if (counter > 0) {
                n = prefix + counter;
            }
            while (beans.putIfAbsent(n, bean) != null) {
                n = prefix + ++counter;
            }
            bean.multiInstall = counter;

            // Register the bean class for installIfAbsent lookups
            beanClasses.put(key, bean);
        }
        bean.name = n;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<BeanSetup> iterator() {
        return beans.values().iterator();
    }

    /**
     * Prefix extension beans with the name of the extension.
     *
     * @param bean
     * @param newName
     * @return
     */
    private String prefixExtension(BeanSetup bean, String newName) {
        if (bean.owner instanceof ExtensionSetup es) {
            newName = es.tree.name + "#" + newName;
        }
        return newName;
    }

    public Stream<BeanSetup> stream() {
        return beans.values().stream();
    }

    public void updateBeanName(BeanSetup bean, String newName) {
        // We start by validating the new name of the component
        NameCheck.checkComponentName(newName);

        String existingName = bean.name();

        // Make sure to prefix extension beans with the name of the extension
        newName = prefixExtension(bean, newName);

        // Check that this component is still active and the name can be set
        // Do we actually care? Of course we can only set as long as the realm is open
        // But other than that why not
        // Issue should be the container which should probably work identical
        // And I do think we should have it as the first thing

        if (beans.putIfAbsent(newName, bean) != null) {
            if (newName.equals(bean.name())) { // tried to set the current name which is okay i guess?
                return;
            }
            throw new IllegalArgumentException("A bean or container with the specified name '" + newName + "' already exists");
        }
        beans.remove(existingName);
        bean.name = newName;
    }

    public static boolean isMultiInstall(BeanSetup bean) {
        return false;
    }

    public static int multiInstallCounter(BeanSetup bean) {
        return bean.multiInstall & CLASS_COUNT_MASK;
    }

    @ValueBased
    public record BeanClassKey(ComponentRealm realm, Class<?> beanClass) {}

    // Kunne maaske have en int paa BeanSetup
    // Og saa i Bean Classes har vi den seneste indsatte
    // som vi tilsidt checker alle af.
    // if (count & COUNT_MASK > )
    // 0 = No MultiInstance, Alone
    // 1 = Multi instance + Alone
    // 3 = Multi Instance, <<1 = count
    static class MultiInstallCounter {
        int counter;
    }
}

//
//boolean multiInstall = false;
//if (multiInstall) {
//  MultiInstallCounter i = (MultiInstallCounter) container.beans.beanClasses.compute(key, (c, o) -> {
//      if (o == null) {
//          return new MultiInstallCounter();
//      } else if (o instanceof BeanSetup) {
//          throw new BeanInstallationException("Oops");
//      } else {
//          ((MultiInstallCounter) o).counter += 1;
//          return o;
//      }
//  });
//  int next = i.counter;
//  if (next > 0) {
//      n = prefix + next;
//  }
//  while (container.beans.beans.putIfAbsent(n, bean) != null) {
//      n = prefix + ++next;
//      i.counter = next;
//  }
//} else {
//  container.beans.beanClasses.compute(key, (c, o) -> {
//      if (o == null) {
//          return bean;
//      } else if (o instanceof BeanSetup) {
//          // singular???
//          throw new BeanInstallationException("A bean of type [" + bean.beanClass + "] has already been added to " + container.path());
//      } else {
//          // We already have some multiple beans installed
//          throw new BeanInstallationException("Oops");
//      }
//  });
//  // Not multi install, so should be able to add it first time
//  int size = 0;
//  while (container.beans.beans.putIfAbsent(n, bean) != null) {
//      n = prefix + ++size;
//  }
//}