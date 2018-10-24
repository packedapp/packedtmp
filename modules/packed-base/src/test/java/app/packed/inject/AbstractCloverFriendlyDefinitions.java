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

import java.util.List;

import support.stubs.annotation.SystemProperty;

/**
 * This class sole exist so clover can run, as it does not support type annotations.
 * https://bitbucket.org/openclover/clover/issues/20/clov-1839-clover-fails-to-instrument
 */
public class AbstractCloverFriendlyDefinitions {

    static final Key<List<Integer>> KEY_ = new Key<@SystemProperty("fff") List<Integer>>() {};
}
