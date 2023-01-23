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
package app.packed.extension.sandbox;

import java.lang.invoke.MethodHandle;

import app.packed.application.App;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.bean.OnInitialize;
import app.packed.container.BaseAssembly;
import app.packed.extension.BaseExtensionPoint.CodeGenerated;
import app.packed.extension.Extension;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;

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
            System.out.println("New fff");
        }
    }

    public static class EBean {
        final MethodHandle mh;

        public EBean(@CodeGenerated MethodHandle f) throws Throwable {
            this.mh = f;
        }

        @OnInitialize
        public void onInit() throws Throwable {
            mh.invoke();
        }
    }

    public static class MyE extends Extension<MyE> {

        MyE() {}

        BeanHandle<?> h;

        public void ownL(Class<?> cl) {
            h = base().newBean(BeanKind.MANYTON).lifetimes(OperationTemplate.raw()).install(cl);
        }

        public void onAssemblyClose() {

            InstanceBeanConfiguration<EBean> b = base().install(EBean.class);
            base().addCodeGenerated(b, MethodHandle.class, () -> {
                // new Exception().printStackTrace();
                if (h != null) {
                    OperationHandle oh = h.lifetimeOperations().get(0);
                    System.out.println(oh);
                    return oh.generateMethodHandle();
                }
                return null;
            });
        }
    }
}
