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
package app.packed.base;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The type of the field or the return type of the method must match the type of attribute.
 * Packed makes no check if any of the types are generic. 
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Opens(to = { OpenMode.METHOD_INVOKE, OpenMode.FIELD_SET })
public @interface AttributeProvide {
    
    Class<?> declaredBy();
    
    /**
     * The name of the attribute that the member provides a value for.
     * @return the name of the attribute that the member provides a value for
     */
    String name();
}
// Meningen man kan bruge den paa sidecars...

// Eneste grund til vi kalder den AttributeProvide istedet for provideAttribute
// code completion...
// No concept of permanent anymore  The specified attribute must be {@link Attribute.Option#permanent() permanent}