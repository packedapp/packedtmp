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
package packed.internal.container.extension.test;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.artifact.App;
import app.packed.artifact.ArtifactImage;
import app.packed.container.BaseBundle;
import app.packed.container.Extension;
import app.packed.container.ExtensionComposer;
import app.packed.container.ExtensionWirelet;
import app.packed.service.ServiceExtension;

/**
 *
 */
public class WTest extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        use(ServiceExtension.class);
        use(MyExtension.class).setIt(123);
    }

    public static void main(String[] args) {
        ArtifactImage ai = ArtifactImage.build(new WTest(), new MyWirelet("hejhej"), new MyWirelet("hejhej3"));
        App.start(ai);
        App.start(ai, new MyWirelet("A1"), new MyWirelet("A4"));

        App.start(ai, new MyWirelet("A2"));
        System.out.println("-----");
        App.start(ai, new MyWirelet("A4"));
    }

    // @UseExtension(ServiceExtension.class)
    public static class MyExtension extends Extension {
        int i;

        public void setIt(int i) {
            this.i = i;
        }

        static final class Composer extends ExtensionComposer<MyExtension> {

            /** {@inheritDoc} */
            @Override
            protected void configure() {
                addPipeline(MyPipeline.class, e -> new MyPipeline());
                onConfigured(e -> e.use(ServiceExtension.class).provide(RuntimeService.class));
                addDependencies(ServiceExtension.class);
                // onInstantiation((e, c) -> System.out.println("Inst " + c.getPipelin(MyPipeline.class)));
            }
        }
    }

    public static class RuntimeService {
        public RuntimeService(MyExtension e, Optional<MyPipeline> mp) {
            System.out.println("new RuntimeService " + e.i + "  " + mp.get().val);
        }
    }

    static final class MyPipeline extends ExtensionWirelet.Pipeline<MyExtension, MyPipeline, MyWirelet> {

        String val;

        @Override
        public void onInitialize() {
            for (MyWirelet w : this) {
                this.val = w.msg;
            }
        }
    }

    static final class MyWirelet extends ExtensionWirelet<MyPipeline> {

        final String msg;

        /**
         * @param msg
         */
        public MyWirelet(String msg) {
            this.msg = requireNonNull(msg);
        }

        @Override
        public String toString() {
            return "msg(\"" + msg + "\")";
        }
    }
}
