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
package app.packed.cli.other;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import app.packed.assembly.Assembly;
import app.packed.bean.BeanConfiguration;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import app.packed.operation.Op;
import sandbox.lifetime.old.ApplicationConfiguration;

/**
 *
 */
// Kan ikke rigtig lave et prefix interface.
// use(WebExtension.class).spawn().newBean(MyController.class);
// is too much, so we need a single method on the extension

// Det samme for WebServices
// Det samme for Schedule
// Det samme for @OnConnectSocker?
// Generalt for alt hvad der laver mere end 1 entry point vel?

interface Spawn {

    /// 7! metoder.. hmm, hvis det er det der skal til
    /// Scheduling har vel ikke et domain?
    ScheduleOperationConfiguration schedule(Runnable runnable);
    ScheduleOperationConfiguration schedule(Callable<?> callable);
    <T> BeanConfiguration<T> schedule(Class<T> beanClass);
    <T> BeanConfiguration<T> schedule(Op<T> beanClass);
    void scheduleContainer(Assembly assembly, Wirelet... wirelets);
    ContainerConfiguration scheduleContainer(Wirelet... wirelets);

    ApplicationConfiguration scheduleApplication(Wirelet... wirelets);


    WebRequestOperation get(Consumer<WebRequest> request);
    WebRequestOperation post(Consumer<WebRequest> request);

    <T> BeanConfiguration<T> webOp(Class<T> beanClass);
    <T> BeanConfiguration<T> webOp(Op<T> beanClass);
    void webOpContainer(Assembly assembly, Wirelet... wirelets);
    ContainerConfiguration webOpContainer(Wirelet... wirelets);
    ApplicationConfiguration webOpApplication(Wirelet... wirelets);

    interface WebRequestOperation {}
    interface WebRequest{}
    interface ScheduleOperationConfiguration {}
    interface WebOperation {}
}
