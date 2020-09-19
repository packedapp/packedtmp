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
package packed.internal.methodhandle;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 *
 */
public class MethodHandleUtil {

    public static MethodHandle castReturnType(MethodHandle target, Class<?> newReturnType) {
        return target.asType(target.type().changeReturnType(newReturnType));
    }

    public static MethodHandle replaceParameter(MethodHandle target, int position, MethodHandle replaceWith) {
        return MethodHandles.filterArguments(target, position, replaceWith);
    }
}
