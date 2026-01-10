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
package internal.app.packed.util.accesshelper;

import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanMirror;
import internal.app.packed.bean.BeanSetup;

/**
 * Access helper for BeanHandle and related classes.
 */
public abstract class BeanAccessHandler extends AccessHelper {

    private static final Supplier<BeanAccessHandler> CONSTANT = StableValue.supplier(() -> init(BeanAccessHandler.class, BeanHandle.class));

    public static BeanAccessHandler instance() {
        return CONSTANT.get();
    }

    /**
     * Gets the BeanHandle from a BeanConfiguration.
     *
     * @param configuration the configuration
     * @return the bean handle
     */
    public abstract BeanHandle<?> getBeanConfigurationHandle(BeanConfiguration<?> configuration);

    /**
     * Gets the BeanSetup from a BeanHandle.
     *
     * @param handle the handle
     * @return the bean setup
     */
    public abstract BeanSetup getBeanHandleBean(BeanHandle<?> handle);

    /**
     * Gets the BeanHandle from a BeanMirror.
     *
     * @param mirror the mirror
     * @return the bean handle
     */
    public abstract BeanHandle<?> getBeanMirrorHandle(BeanMirror mirror);

    /**
     * Invokes the protected onStateChange method on a BeanHandle.
     *
     * @param handle the handle
     * @param isClose whether this is a close event
     */
    public abstract void invokeBeanHandleDoClose(BeanHandle<?> handle, boolean isClose);
}
