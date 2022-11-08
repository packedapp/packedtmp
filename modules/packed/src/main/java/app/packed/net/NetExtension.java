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
package app.packed.net;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.channels.spi.SelectorProvider;

import app.packed.bean.BeanExtension;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.CustomHook;
import app.packed.bean.CustomHook.CustomBindingHook;
import app.packed.container.Extension;
import app.packed.container.Extension.DependsOn;
import app.packed.operation.Op1;

/**
 *
 */
@DependsOn(extensions = BeanExtension.class)
public final class NetExtension extends Extension<NetExtension> {
    NetExtension() {}

    /**
     * The bean introspector returned by this method handles:
     * 
     * <ul>
     * <li>{@link SelectorProvider}. A selector provider can be injected.</li>
     * </ul>
     * 
     * 
     * 
     */
    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {

            @Override
            public void onBinding(OnBinding binding) {
                if (binding.variable().getType() == SelectorProvider.class) {
                    binding.provide(new Op1<>(PackedSelectorProvider::new) {});
                } else {
                    super.onBinding(binding);
                }
                bean().install(ChannelManager.class);
            }
        };
    }

    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @CustomHook
    @CustomBindingHook(className = "java.nio.channels.spi.SelectorProvider")
    public @interface JavaNetworkSupport {} // JavaNioChannelSupport

}
