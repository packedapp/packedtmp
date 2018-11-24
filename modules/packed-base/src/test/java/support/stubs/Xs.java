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
package support.stubs;

import org.junit.jupiter.api.Test;

import app.packed.inject.Injector;
import app.packed.inject.InjectorImportStage;

/**
 *
 */
public class Xs {

    @Test
    public void main() {
        Injector i = Injector.of(c -> {
            c.bind("fooo");
            c.bind(1234);
        });

        Injector i2 = Injector.of(c -> {
            c.injectorBind(i, InjectorImportStage.peek(e -> System.out.println("Available " + e.getKey())), InjectorImportStage.accept(String.class));
        });

        System.out.println();
        i2.getService(String.class).getConfigurationSite().print();

    }
}
