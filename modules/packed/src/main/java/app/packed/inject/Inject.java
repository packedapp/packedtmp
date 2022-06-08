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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanExtension;
import app.packed.bean.BeanField;
import app.packed.bean.BeanScanner.MethodHook;

/**
 * Unlike many other popular dependency injection frameworks. There are usually no requirements in Packed to use
 * <code>@Inject</code> annotations on the constructor or method that must have dependencies injected. However, in some
 * situations an annotation can be used for providing greater control over how dependencies are being injected.
 * <p>
 * One such example is if a dependency should only be injected if it is available. Injecting {@code null} instead of
 * throwing an {@link FactoryException}.
 * <p>
 * While we support direct injection onto fields and into methods. We recommend using constructor injection where ever
 * possible. List reasons...
 * 
 * <p>
 * The annotation can also be applied to
 * 
 * <p>
 * Injection of services into static fields or method are not supported. There is no general support for injecting into
 * static fields or methods. If you absolutely need it, it is fairly easy to support using sidecars... TODO example
 */
@Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BeanField.FieldHook(extension = BeanExtension.class, allowSet = true)
@MethodHook(extension = BeanExtension.class, allowInvoke = true)
public @interface Inject {}
