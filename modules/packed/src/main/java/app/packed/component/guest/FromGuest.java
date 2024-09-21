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
package app.packed.component.guest;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.extension.BaseExtension;
import app.packed.extension.BeanTrigger.AnnotatedVariableBeanTrigger;

/**
 * Can be used to annotated injectable parameters into a host bean.
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@AnnotatedVariableBeanTrigger(extension = BaseExtension.class)
public @interface FromGuest {}

//FromGuest, FromGuestContainer
//Should be possible to see somewhere what is available, Have A ContainerGuestBean?
//I don't think we need to see at runtime. The guest may provide more, but we have filtered what we want

//Was ContainerCarrierService
//Men taenker vi maaske kan faa BeanHosts paa et tidspunkt (og i saa fald hvor skal den vaere..
//Maaske HostBeanConfiguration som jeg saa heller ikke ved hvor skal vaere
//Maaske kan vi ikke hoste en bean... Men vi kan have en container med en enkelt bean
//Altsaa Containeren ka

//GuestToAdaptor
//Ved ikke om vi vil injecte noget i fx OnComponentGuestRunstateChange

// Host | HostManager