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
import app.packed.container.BaseBundle;
import app.packed.container.Extension;
import app.packed.service.ServiceExtension;

/**
 *
 */
public class Dddd extends BaseBundle {

    public static void main(String[] args) {
        ArtifactImage ai = ArtifactImage.of(new Dddd());

        ai.stream().forEach(e -> System.out.println(e.path() + " - " + e.name()));

        System.out.println("------ run time");

        App a = App.of(ai /* , Wirelet.name("HejHej"), Wirelet.name("HjX") */);

        a.stream().forEach(e -> System.out.println(e.path() + " " + e.name()));

    }

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        use(ServiceExtension.class);
        use(MyExtension.class);
        setName("dd?");
        link(new LinkMe());
    }

    static class MyExtension extends Extension {}

    static class LinkMe extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            provideConstant("HejHej");
        }
    }
}
