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
package aexp.xain;

import app.packed.app.App;
import app.packed.app.Main;
import app.packed.container.Bundle;

/**
 *
 */
public class MainTest2 extends Bundle {

    @Override
    protected void configure() {
        link(new MyMain());
    }

    public static void main(String[] args) {
        App.of(new MainTest2());
    }

    static class MyMain extends Bundle {

        @Override
        protected void configure() {
            System.out.println(path());
            System.out.println(install("foo").path());
            System.out.println(install(334).path());
        }

        @Main
        public static void say() {
            System.out.println("HelloWorld");
        }
    }
}
