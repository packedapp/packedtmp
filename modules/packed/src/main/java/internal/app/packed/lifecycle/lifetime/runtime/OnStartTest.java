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
package internal.app.packed.lifecycle.lifetime.runtime;

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.lifecycle.LifecycleDependantOrder;
import app.packed.bean.lifecycle.OnStart;
import app.packed.bean.lifecycle.OnStartContext;

/**
 *
 */
public class OnStartTest extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(Mine.class);
    }

    public static void main(String[] args) {
        App.run(new OnStartTest());
    }

    public static class Mine {

        @OnStart(order = LifecycleDependantOrder.BEFORE_DEPENDANTS)
        public void onStart(OnStartContext context) {
            System.out.println("1 - NICE " + Thread.currentThread());
        }

        @OnStart(fork = true)
        public void onStartdx(OnStartContext context) throws InterruptedException {
            System.out.println("2 - Started " + Thread.currentThread());
            context.fork(() -> {
                System.out.println("2 - NICE from " + Thread.currentThread());
                context.fork(() -> {
                    System.out.println("2 - NICE2 from " + Thread.currentThread());
                });
                context.fork(() -> {
                    System.out.println("2 - NICE2 from " + Thread.currentThread());
                });

            });
            Thread.sleep(500);
        }

        @OnStart(fork = true)
        public void onStart2(OnStartContext context) throws InterruptedException {
            System.out.println("3 - Started " + Thread.currentThread());
            context.fork(() -> {
                System.out.println("3 - NICE " + Thread.currentThread());
            });
            Thread.sleep(300);
        }
    }
}
