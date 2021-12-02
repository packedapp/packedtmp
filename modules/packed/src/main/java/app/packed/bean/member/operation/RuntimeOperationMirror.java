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
package app.packed.bean.member.operation;

import app.packed.application.ApplicationMirror;
import app.packed.bean.member.BeanClassMirror;
import app.packed.bean.member.BeanElementMirror;
import app.packed.extension.Extension;
import app.packed.mirror.Mirror;

/**
 *
 */
public interface RuntimeOperationMirror extends Mirror {

    default ApplicationMirror application() {
        return bean().application();
    }
    
    // Den her slags functionalitet ligger altid hos Extensions
    Class<? extends Extension> manager();

    /** {@return the bean that declares the operation.} */
    BeanClassMirror bean();

    BeanElementMirror element(); // Altsaa det er vel naermest aldrig en constructor???

    RuntimeOperationErrorHandlingMirror errorHandling(); // What happens if the operation fails
    
    
    // Might not match the signature of the method.
    // For example, a @Schdule method might have a non-void signature.
    // But we don't use the result
    boolean computesResult();
}
//ExportServiceMirror vs ExportedServiceMirror
//ProvideServiceMirror vs ProvidedServiceMirror
//SubscribeEventMirror vs SubscribedEventMirror
//ConfigUseMirror vs ConfigUsedMirror
//LifecycleCallMirror vs LifecycleCalledMirror
//ScheduleTaskMirror vs ScheduledTaskMirror