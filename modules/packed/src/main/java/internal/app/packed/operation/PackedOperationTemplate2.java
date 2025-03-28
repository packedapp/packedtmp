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
package internal.app.packed.operation;

import java.util.List;

import app.packed.util.Nullable;
import internal.app.packed.context.PackedContextTemplate;
import internal.app.packed.operation.PackedOperationTemplate.ReturnKind;

/**
 *
 */
public record PackedOperationTemplate2(
        ReturnKind returnKind,
        Class<?> returnClass,
        boolean extensionContext,
        @Nullable Class<?> beanClass,
        List<PackedContextTemplate> contexts,
        List<Class<?>> args

        )



{

}
