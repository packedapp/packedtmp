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
package internal.app.packed.util.accesshelper;

import java.util.function.Supplier;

import app.packed.bean.scanning.BeanIntrospector;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.BeanIntrospectorSetup;

/**
 * Access helper for BeanIntrospector and related classes in the bean.scanning package.
 */
public abstract class BeanScanningAccessHandler extends AccessHelper {

    private static final Supplier<BeanScanningAccessHandler> CONSTANT = StableValue.supplier(() -> init(BeanScanningAccessHandler.class, BeanIntrospector.class));

    public static BeanScanningAccessHandler instance() {
        return CONSTANT.get();
    }

    /**
     * Invokes the package-private bean() method on a BeanIntrospector.
     *
     * @param introspector the introspector
     * @return the bean setup
     */
    public abstract BeanSetup invokeBeanIntrospectorBean(BeanIntrospector<?> introspector);

    /**
     * Invokes the package-private initialize method on a BeanIntrospector.
     *
     * @param introspector the introspector
     * @param ref the introspector setup
     */
    public abstract void invokeBeanIntrospectorInitialize(BeanIntrospector<?> introspector, BeanIntrospectorSetup ref);
}
