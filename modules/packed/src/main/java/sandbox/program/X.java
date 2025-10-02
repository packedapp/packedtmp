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
import app.packed.bean.lifecycle.Initialize;
import app.packed.bean.lifecycle.OnStart;
import app.packed.bean.lifecycle.Stop;
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
            // IO.println("New f");
        }

    }
    public static void main(String[] args) {
        ProgramX p = ProgramX.start(new X());
        IO.println(p.runtime().currentState());
    }

    public static class F {
        public F() {
            // IO.println("New f");
        }

        @Initialize
        public static void init() {
            IO.println("Init F");
        }

        @OnStart
      //  @Fork
        public static void start() {
            IO.println("start F");
        }

        @OnStart(fork = true)
        public static void stardt() {
            IO.println("start F");
        }

        @Stop
        public static void stop() {
            IO.println("stop F");
        }

        @Main
        public void ff() {
            IO.println("MAIN");
        }
    }

    public static class NeedsF {
        public NeedsF(F f) {
            requireNonNull(f);
        }

        @Initialize
        public static void init() {
            IO.println("Init NeedsF");
        }

        @OnStart
        public static void start() {
            IO.println("start NeedsF");
        }

        @Stop
        public static void stop() {
            IO.println("stop NeedsF");
        }
    }
}
