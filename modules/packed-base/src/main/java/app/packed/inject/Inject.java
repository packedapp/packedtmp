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
package app.packed.inject;

import static app.packed.lang.invoke.AccessType.INVOKE;
import static app.packed.lang.invoke.AccessType.SET_FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.container.UseExtension;
import app.packed.lang.invoke.OpensFor;
import app.packed.service.ServiceExtension;

/**
 * Unlike many other popular dependency injection frameworks. There are usually no requirements in Packed to use
 * <code>@Inject</code> annotations on the constructor or method that must have dependencies injected. However, in some
 * situations an annotation can be used for providing greater control over how dependencies are bseing injected.
 * <p>
 * One such example is if a dependency should only be injected if it is available. Injecting {@code null} instead of
 * throwing an {@link InjectionException}.
 * <p>
 * While we support direct injection onto fields and into methods. We recommend using constructor injection where ever
 * possible. List reasons...
 * 
 * <p>
 * The annotation can also be applied to
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@UseExtension(ServiceExtension.class) // Replace with UseSidecar(InjectSidecar.class) [maske defineret i denne klasse]
@OpensFor({ INVOKE, SET_FIELD })

// Maaske skal vi endda at hvis man ikke har en sidecar, og kun @OnLifecycle... Saa koerer man setField/invokeMethod
// naar
// man naar det den givne lifecycle... Ja det taenker jeg man goer. Saa slipper man ogsaa for at lave baade en
// InjectFieldSidecar + InjectMethodSidecar
//

public @interface Inject {}

// Field injection first
// Maybe run @Inject noArgConstruct() last
// Used on annotation types, to avoid having to use @Inject @QA("sdsd") to
// It is valid to use @Inject on a method with no parameters, in which it just indicates that the method should be
// invoked during the injection phase

// TODO taenker vi bare bruger en sidecar... Hvis der ikke er nogle services, bruger invoker