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
package tests.injector;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.module.Configuration;

import app.packed.inject.Injector;

/**
 *
 */
@SuppressWarnings("unused")

// Det her bliver lavet i en integrations test..
public class InjectorLookupAccess {

    public static void main(String[] args) {
        Injector.of(c -> {
            // c.
        });

        Lookup l = MethodHandles.lookup();

        Module m;

        Configuration configuration = Configuration.empty();
        // ModuleFinder.Controller con = ModuleLayer.defineModulesWithOneLoader(configuration, List.of(),
        // InjectorLookupAccess.class.getClassLoader());

        // System.out.println(con.layer().modules());
    }
}
