/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.operation;

import java.util.List;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.BeanMirror;
import app.packed.service.Provide;

/**
 *
 */
public class Fff extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        installInstance(new Mb());
        provideInstance("qwe");
        provideInstance(List.of());

    }

    public static void main(String[] args) {
        ApplicationMirror am = App.mirrorOf(new Fff());
        List<BeanMirror> list = am.container().beans().toList();
        for (BeanMirror b : list) {
            IO.println("-----");
            IO.println(b);
            b.operations().forEach(c -> IO.println("  " + c.name()));
        }

        IO.println("asd");
    }

    public static class Mb {

        @Provide
        public int foo() {
            return 1;
        }

        @Provide
        public long foo(String s) {
            return 1;
        }

        @Provide
        public short foo(String s, List<?> ff) {
            return 1;
        }
    }
}
