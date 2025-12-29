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

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanMirror;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.service.Provide;
import app.packed.service.mirror.ServiceBindingMirror;
import app.packed.service.mirrorold.ProvidedServiceMirror;

/**
 *
 */
public class TestNew extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provideInstance("foo");
        provide(Fop.class);
        install(Gop.class);
    }

    public static void main(String[] args) {
        App.run(new TestNew());

        App.mirrorOf(new TestNew()).print();
        IO.println("_---");

        for (BeanMirror b : App.mirrorOf(new TestNew()).container().beans().toList()) {
            b.operations().ofType(ProvidedServiceMirror.class).forEach(e -> {
                List<ServiceBindingMirror> sbm = e.useSites().toList();
                IO.println(sbm);
                IO.println("Bean " + b.componentPath() + " provides services for key " + e.key());
                for (var v : sbm) {
                    IO.println("Bound to " + v.operation().target());
                }
            });
        }
        //
        IO.println("Bye");
    }

    public static class Gop {
        public Gop(Integer i) {
            IO.println("New Gop " + i);
        }

//
//        @ProvideService
//        public Long sss(Integer i) {
//            return 34L;
//        }

    }

    public static class Fop {

        @Provide
        public static Integer foo(@XX("Nice") String s) {
            IO.println("Got " + s);
            return 34;
        }
    }

    public static class MyExt extends Extension<MyExt> {

        /**
         * @param handle
         */
        MyExt(ExtensionHandle<MyExt> handle) {
            super(handle);
        }

        static class MyI extends BeanIntrospector<MyExt> {
            @Override
            public void onAnnotatedVariable(Annotation hook, OnVariable h) {
                String str = h.annotations().readRequired(XX.class).value();
                h.bindConstant(str.toUpperCase());
            }
        }
    }

    @Target({ ElementType.PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    // @OnAnnotatedVariable(extension = MyExt.class)
    @interface XX {
        String value();
    }
}
