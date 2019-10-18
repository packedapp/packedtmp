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
package packed.internal.container.extension;

import static java.util.Objects.requireNonNull;

import app.packed.artifact.App;
import app.packed.artifact.ArtifactImage;
import app.packed.container.BaseBundle;
import app.packed.container.MutableWireletList;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionComposer;
import app.packed.container.extension.ExtensionInstantiationContext;
import app.packed.container.extension.ExtensionWirelet;
import app.packed.container.extension.UseExtension;
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
        ArtifactImage ai = ArtifactImage.of(new WTest(), new MyWirelet("hejhej"), new MyWirelet("hejhej3"));
        App.of(ai);
        App.of(ai, new MyWirelet("A1"), new MyWirelet("A4"));

        App.of(ai, new MyWirelet("A2"));
        System.out.println("-----");
        App.of(ai, new MyWirelet("A4"));
    }

    @UseExtension(ServiceExtension.class)
    public static class MyExtension extends Extension {
        int i;

        public void setIt(int i) {
            this.i = i;
        }

        static final class Composer extends ExtensionComposer<MyExtension> {

            /** {@inheritDoc} */
            @Override
            protected void configure() {
                addPipeline(MyPipeline.class, (e, w) -> new MyPipeline(w));
                onConfigured(e -> e.use(ServiceExtension.class).provide(RuntimeService.class));
                // onInstantiation((e, c) -> System.out.println("Inst " + c.getPipelin(MyPipeline.class)));
            }
        }
    }

    public static class RuntimeService {
        public RuntimeService(MyExtension e, ExtensionInstantiationContext eis) {
            System.out.println("new RuntimeService " + e.i + "  " + eis.getPipelin(MyPipeline.class).val);
        }
    }

    static final class MyPipeline extends ExtensionWirelet.Pipeline<MyExtension, MyPipeline, MyWirelet> {

        String val;

        /**
         * @param wirelets
         */
        protected MyPipeline(MutableWireletList<MyWirelet> wirelets) {
            super(wirelets);
        }

        /**
         * @param myPipeline
         * @param wirelets
         */
        public MyPipeline(MyPipeline myPipeline, MutableWireletList<MyWirelet> wirelets) {
            super(myPipeline, wirelets);
        }

        /** {@inheritDoc} */
        @Override
        protected MyPipeline spawn(MutableWireletList<MyWirelet> wirelets) {
            return new MyPipeline(this, wirelets);
        }

        @Override
        public void onInitialize() {
            for (MyWirelet w : wirelets()) {
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
