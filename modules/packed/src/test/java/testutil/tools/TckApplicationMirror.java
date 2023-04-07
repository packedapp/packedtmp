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
package testutil.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import app.packed.application.ApplicationMirror;
import app.packed.bean.BeanMirror;
import app.packed.container.Author;

/** A special mirror returned by the {@link TckApp}. */
public class TckApplicationMirror extends ApplicationMirror {

    /** {@return a bean mirror for the a single application bean.} */
    public BeanMirror singleApplicationBean() {
        List<BeanMirror> beans = container().beans().filter(b -> b.owner() == Author.application()).toList();
        assertThat(beans).hasSize(1);
        return beans.get(0);
    }
}
