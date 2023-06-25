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
package sandbox.extension.bean.pouch;

import java.lang.invoke.MethodHandles;

import app.packed.bean.BeanTransformer;
import app.packed.bean.Inject;
import app.packed.container.AssemblyHook;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerConfiguration;
import app.packed.extension.BaseExtension;
import app.packed.util.Variable;

/**
 *
 */
public class BeanTransformerUsage {

    public class Usage extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            base().transformNextBean(c -> c.ignoreAllFields(f -> f.getName().equals("foo")));

            install(String.class);
            base().transformNextBean(c -> c.ignoreAllFieldsNamed("foo"));
            install(String.class);

            // Ignore all fields annotated with @Inject
            // AssemblyHook?
            base().transformFutureBeans(c -> c.ignoreAllFields(f -> f.isAnnotationPresent(Inject.class)));

            Runnable cancel = base().transformFutureBeans(c -> {
                if (c.beanClass().getPackageName().equals("foobar")) {
                    System.out.println(c.beanClass());
                }
                c.ignoreAllFields(f -> f.isAnnotationPresent(Inject.class));
            });

            install(String.class);
            install(String.class);
            cancel.run();
        }
    }

    public class MyProc implements AssemblyHook.Processor {

        @Override
        public void beforeBuild(ContainerConfiguration configuration) {
            configuration.use(BaseExtension.class).transformFutureBeans(c -> {

                // replace Jakarta.inject -> Doo.inject.class;
                // if c.eachBeanField.isAnnotatedWith(Inject.class) -> Throw new UOE;
            });
        }
    }

    public class MyBean {

        static {
            BeanTransformer.alwaysTransform(MethodHandles.lookup(), c -> {
                c.addFunction(Variable.of(Void.class), () -> {
                    System.out.println();
                    return null;
                });
            });
        }
    }
}
