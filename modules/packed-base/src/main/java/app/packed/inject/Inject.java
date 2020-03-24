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

import static app.packed.base.invoke.OpenMode.FIELD_GET;
import static app.packed.base.invoke.OpenMode.METHOD_INVOKE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.base.invoke.Opens;
import app.packed.component.Packlet;
import app.packed.service.ServiceExtension;

/**
 * Unlike many other popular dependency injection frameworks. There are usually no requirements in Packed to use
 * <code>@Inject</code> annotations on the constructor or method that must have dependencies injected. However, in some
 * situations an annotation can be used for providing greater control over how dependencies are being injected.
 * <p>
 * One such example is if a dependency should only be injected if it is available. Injecting {@code null} instead of
 * throwing an {@link FactoryDefinitionException}.
 * <p>
 * While we support direct injection onto fields and into methods. We recommend using constructor injection where ever
 * possible. List reasons...
 * 
 * <p>
 * The annotation can also be applied to
 * 
 * <p>
 * There is no general support for injecting into static fields or methods. If you absolutely need it, it is fairly easy
 * to support using sidecars... TODO example
 */
@Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Packlet(extension = ServiceExtension.class) // Replace with UseSidecar(InjectSidecar.class) [maske defineret i denne klasse]
@Opens(to = { METHOD_INVOKE, FIELD_GET })
public @interface Inject {}

//Maaske skal vi endda at hvis man ikke har en sidecar, og kun @OnLifecycle... Saa koerer man setField/invokeMethod
//naar
//man naar det den givne lifecycle... Ja det taenker jeg man goer. Saa slipper man ogsaa for at lave baade en
//InjectFieldSidecar + InjectMethodSidecar
//

// Field injection first
// Maybe run @Inject noArgConstruct() last
// Used on annotation types, to avoid having to use @Inject @QA("sdsd") to
// It is valid to use @Inject on a method with no parameters, in which it just indicates that the method should be
// invoked during the injection phase

// TODO taenker vi bare bruger en sidecar... Hvis der ikke er nogle services, bruger invoker