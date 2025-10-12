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

import app.packed.lifetime.BeanLifetimeMirror;
import app.packed.lifetime.CompositeLifetimeMirror;
import internal.app.packed.lifecycle.lifetime.BeanLifetimeSetup;
import internal.app.packed.lifecycle.lifetime.ContainerLifetimeSetup;

/**
 * Access helper for bean lifetime mirrors and related classes.
 */
public abstract class BeanLifetimeAccessHandler extends AccessHelper {

    private static final Supplier<BeanLifetimeAccessHandler> CONSTANT = StableValue.supplier(() -> init(BeanLifetimeAccessHandler.class, BeanLifetimeMirror.class));

    public static BeanLifetimeAccessHandler instance() {
        return CONSTANT.get();
    }

    /**
     * Creates a new BeanLifetimeMirror.
     *
     * @param setup the bean lifetime setup
     * @return the mirror
     */
    public abstract BeanLifetimeMirror newBeanLifetimeMirror(BeanLifetimeSetup setup);

    /**
     * Creates a new CompositeLifetimeMirror (regional lifetime mirror).
     *
     * @param setup the container lifetime setup
     * @return the mirror
     */
    public abstract CompositeLifetimeMirror newRegionalLifetimeMirror(ContainerLifetimeSetup setup);
}
