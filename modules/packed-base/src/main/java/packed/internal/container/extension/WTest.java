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

import app.packed.app.App;
import app.packed.container.BaseBundle;
import app.packed.container.MutableWireletList;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionComposer;
import app.packed.container.extension.ExtensionWirelet;
import app.packed.container.extension.ExtensionWireletPipeline;

/**
 *
 */
public class WTest extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        use(MyExtension.class);
    }

    public static void main(String[] args) {
        App.of(new WTest(), new MyWirelet("hejhej"), new MyWirelet("hejhej3"));
    }

    public static class MyExtension extends Extension {

        static final class Composer extends ExtensionComposer<MyExtension> {

            /** {@inheritDoc} */
            @Override
            protected void configure() {
                useWirelets(MyPipeline.class, (e, w) -> new MyPipeline(e, w));
            }

        }
    }

    static final class MyPipeline extends ExtensionWireletPipeline<MyExtension, MyPipeline, MyWirelet> {

        /**
         * @param extension
         * @param wirelets
         */
        protected MyPipeline(MyExtension extension, MutableWireletList<MyWirelet> wirelets) {
            super(extension, wirelets);
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
                System.out.println(w.msg);
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
    }
}
