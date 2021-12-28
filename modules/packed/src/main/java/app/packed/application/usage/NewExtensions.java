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
package app.packed.application.usage;

import app.packed.application.App;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;

/**
 *
 */
public class NewExtensions extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        installInstance("asd");
        link(new FooAss());
        use(MyExt.class);
    }

    public static void main(String[] args) {
        App.mirrorOf(new NewExtensions());
    }

    public static class FooAss extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            use(MyExt.class);
        }
    }
    
    
    public static class MyExt extends Extension {
                
        @Override
        protected void onClose() {
            super.onClose();
            System.out.println("OnClose " + configuration().depth());
        }

        @Override
        protected void onNew() {
            super.onNew();
            System.out.println("OnNew " + configuration().depth());
        }

        @Override
        protected void onUserClose() {
            super.onUserClose();
            System.out.println("OnUserClose " + configuration().depth());
        }
    }
}
