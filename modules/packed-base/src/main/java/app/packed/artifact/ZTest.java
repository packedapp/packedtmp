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
package app.packed.artifact;

import java.lang.invoke.MethodHandles;

import app.packed.container.BaseBundle;
import app.packed.inject.InjectionContext;

/**
 *
 */
public class ZTest extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {}

    public static void main(String[] args) {
        MyArti.d.start(new ZTest());
        System.out.println("NYE");
    }

    public static class MyArti implements AutoCloseable {
        static final ShellDriver<MyArti> d = ShellDriver.of(MethodHandles.lookup(), MyArti.class, MyArti.class);

        MyArti(InjectionContext c) {
            System.out.println(c.keys());
        }

        /** {@inheritDoc} */
        @Override
        public void close() throws Exception {}
    }
}
