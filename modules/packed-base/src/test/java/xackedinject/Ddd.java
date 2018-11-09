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
package xackedinject;

import app.packed.inject.Injector;

/**
 *
 */
public class Ddd {

    public Ddd() {}

    public static void main(String[] args) {
        Injector oi = Injector.of(i -> {
            i.bind(Ddd.class);
            i.bind("123.class");
        });

        System.out.println(oi.getService(Ddd.class).getConfigurationSite());
        System.out.println(oi.getService(String.class).getConfigurationSite());

        System.out.println(oi.getService(Ddd.class).getConfigurationSite().parent().get());

        System.out.println(oi.getConfigurationSite());

    }
}
