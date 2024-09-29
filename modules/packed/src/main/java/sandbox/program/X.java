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
package sandbox.program;

import static java.util.Objects.requireNonNull;

import app.packed.assembly.BaseAssembly;
import app.packed.lifecycle.OnInitialize;
import app.packed.lifecycle.OnStart;
import app.packed.lifecycle.OnStop;
import app.packed.lifetime.Main;

/**
 *
 */
public class X extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provide(NeedsF.class);
        provide(F.class);
    }

    public static class Xa {
        @Main
        public void s() {
            // System.out.println("New f");
        }

    }
    public static void main(String[] args) {
        ProgramX p = ProgramX.start(new X());
        System.out.println(p.runtime().currentState());
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
      //  @Fork
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
