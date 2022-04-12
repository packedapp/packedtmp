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
package app.packed.bean.hooks.operations.dependecy;

/**
 *
 */


// Hmm det den ikke daekker er fx. @ExtractHeader
// Hvis den bare har en UnresolvedServiceErrorMessage...
// Saa ignorere vi den jo bare

// See RequiresContextHook
public @interface UnresolvedServiceErrorMessage {
    String value();
}
// Usage

@UnresolvedServiceErrorMessage("HttpRequest is only available for HttpRequest operations")
interface HttpRequest {
    
}


// skal extendes. Og operationDriver + Interface/Annotering skal alle have den configureret
abstract class OperationContext {
    
    public String outsideOperationContext() {
        return "HttpRequest is only available for HttpRequest operations";
    }
}


// Der er vel 4 "Context" typer???
// None - no strings attached
// Container - Some property of the container, Must be registered with EntityManagerExtension i en speciel container
// Bean - Some property of the bean, for example, an entity bean, @DatabaseMapFoo is only available for entity beans
// Operation context - Some property of the operation, for example, a HTTP Request