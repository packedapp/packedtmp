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
package usage.container;

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;
import app.packed.build.hook.BuildHook;
import app.packed.container.Wirelet;
import app.packed.container.Wirelets;
import app.packed.lifetime.Main;

/**
 *
 */
public class MainUsage extends BaseAssembly {

    static final Wirelet W = BuildHook.applyWirelet(c -> c.observe());

    static final App.Image AI = App.imageOf(new MainUsage(), W, Wirelets.buildApplicationLazily());

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(MyBean.class);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // throw new Error();
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        long l = System.nanoTime();
        App.Image i = App.imageOf(new MainUsage(), Wirelets.buildApplicationLazily());

        App.Image i2 = App.imageOf(new MainUsage(), W, Wirelets.buildApplicationLazily());

        System.out.println(System.nanoTime() - l);
        i.run();

    }

    public static class MyBean {

        @Main
        public void hello() {
            System.out.println("HelloWorld ");
        }
    }
}
