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
package internal.app.packed.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import app.packed.build.BuildException;
import app.packed.build.hook.ApplyBuildHook;
import app.packed.build.hook.BuildHook;
import internal.app.packed.assembly.AssemblyClassModel;
import internal.app.packed.invoke.MethodHandleInvoker.BuildHookFactory;

/**
 *
 */
public class UserLookupSupport {

    public static BuildHookFactory newBuildHookFactory(Class<?> assemblyClass, ApplyBuildHook applyHook, Class<? extends BuildHook> type) {
        MethodHandle constructor;

        if (!AssemblyClassModel.class.getModule().canRead(type.getModule())) {
            AssemblyClassModel.class.getModule().addReads(type.getModule());
        }

        Lookup privateLookup;
        try {
            privateLookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup() /* lookup */);
        } catch (IllegalAccessException e1) {
            throw new RuntimeException(e1);
        }
        // TODO fix visibility
        // Maybe common findConstructorMethod
        try {
            constructor = privateLookup.findConstructor(type, MethodType.methodType(void.class));
        } catch (NoSuchMethodException e) {
            throw new BuildException("A container hook must provide an empty constructor, hook = " + applyHook, e);
        } catch (IllegalAccessException e) {
            throw new BuildException("Can't see it sorry, hook = " + applyHook, e);
        }

        // For consistency reasons we always tries to use invokeExact() even if not strictly needed
        constructor = constructor.asType(MethodType.methodType(BuildHook.class));

        return new BuildHookFactory(constructor);
    }
}
