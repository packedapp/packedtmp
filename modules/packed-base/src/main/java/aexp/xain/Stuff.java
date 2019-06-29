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

import app.packed.app.AppBundle;
import app.packed.app.Main;
import app.packed.container.ArtifactImage;

/**
 *
 */
public class Stuff extends AppBundle {

    private static final ArtifactImage IMAGE = newImage(new Stuff());

    @Override
    protected void configure() {
        install(this);
    }

    public static void main(String[] args) {
        run(IMAGE);
    }

    @Main
    public static void saySomething() {
        System.out.println("!!!!!!!!!!!HelloWorld!!!!!!!!!!!");
    }
}
