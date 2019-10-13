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
package app.packed.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.artifact.app.App;
import app.packed.service.Injector;

/**
 * An annotation used to indicate that a particular method should be invoked whenever the declaring entity reaches the
 * {@link RunState#STARTING} state.
 *
 * <p>
 * A simple usage example:
 *
 * <pre>
 * &#064;OnStart()
 * public void runMeWhenComponentIsStarting() {
 *     System.out.println(&quot;Container is being started&quot;);
 *
 * }
 * </pre>
 *
 * Methods annotated with OnStart can have any service that is also available from the component. For example, the
 * following method will print out the name and the state of the container when it starts.
 *
 * <pre>
 * &#064;OnStart()
 * public void hello(Container container) {
 *     System.out.println(&quot;The current state of container &quot; + container.getName() + &quot; is &quot; + container.getState());
 * }
 * </pre>
 * <p>
 * To find out exactly what kind of services that can be injected into the annotated method use an {@link Injector}.
 *
 * <pre>
 * &#064;OnStart()
 * public void showMeWhatCanBeInjected(Injector injector) {
 *     System.out.println(&quot;The following services can be injected into this method&quot;);
 *     for (Class&lt;?&gt; c : injector.services().keySet()) {
 *         System.out.println(c.getCanonicalName());
 *     }
 * }
 * </pre>
 * <p>
 * A common usage examples is initializing services with data:
 *
 * For example, fetching it from disk.
 *
 * <pre>
 * &#064;OnStart()
 * public void hello() throws IOException {
 *     // load data from disk
 *     // prepare service for use
 * }
 * </pre>
 *
 * Notice that this method throws a checked exception. Any method that throws an Exception will result in the container
 * failing to start and moving to the shutdown phase.
 * <p>
 *
 * Normally services are not available from {@link App#use(Class)} until all services have been successfully started.
 * However, by using this annotation. Services that not yet completed startup can be injected. It is up to the user to
 * make sure that invoking method on instances that injected this does not cause any problems. For example, calling a
 * method on another service that only works when the container is in the running phase.
 *
 * @see OnInitialize
 * @see OnStop
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
// @RequiresFeature(LifecycleFeature.class)
public @interface OnStart {
    String[] after() default {};
}
// Problemet med at have baade after og before er at det er forskellige modeller.
// Hvis tilfoejer en before <- Saa virker den paa den maade at skal med en beforeX skal vaere faerdige inde vi kan
// begyndede...
// Maaske kun have starting points til at starte med???

// Maaske gaa over til XXX og *XXX
// XXX venter man paa en, *XXX venter paa alle er faerdige, flere end en kan complete den...

// OnStart()
// @StartPoint();<- fields and methods
// public CompletableFuture<>