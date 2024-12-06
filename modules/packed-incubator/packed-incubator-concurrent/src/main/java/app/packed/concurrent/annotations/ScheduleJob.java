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
package app.packed.concurrent.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import app.packed.bean.scanning.BeanTrigger.OnAnnotatedMethod;
import app.packed.concurrent.job2.JobExtension;

/**
 * <p>
 * Operations created from this annotations are always duration based and never takes timezones or Daylight saving times
 * into considation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@OnAnnotatedMethod(allowInvoke = true, introspector = JobExtension.MyI.class)
public @interface ScheduleJob {

    String startingPoint() default "Running";

    long initialDelay() default 0;

    String initialDelayExpression() default "";

    /**
     * @return
     *
     * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, TimeUnit)
     */
    long atFixedRate() default 0;

    String atFixedRateExpression() default "";

    /**
     * @return
     * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)
     */
    long withFixedDelay() default 0;

    String withFixedDelayExpression() default "";

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
