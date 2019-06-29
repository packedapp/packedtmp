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
import app.packed.app.AppBundle;
import app.packed.app.Main;
import app.packed.container.ArtifactImage;

/**
 *
 */
public class MainTest extends AppBundle {

    static final ArtifactImage IMAGE = newImage(new MainTest());

    @Override
    protected void configure() {
        install(new MyMain());
        System.out.println(path());
    }

    public static void main(String[] args) {
        MyMain.foo();
        var app = App.of(new MainTest());
        app.stream().forEach(e -> System.out.println(e.path()));

        ArtifactImage i = ArtifactImage.of(new MainTest());

        System.out.println("-----");
        app = App.of(i);
        app.stream().forEach(e -> System.out.println(e.path()));
        app = App.of(i);
        System.out.println("-----");
        app.stream().forEach(e -> System.out.println(e.path()));
    }

    static class MyMain {

        @Main
        public static void say() {
            System.out.println("!!!!!!!!!!!HelloWorld!!!!!!!!!!!");
        }

        @Main
        public static void say2() {
            System.out.println("!!!!!!!!!!!HelloWorld!!!!!!!!!!!");
        }

        static void foo() {
            // throw new Error();
        }
    }

}
