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
package internal.app.packed.lifetime;

import static java.util.Objects.requireNonNull;

import app.packed.bean.OnInitialize;
import app.packed.bean.OnStart;
import app.packed.bean.OnStop;
import app.packed.concurrent.Fork;
import app.packed.container.BaseAssembly;
import app.packed.entrypoint.Main;
import internal.app.packed.lifetime.sandbox.Program;

/**
 *
 */
public class X extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provide(NeedsF.class);
        providePrototype(F.class);
    }

    public static void main(String[] args) {
        Program p = Program.start(new X());

        System.out.println(p.runtime().state());
    }

    public static class F {
        public F() {
            // System.out.println("New f");
        }

        @OnInitialize
        public static void init() {
            System.out.println("Init F");
        }

        @OnStart
        @Fork
        public static void start() {
            System.out.println("start F");
        }

        @OnStart(fork = true)
        public static void stardt() {
            System.out.println("start F");
        }

        @OnStop
        public static void stop() {
            System.out.println("stop F");
        }

        @Main
        public void ff() {
            System.out.println("MAIN");
        }

    }

    public static class NeedsF {
        public NeedsF(F f) {
            requireNonNull(f);
        }

        @OnInitialize
        public static void init() {
            System.out.println("Init NeedsF");
        }

        @OnStart
        public static void start() {
            System.out.println("start NeedsF");
        }

        @OnStop
        public static void stop() {
            System.out.println("stop NeedsF");
        }

    }

}
