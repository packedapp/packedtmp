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
package packed.internal.container.extension.load;

import app.packed.container.BaseBundle;
import app.packed.container.Extension;
import app.packed.container.UseExtension;
import app.packed.service.ServiceExtension;
import packed.internal.container.extension.ExtensionUseModel2;

/**
 *
 */
public class TestIt {

    static Foox foox = new Foox();

    static class Foox extends RetainThrowableClassValue<String> {

        /** {@inheritDoc} */
        @Override
        protected String computeValue(Class<?> type) {
            if (type == Integer.class) {
                throw new UnsupportedOperationException("asdasdsad " + System.nanoTime());
            }
            return "OK " + System.nanoTime();

        }
    }

    public static void main(String[] args) {

        // try {
        // System.out.println(ExtensionUseModel2.directDependenciesOf(MyE.class));
        // } catch (Throwable t) {
        // t.printStackTrace();
        // }
        // try {
        // System.out.println(ExtensionUseModel2.directDependenciesOf(MyE.class));
        // } catch (Throwable t) {
        // t.printStackTrace();
        // }

        System.out.println(ExtensionUseModel2.totalOrder(MyE.class));

    }

    @UseExtension(value = { ServiceExtension.class, MyF.class })
    public class MyE extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            // TODO Auto-generated method stub

        }

    }

    @UseExtension(value = ServiceExtension.class)
    public class MyF extends Extension {

    }
}
