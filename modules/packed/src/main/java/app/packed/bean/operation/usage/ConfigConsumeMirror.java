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
package app.packed.bean.operation.usage;

/**
 * A mirror for an operation that involves consuming a Config element.
 */
// Er det en speciel usesite mirror???

// Not an operation....
// Fordi
// @ScheduleTask
// public void foo(@ConsumeConfig("foo.bar"))
// Her er Schedule operation, og ConsumeConfig noget dependency knald

public abstract class ConfigConsumeMirror /*extends OperationMirror */ {

    public abstract String path();
    
    public abstract Object scope();
}
