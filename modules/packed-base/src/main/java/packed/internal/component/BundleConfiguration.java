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
package packed.internal.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.component.Bundle;
import app.packed.component.ComponentDriver;
import packed.internal.util.LookupUtil;

/**
 *
 */
public final class BundleConfiguration {

    /** A VarHandle that can access Bundle#driver. */
    private static final VarHandle VH_BUNDLE_DRIVER = LookupUtil.vhPrivateOther(MethodHandles.lookup(), Bundle.class, "driver", ComponentDriver.class);

    public static final BundleConfiguration CONSUMED_SUCCESFULLY = new BundleConfiguration();

    public void configurationAccessed() {

    }

    public static ComponentDriver<?> driver(Bundle<?> bundle) {
        return (ComponentDriver<?>) VH_BUNDLE_DRIVER.get(bundle);
    }

    public static PackedComponentDriver<?> pdriver(Bundle<?> bundle) {
        return (PackedComponentDriver<?>) driver(bundle);
    }
}
