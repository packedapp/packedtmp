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

import java.io.IOException;

import app.packed.artifact.App;
import app.packed.artifact.ArtifactImage;
import app.packed.component.Wirelet;
import app.packed.container.BaseBundle;
import app.packed.container.Extension;
import app.packed.service.ServiceExtension;

/**
 *
 */
public class Dddd extends BaseBundle {

    public static void main(String[] args) throws IOException {
        long now = System.currentTimeMillis();
        ArtifactImage ai = ArtifactImage.of(new Dddd());

        // ai.stream().forEach(e -> System.out.println(e.path() + " - " + e.name()));

        System.out.println("------ run time");
        System.out.println(System.currentTimeMillis() - now);
        App a = App.of(ai /* , Wirelet.name("HejHej"), Wirelet.name("HjX") */);

        // a.stream().forEach(e -> System.out.println(e.path() + " " + e.name()));

        System.out.println(a.stream().filter(e -> e.depth() == 2).count());
        System.out.println(System.currentTimeMillis() - now);
    }

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        use(ServiceExtension.class);
        use(MyExtension.class);
        setName("dd?");
        for (int i = 0; i < 100000; i++) {
            link(new LinkMe(20), Wirelet.name("Hej" + i));
        }
    }

    static class MyExtension extends Extension {}

    static class LinkMe extends BaseBundle {

        int depth;

        LinkMe(int depth) {
            this.depth = depth;
        }

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            installInstance("HejHej");
            provideConstant("HejHej");
            if (depth > 0) {
                // link(new LinkMe(depth - 1));
                // link(new LinkMe(depth - 1));
            }
        }
    }
}
