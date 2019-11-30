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
import app.packed.artifact.ArtifactImage;
import app.packed.container.BaseBundle;
import app.packed.container.Wirelet;

/**
 *
 */
public class FooBar extends BaseBundle {

    static final ArtifactImage IMG = ArtifactImage.build(new TestBundle());

    static final ArtifactImage IMG2 = ArtifactImage.build(new TestBundl2e());

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        MyHostConf hc = addHost(MyHostConf.class).as(AppHost.class);
        export(AppHost.class); // export(hc); <- Virker kun med ServiceInstanceConfiguration
        hc.deploy(IMG, App.DRIVER);
        hc.deploy(IMG2, App.DRIVER);
        hc.deploy(IMG2, App.DRIVER, Wirelet.name("XXXX"));
    }

    public static void main(String[] args) {
        App.start(new FooBar()).stream().forEach(e -> System.out.println(e.path()));
    }

    static class TestBundl2e extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            setName("SSS1");
            installInstance("HejHej").setName("123");
        }
    }

    static class TestBundle extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            setName("SSS2");
            installInstance("HejHej").setName("123");
        }
    }
}
