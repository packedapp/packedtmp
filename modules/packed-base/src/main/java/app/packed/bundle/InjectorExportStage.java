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
package app.packed.bundle;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import app.packed.inject.Key;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceDescriptor;

/**
 *
 */
// All services available in the staging area, except those exposed
// by the target

// An SES never gets any instances of services it exports itself...

// Altsaa man kan jo ikke slippe for de services, der er brug for.....
// Saa required ligger jo lidt fast, selvom man ikke specificere dem.....

// All available services flows past each export stage.

// When the pipeline is finished, all the services that are part of
// the targets requirements and an.

// Services that accepted by the pipeline but is not either a mandatory or
// optional requirement of the target, are automatically ignore.

// If all mandatory requirements are not met, the export will fail with
// MissingDependencies()

// Do we expose Dependencies????? It is kind of breaking the encapsulation...

// Maa koere en BundleDescriptor hvis man vil se..
// Vi er strictly flow.....Eller det behoever vi jo saadan ikke at vaere.....
// Ellers k
public class InjectorExportStage extends ImportExportStage {

    /** An export stage, that ignores all optional dependencies */
    public static final InjectorExportStage IGNORE_OPTIONAL = null;

    public static final InjectorExportStage ACCEPT_REQUIRED = null;

    public static final InjectorExportStage ACCEPT_REQUIRED_MANDATORYs = null;

    protected final boolean isRequired(Key<?> key) {
        return false;
    }

    protected void filterMandatory(ServiceConfiguration<?> configuration) {
        filter(configuration);
    }

    protected void filterOptional(ServiceConfiguration<?> configuration) {
        filter(configuration);
    }

    protected void filter(ServiceConfiguration<?> configuration) {
        // if (configuration.getKey() isN)
    }

    /**
     * Process each
     * 
     * @param sc
     *            the service configuration
     */
    public void process(ServiceConfiguration<?> sc) {}

    public static InjectorExportStage peek(Consumer<? super ServiceDescriptor> action) {
        requireNonNull(action, "action is null");
        return new InjectorExportStage() {
            @Override
            public void process(ServiceConfiguration<?> sc) {
                action.accept(ServiceDescriptor.of(sc));
            }
        };
    }
}
