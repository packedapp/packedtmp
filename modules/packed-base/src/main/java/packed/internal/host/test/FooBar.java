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
package packed.internal.host.test;

import app.packed.artifact.App;
import app.packed.component.ComponentStream;
import app.packed.container.BaseBundle;

/**
 *
 */
public class FooBar extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        MyHostConf hc = provideHost(MyHostConf.class).setName("HA");
        hc.deploy(new TestBundle(), App.DRIVER);
        hc.deploy(new TestBundle(), App.DRIVER);
        hc.deploy(new TestBundle(), App.DRIVER);
        // hc.deploy(new TestBundle(), ArtifactDriver.APP);
    }

    public static void main(String[] args) {
        // App.start(new FooBar()).stream().sorted().forEach(e -> System.out.println(e.path() + " " + e.type()));
        System.out.println();
        ComponentStream.of(new FooBar()).sorted().forEach(e -> System.out.println(e.path() + " " + e.depth()));
    }

    static class TestBundl3e extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            setName("Stuff?");
            installInstance("HejHej").setName("123");
        }
    }

    static class TestBundl2e extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            setName("Ss1?");
            installInstance("HejHej").setName("123");

            MyHostConf hc = provideHost(MyHostConf.class);
            hc.deploy(new TestBundl3e(), App.DRIVER);
            hc.deploy(new TestBundl3e(), App.DRIVER);

        }
    }

    static class TestBundle extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            setName("Sss2?");
            installInstance("HejHej").setName("123");

            MyHostConf hc = provideHost(MyHostConf.class);
            hc.deploy(new TestBundl2e(), App.DRIVER);

            // Problemet er
        }
    }
}
