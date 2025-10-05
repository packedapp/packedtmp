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
package sandbox.extension.sandbox;

import java.lang.invoke.MethodHandle;

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.Bean;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanLifetime;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.bean.lifecycle.Initialize;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionContext;
import app.packed.extension.ExtensionHandle;

/**
 *
 */
public class MhExt extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        installInstance("ASASD");
        use(MyE.class).ownL(FFF.class);
    }

    public static void main(String[] args) {
        App.run(new MhExt());
    }

    public static class FFF {
        FFF() {
            IO.println("New fff");
        }
    }

    public static class EBean {
        final MethodHandle mh;

        public EBean(ExtensionContext context, MethodHandle f) throws Throwable {
            IO.println("Got computed F");
            IO.println(f.type());
            this.mh = f;
        }

        @Initialize
        public void onInit(ExtensionContext ec) throws Throwable {
            FFF fff = (FFF) mh.invokeExact(ec);

            IO.println(fff);
        }
    }

    public static class MyE extends Extension<MyE> {

        /**
         * @param handle
         */
        protected MyE(ExtensionHandle<MyE> handle) {
            super(handle);
        }

        BeanHandle<?> h;

        public void ownL(Class<?> cl) {
            BeanInstaller builder = base().newBean(BeanLifetime.UNMANAGED.template());
            h = builder.install(Bean.of(cl), BeanHandle::new);
        }

        @Override
        public void onConfigured() {
            InstanceBeanConfiguration<EBean> b = base().install(EBean.class);
            b.bindServiceInstance(MethodHandle.class, h.lifecycleInvokers().get(0).invoker().asMethodHandle());
        }
    }
}
