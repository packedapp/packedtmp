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
package internal.app.packed.util.handlers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.lifetime.BeanLifetimeMirror;
import app.packed.lifetime.CompositeLifetimeMirror;
import internal.app.packed.lifecycle.lifetime.BeanLifetimeSetup;
import internal.app.packed.lifecycle.lifetime.ContainerLifetimeSetup;

/**
 *
 */
public class BeanLifetimeHandlers extends Handlers {
    /** A MethodHandle for invoking {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_NEW_BEAN_LIFETIME_MIRROR = constructor(MethodHandles.lookup(), BeanLifetimeMirror.class,
            BeanLifetimeSetup.class);

    public static BeanLifetimeMirror newBeanLifetimeMirror(BeanLifetimeSetup setup) {
        try {
            return (BeanLifetimeMirror) MH_NEW_BEAN_LIFETIME_MIRROR.invokeExact(setup);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }

    /** A MethodHandle for invoking {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_NEW_REGIONAL_LIFETIME_MIRROR = constructor(MethodHandles.lookup(), CompositeLifetimeMirror.class,
            ContainerLifetimeSetup.class);

    public static CompositeLifetimeMirror newRegionalLifetimeMirror(ContainerLifetimeSetup setup) {
        try {
            return (CompositeLifetimeMirror) MH_NEW_REGIONAL_LIFETIME_MIRROR.invokeExact(setup);
        } catch (Throwable t) {
            throw throwIt(t);
        }
    }
}
