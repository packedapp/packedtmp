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
package app.packed.sidecar;

import java.util.Optional;

/**
 *
 */
public abstract class BuildMethodSidecar extends MethodSidecar {

    public final Optional<Class<?>> buildProcessor() {
        // disabled -> Optional.empty

        // Can specify both
        // class
        // instance
        // prototype
        return Optional.of(getClass());
    }

    public void buildtimeDisable() {}

    // replaces the sidecar with another class that can be used
    // Vi kan jo faktisk generere kode her..
    // Vi kan ogsaa tillade Class instances som saa bliver instantieret..
    // Men helst ikke
    public void setBuildProcessor(Object o) {}

    // Man kan ogsaa have en BuildContext som man kan faa injected...
    //
    public abstract static class BuildTime {

        protected void configure() {

        }
    }

    public final static class BuildTimeContext {
        // Alternativ til BuildTime
    }

    // Things happens here
    // Runtime vs Buildtime
    // Do we take extensions or not
    public @interface AtBuild {
        boolean runtimeBundle() default false;

        boolean buildtimeBundle() default true;
    }
}
