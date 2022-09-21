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
package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.bean.BeanConfiguration;
import app.packed.container.ExtensionBeanConfiguration;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.RealmSetup;
import internal.app.packed.util.LookupUtil;

/**
 * A special version of bean setup for extension beans.
 */
public final class ExtensionBeanSetup extends BeanSetup {

    /** A handle that can access BeanConfiguration#beanHandle. */
    private static final VarHandle VH_HANDLE = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), BeanConfiguration.class, "handle",
            PackedBeanHandle.class);

    /** The extension the bean is a part of. */
    public final ExtensionSetup extension;

    /**
     * @param builder
     * @param owner
     */
    public ExtensionBeanSetup(ExtensionSetup extension, PackedBeanHandleInstaller<?> builder, RealmSetup owner) {
        super(builder, owner);
        this.extension = requireNonNull(extension);
    }

    public static ExtensionBeanSetup crack(ExtensionBeanConfiguration<?> configuration) {
        PackedBeanHandle<?> bh = (PackedBeanHandle<?>) VH_HANDLE.get((BeanConfiguration) configuration);
        return (ExtensionBeanSetup) bh.bean();
    }
}
