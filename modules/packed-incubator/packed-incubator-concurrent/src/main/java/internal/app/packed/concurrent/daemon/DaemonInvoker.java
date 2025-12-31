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
package internal.app.packed.concurrent.daemon;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.concurrent.DaemonJobContext;
import internal.app.packed.extension.ExtensionContext;
import internal.app.packed.invoke.LookupUtil;

/**
 *
 */
public record DaemonInvoker(MethodHandle methodHandle, ExtensionContext extensionContext) {

   public static final MethodHandle CONSTRUCTOR = LookupUtil.findConstructor(MethodHandles.lookup(), DaemonInvoker.class, MethodHandle.class, ExtensionContext.class);

    public void invoke(DaemonJobContext djc) {
        try {
            methodHandle.invokeExact(extensionContext, djc);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
