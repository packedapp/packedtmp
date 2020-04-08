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
package packed.internal.service.build;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import app.packed.artifact.App;
import app.packed.container.BaseBundle;
import app.packed.container.PipelineWirelet;
import app.packed.container.WireletPipeline;

/**
 *
 */
public class WFreePipelinex extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void compose() {
        lookup(MethodHandles.lookup());
        provide(MyComp.class);
    }

    public static void main(String[] args) {
        App.of(new WFreePipelinex(), new SomeWirelet("Saturday"), new SomeWirelet("Sundday"), new SomeWirelet("SunddayXX"));
        App.of(new WFreePipelinex());
        System.out.println("Nye");
    }

    public static class MyComp {

        public MyComp(Optional<SomePipeline> o) {
            if (o.isPresent()) {
                System.out.println(o.get().stream().count());
            } else {
                System.out.println("No pip specified");
            }
        }
    }

    static class SomeWirelet extends PipelineWirelet<SomePipeline> {
        final String x;

        SomeWirelet(String x) {
            this.x = x;
        }
    }

    static class SomePipeline extends WireletPipeline<SomePipeline, SomeWirelet> {}
}
