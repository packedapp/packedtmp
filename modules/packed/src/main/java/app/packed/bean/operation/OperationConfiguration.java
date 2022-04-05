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
package app.packed.bean.operation;

import java.util.function.Supplier;

/**
 *
 */
// Kan godt kalde det BeanOperation... Det er ikke navne brugeren nogensinde bruger...
// Men saa BeanOperationInterceptorMirror... May be okay.

// Den her svare lidt til BeanDriver, og saa alligvl ikke
public interface OperationConfiguration {
    // newInstance(); <-- automatisk???
    
    // sets a non-generic mirror
    public void useMirror(Supplier<? extends OperationMirror> supplier);
}

// addHttpRequest();