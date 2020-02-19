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
package packed.internal.hook.applicator;

import app.packed.artifact.App;
import app.packed.component.SingletonConfiguration;
import app.packed.container.BaseBundle;
import app.packed.container.Extension;
import app.packed.container.UseExtension;
import app.packed.hook.AssignableToHook;
import app.packed.hook.OnHook;
import packed.internal.hook.applicator.TestIt.FooExtension;

/**
 *
 */
@UseExtension(FooExtension.class)
public class TestIt extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void compose() {
        install(XXX.class);
    }

    public static void main(String[] args) {
        App.start(new TestIt());
    }

    public static class XXX implements Runnable, FooBar {

        /** {@inheritDoc} */
        @Override
        public void run() {
            // TODO Auto-generated method stub

        }
    }

    public interface FooBar {

    }

    public static class FooExtension extends Extension {

        FooExtension() {
            System.out.println("Installed FooExtension");
        }

        @OnHook
        public static void foo(AssignableToHook<FooBar> foob, SingletonConfiguration<?> cc) {
            System.out.println("NICE " + foob.type() + " virker");
            System.out.println(cc.path());
        }

        @OnHook
        public static void fsoo(AssignableToHook<Runnable> foob, SingletonConfiguration<?> cc) {
            System.out.println("NICE " + foob.type() + " virker");
            System.out.println(cc.path());
        }
    }

}
