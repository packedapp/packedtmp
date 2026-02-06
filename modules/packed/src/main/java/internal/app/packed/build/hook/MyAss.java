/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import app.packed.application.App;
import app.packed.application.Main;
import app.packed.assembly.AssemblyConfiguration;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.BeanConfiguration;
import internal.app.packed.build.hook.MyAss.MyBe;
import internal.app.packed.build.hooks.AssemblyBuildHook;
import internal.app.packed.build.hooks.BeanHook;
import internal.app.packed.build.hooks.UseBuildHooks;

/**
 *
 */
@UseBuildHooks(hooks = MyBe.class)
public class MyAss extends BaseAssembly {

    @Override
    protected void build() {
        install(ExampleBean.class);
    }

    public static void main(String[] args) {
        App.run(new MyAss());
    }

    public static class ExampleBean {

        @Main
        public void runMeAndExit() {
            IO.println("HelloWorld");
        }
    }

    static class MyB extends AssemblyBuildHook {

        @Override
        public void beforeBuild(AssemblyConfiguration configuration) {
            new Exception().printStackTrace();
            IO.println("ASDSD");
        }

    }

    static class MyBe extends BeanHook {

        @Override
        public void onNew(BeanConfiguration<?> configuration) {
            IO.println("Bean Added of type " + configuration.beanClass());
        }
    }
}
