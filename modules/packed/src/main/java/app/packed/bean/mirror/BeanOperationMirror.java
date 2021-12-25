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
package app.packed.bean.mirror;

import java.util.Optional;

import app.packed.application.ApplicationMirror;
import app.packed.bean.operation.BeanOperationErrorHandlingMirror;
import app.packed.extension.Extension;
import app.packed.lifetime.Lifetime;
import app.packed.mirror.Mirror;

/**
 *
 */
// Is invoked by an extension

// A bean function
// A bean method
// A bean field get/set/compute

// A bean constructor is _not_ an operation...
public abstract class BeanOperationMirror implements Mirror {

    /** {@return the application the operation is a part of.} */
    public final ApplicationMirror application() {
        return bean().application();
    }

    /** {@return the bean that declares the operation.} */
    public final Bean2Mirror bean() {
        throw new UnsupportedOperationException();
    }

    public Optional<Lifetime> lifetime() {
        throw new UnsupportedOperationException();
    }
    
    public Optional<Lifetime> requiresNewLifetime() {
        // @Get may be both
        // @OnInitialize never requires new Lifetime
        throw new UnsupportedOperationException();
    }

    // Might not match the signature of the method.
    // For example, a @Schdule method might have a non-void signature.
    // But we don't use the result
   public final boolean computesResult() {
       return false;
   }

    public final BeanMemberMirror member() {
        throw new UnsupportedOperationException();
    }

    public final BeanOperationErrorHandlingMirror errorHandling() {
        throw new UnsupportedOperationException();
    }

    public final Class<? extends Extension> extension() {
        throw new UnsupportedOperationException();
    }

    // Den her slags functionalitet ligger altid hos Extensions
    public Class<? extends Extension> manager() {
        throw new UnsupportedOperationException();
    }
}
//ExportServiceMirror vs ExportedServiceMirror
//ProvideServiceMirror vs ProvidedServiceMirror
//SubscribeEventMirror vs SubscribedEventMirror
//ConfigUseMirror vs ConfigUsedMirror
//LifecycleCallMirror vs LifecycleCalledMirror
//ScheduleTaskMirror vs ScheduledTaskMirror