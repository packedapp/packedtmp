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
package tests.injector.importing;

import app.packed.bundle.InjectorBundle;
import app.packed.inject.Injector;
import app.packed.inject.InjectorImportStage;

/**
 *
 */
public class ImportTest {

    public static void main(String[] args) {
        //
        System.out.println(ParseIT.def());

        Injector.of(ParseIT.def().getBundleType());

        Injector.of(c -> {
            c.injectorBind(I.class, InjectorImportStage.NONE);

        });
    }

    public final class I extends InjectorBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {}
    }
}
