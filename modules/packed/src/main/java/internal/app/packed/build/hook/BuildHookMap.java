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
package internal.app.packed.build.hook;

import java.util.List;
import java.util.function.Consumer;

import app.packed.assembly.AssemblyBuildHook;
import app.packed.build.hook.BuildHook;

/**
 *
 */
public interface BuildHookMap {

    // Maybe usefull for mirrors
    <T extends BuildHook> List<T> models(Class<T> hookType);

    <T extends BuildHook> void forEach(Class<T> hookType, Consumer<? super T> action);

    <T extends BuildHook> void forEachReversed(Class<T> hookType, Consumer<? super T> action);

    @SuppressWarnings("unchecked")
   static Class<? extends BuildHook> classOf(Class<? extends BuildHook> h) {
        if (h == BuildHook.class) {
            throw new Error();
        } else if (h.getSuperclass() == BuildHook.class) {
            throw new Error();
        } else if (!BuildHook.class.isAssignableFrom(h)) {
            throw new Error();
        }
        Class<? extends BuildHook> r = h;
        while (r.getSuperclass() != BuildHook.class) {
            r = (Class<? extends BuildHook>) r.getSuperclass();
        }
        return r;
    }


    static void main(String[] args) {
        System.out.println(classOf(MyAss.class));
    }

    static class MyAss extends AssemblyBuildHook {}
}
