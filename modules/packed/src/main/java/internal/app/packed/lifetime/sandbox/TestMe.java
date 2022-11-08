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
package internal.app.packed.lifetime.sandbox;

import app.packed.application.App;
import app.packed.bean.BeanExtension;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.container.Extension.DependsOn;

/**
 *
 */
public class TestMe extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        bean().multiInstall(Foo.class);
        bean().multiInstall(Foo.class);
    }

    public static void main(String[] args) {
        App.run(new TestMe());
        App.print(new TestMe());
    }

    public record Foo() {
        
    }

    @DependsOn(extensions = BeanExtension.class)
    public static class MyExt extends Extension<MyExt> {
        MyExt() {}

        @Override
        protected void onNew() {
            // Hah virker ikke lige nu, fordi extensions ikke har separat boen container
            bean().install(Foo.class);
            record X() {}
            bean().install(X.class);
        }
    }
}
