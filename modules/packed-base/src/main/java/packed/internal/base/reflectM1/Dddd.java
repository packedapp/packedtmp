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
package packed.internal.base.reflectM1;

import app.packed.artifact.ArtifactImage;
import app.packed.component.Wirelet;
import app.packed.container.DefaultBundle;

/**
 *
 */
public class Dddd extends DefaultBundle {

    int depth;

    Dddd(int depth) {
        this.depth = depth;
    }

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        // System.out.println(Option.inSameContainer());
        installConstant("sdfsdf").setName("Hej");
        installConstant("sdfsdf");
        installConstant("sdfsdf");
        installConstant("sdfsdf");
        installConstant("sdfsdf");
        if (depth > 0) {
            link(new Dddd(depth - 1), Wirelet.name("X123123XXX"));
            link(new Dddd(depth - 1));
            link(new Dddd(depth - 1));
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        ArtifactImage img = ArtifactImage.of(new Dddd(10));
        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        img = ArtifactImage.of(new Dddd(10));
        System.out.println(System.currentTimeMillis() - start);
        // BundleValidator.assertValid(new Dddd(4));

//        img.stream().forEach(c -> System.out.println(c.path() + " " + c.type()));

        System.out.println("--------------");
        System.out.println(img.stream().count());
        // try (App app = App.start(new Dddd(1))) {
        //
        // // ComponentStream.Option.skipOrigin().andInSameArtifact();
        //
        // app.stream().forEach(c -> System.out.println(c.path() + " " + c.type()));
        // System.out.println(app.stream().count());
        //
        // app.stream().containers().forEach(e -> {
        // System.out.println(e.getClass());
        // });
        // }
    }
}
