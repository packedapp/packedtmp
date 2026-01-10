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
package internal.app.packed.service;

import java.util.Collection;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.BeanMirror;
import app.packed.extension.BaseExtensionMirror;
import app.packed.service.Export;
import app.packed.service.ProvidableBeanConfiguration;
import app.packed.service.Provide;
import app.packed.service.mirrorold.ExportedServiceMirror;

/**
 *
 */
public class CheckCycles extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        //bean().multiInstall(A.class).provide();
        ProvidableBeanConfiguration<AA> aaa = base().install(AA.class).allowMultiClass();
        aaa.provide();
        //aaa.provide();
        provide(AA.class);
        provide(BB.class);
    }

    public static void main(String[] args) {

        ApplicationMirror am = App.mirrorOf(new CheckCycles());
        for (var b : am.container().beans().toList()) {
            IO.println(b.beanClass().getSimpleName() + " " + b.lifecycle().factory().get().target());
        }

//        Collection<ProvidedServiceMirror> c = am.use(BaseExtensionMirror.class).serviceProviders().values();

        Collection<ExportedServiceMirror> ex = am.use(BaseExtensionMirror.class).serviceExports().values();

        BeanMirror b = am.container().beans().iterator().next();

        IO.println(b.componentPath() + " " + b.dependencies().extensions());

       // c.forEach(e -> IO.println(e.bean().componentPath() + " provided by " + e.key()));

        ex.forEach(e -> IO.println(e.bean().componentPath() + " exported by " + e.key()));
    }

    public record DD() {}

    public record CC() {}

    public record AA(BB b, BeanMirror bean) {}

    public record BB() {

        @Provide
        String dppro() {
            return null;
        }

        @Provide
        @Export
        CC ppro() {
            return null;
        }

    }
}
