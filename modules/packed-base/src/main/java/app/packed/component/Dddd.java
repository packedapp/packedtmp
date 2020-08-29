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
package app.packed.component;

import app.packed.artifact.App;
import app.packed.container.BaseBundle;

/**
 *
 */
public class Dddd extends BaseBundle {

    public static final String D = fff();

    /** {@inheritDoc} */
    @Override
    protected void configure() {}

    public static String fff() {
        new Exception().printStackTrace();
        return "asd";
    }

    public static void main(String[] args) throws Exception {
        Package pkg = Class.forName("app.packed.component" + ".package-info").getPackage();
        System.out.println(pkg.isAnnotationPresent(Deprecated.class));
        // PackageOwner packageOwner = pkg.getAnnotation(PackageOwner.class);

        String s;
        try (App a = App.start(new Dddd())) {
            s = a.use(String.class);
        }
        System.out.println(s);
    }
}