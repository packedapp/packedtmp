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
package packed.internal.classscan;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.component.Component;
import app.packed.guest.Guest;
import app.packed.service.ServiceLocator;
import packed.internal.classscan.util.InstantiatorBuilder;
import packed.internal.component.PackedInitializationContext;

/**
 *
 */
public final class ShellDriverScan {

    public static MethodHandle of(MethodHandles.Lookup caller, Class<?> implementation, boolean isGuest) {

        // We currently do not support @Provide ect... Don't know if we ever will
        // Create a new MethodHandle that can create shell instances.

        InstantiatorBuilder ib = InstantiatorBuilder.of(caller, implementation, PackedInitializationContext.class);
        ib.addKey(Component.class, PackedInitializationContext.MH_COMPONENT, 0);
        ib.addKey(ServiceLocator.class, PackedInitializationContext.MH_SERVICES, 0);
        if (isGuest) {
            ib.addKey(Guest.class, PackedInitializationContext.MH_GUEST, 0);
        }
        return ib.build();
    }
}
