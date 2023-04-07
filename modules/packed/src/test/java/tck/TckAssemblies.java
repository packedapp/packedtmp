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
package tck;

import java.util.function.Consumer;

import app.packed.container.Assembly;
import app.packed.container.BaseAssembly;
import app.packed.extension.BaseExtension;
import tck.TckBeans.HelloMainBean;

/**
 *
 */
public class TckAssemblies {

    public static class EmptyAssembly extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
        }
    }

    public static class HelloWorldAssembly extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            installInstance(new HelloMainBean());
        }
    }

    public static class SimpleAssembly extends BaseAssembly {
        final Consumer<BaseExtension> c;

        public SimpleAssembly(Consumer<BaseExtension> c) {
            this.c = c;
        }

        /** {@inheritDoc} */
        @Override
        protected void build() {
            c.accept(base());
        }
    }

    public static Assembly create(Consumer<BaseExtension> c) {
        return new SimpleAssembly(c);
    }

    public static class InstanceAssembly extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            installInstance(new HelloMainBean());
        }
    }

}
