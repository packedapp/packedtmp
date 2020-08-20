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
package zamples;

import app.packed.artifact.App;
import app.packed.artifact.ArtifactImage;
import app.packed.component.BeanConfiguration;
import app.packed.container.BaseBundle;

/**
 *
 */
public class Ztes extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        BeanConfiguration<?> sc = install(DD.class);

        System.out.println(sc.key());
        sc.provide();
        System.out.println(sc.key());
        System.out.println();
    }

    public static void main(String[] args) {
        ArtifactImage<App> ai = App.newImage(new Ztes());
        ai.stream().forEach(e -> System.out.println(e.path() + " - " + e.name()));
        // App.of(new Ztes());
    }

    public static class DD {}
}
