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
package tck.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.extension.Extension.DependsOn;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.ExtensionPoint.ExtensionPointHandle;
import tck.AppAppTest;

/**
 *
 */
public class OnNewInstallExtensionTest extends AppAppTest {

    @Test
    public void testCanCallUseFromOnExtensionNew() {
        configuration().use(Ex1.class);
        assertThat(configuration().extensionTypes()).containsExactlyInAnyOrder(BaseExtension.class, Ex3.class, Ex2.class, Ex1.class);
    }

    @DependsOn(extensions = Ex2.class)
    static final class Ex1 extends Extension<Ex1> {

        /**
         * @param handle
         */
        Ex1(ExtensionHandle<Ex1> handle) {
            super(handle);
        }

        @Override
        protected void onNew() {
            use(Ex2.Sub.class);
        }
    }

    @DependsOn(extensions = Ex3.class)
    static final class Ex2 extends Extension<Ex2> {

        /**
         * @param handle
         */
         Ex2(ExtensionHandle<Ex2> handle) {
            super(handle);
        }

        @Override
        protected Sub newExtensionPoint(ExtensionPointHandle usesite) {
            return new Sub(usesite);
        }

        @Override
        protected void onNew() {
            use(Ex3.Ex3ExtensionPoint.class);
        }

        class Sub extends ExtensionPoint<Ex2> {

            /**
             * @param usesite
             */
            protected Sub(ExtensionPointHandle usesite) {
                super(usesite);
            }

        }
    }

    static final class Ex3 extends Extension<Ex3> {

        /**
         * @param handle
         */
        protected Ex3(ExtensionHandle<Ex3> handle) {
            super(handle);
        }

        @Override
        protected Ex3ExtensionPoint newExtensionPoint(ExtensionPointHandle usesite) {
            return new Ex3ExtensionPoint(usesite);
        }

        class Ex3ExtensionPoint extends ExtensionPoint<Ex3> {

            /**
             * @param usesite
             */
            protected Ex3ExtensionPoint(ExtensionPointHandle usesite) {
                super(usesite);
            }

        }
    }
}
