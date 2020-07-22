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
package app.packed.container;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.service.ServiceExtension;
import packed.internal.util.LookupUtil;

/**
 *
 */
public class BundleHelper {
    public static String POST_CONFIGURE = "CONSUMED";

    // I'm not sure I need to read this...
    /** A varhandle that can extract a ServiceExtensionNode from {@link ServiceExtension}. */
    static final VarHandle CONFIGURATION = LookupUtil.initPrivateVH(MethodHandles.lookup(), ContainerBundle.class, "configuration", Object.class);
}
