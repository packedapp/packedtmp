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
package xxx;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Paths;
import java.util.Set;

/**
 *
 */
public class TryLoadJar {

    // * ModuleLayer parent = ModuleLayer.boot();
    // *
    // * Configuration cf = parent.configuration().resolve(finder, ModuleFinder.of(), Set.of("myapp"));
    // *
    // * ClassLoader scl = ClassLoader.getSystemClassLoader();
    // *
    // * ModuleLayer layer = parent.defineModulesWithOneLoader(cf, scl);
    // *
    // * Class<?> c = layer.findLoader("myapp").loadClass("app.Main");
    //
    public static void main(String[] args) throws Exception {
        ModuleFinder mf = ModuleFinder.of(Paths.get("/Users/kasperni/jmodtest.jar"));
        System.out.println(mf.findAll().size());
        for (ModuleReference mr : mf.findAll()) {
            System.out.println(mr.descriptor().name());
        }
        ModuleLayer parent = ModuleLayer.boot();

        Configuration cf = parent.configuration().resolve(mf, ModuleFinder.of(), Set.of("foo.bar"));
        ClassLoader scl = ClassLoader.getSystemClassLoader();

        ModuleLayer layer = parent.defineModulesWithOneLoader(cf, scl);
        Class<?> c = layer.findLoader("foo.bar").loadClass("somepackage.TestBundle");

        System.out.println(TryLoadJar.class);

        System.out.println(c.getClassLoader());

        // Configuration c = Configuration.resolve(mf, List.of(ModuleLayer.boot().configuration()), ModuleFinder.ofSystem(),
        // List.of("foo.bar"));

        // Optional<ResolvedModule> rm = c.findModule("foo.bar");
        //
        // c.defineModulesWithOneLoader(cf, scl)
        // Controller con = ModuleLayer.defineModules(c, List.of(TryLoadJar.class.getModule().getLayer()), s ->
        // TryLoadJar.class.getClassLoader());
        //
        // // TryLoadJar.class.getClassLoader().loadClass("somepackage.TestBundle");
        //
        // for (Module m : con.layer().modules()) {
        // System.out.println(m.getClassLoader());
        // m.getClassLoader().loadClass("somepackage.TestBundle");
        // }
        //
        //
        // * ClassLoader scl = ClassLoader.getSystemClassLoader();
        // *
        // * ModuleLayer layer = parent.defineModulesWithOneLoader(cf, scl);
        // *
        // * Class<?> c = layer.findLoader("myapp").loadClass("app.Main");
    }
}
