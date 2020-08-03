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
package packed.internal.host.prototype;

/**
 *
 */
// A host that only allows definitions at assembly time...
// This should probably be the default...
// And if we want to do lazy initialize/start is the way...

// lazy(Bundle b, Wirelet... wirelets); just wraps LazyStart I think...

// Also ServiceMesh... Should be fine for static host...

public interface StaticHost<A> {

}
