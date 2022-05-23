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
package packed.internal.container;

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
        bean().install(Foo.class);
        bean().install(Foo.class);
        use(MyExt.class).hashCode();
    }

    public static void main(String[] args) {
        App.run(new TestMe());
        App.print(new TestMe());
    }

    public record Foo() {}                  

    @DependsOn(extensions = BeanExtension.class)
    static class MyExt extends Extension<MyExt> {
        @Override
        protected void onNew() {
            bean().install(Foo.class);
            record X() {};
            bean().install(X.class);
        }
    }
}
