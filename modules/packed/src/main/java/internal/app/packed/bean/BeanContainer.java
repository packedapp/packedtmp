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
package internal.app.packed.bean;

import java.util.HashMap;
import java.util.Map;

import app.packed.bean.MultipleBeanOfSameTypeDefinedException;

/**
 *
 */
public class BeanContainer {

    private Map<Class<?>, Object> beanClassMap = new HashMap<>();

    void add(BeanSetup bs) {
        class MuInst {
            int counter;
        }
        int mc = 0;
        Class<?> cl = bs.props.beanClass();
        if (bs.props.beanClass() != void.class) {
            if (bs.props.multiInstall()) {
                MuInst i = (MuInst) beanClassMap.compute(cl, (c, o) -> {
                    if (o == null) {
                        return new MuInst();
                    } else if (o instanceof BeanSetup) {
                        throw new MultipleBeanOfSameTypeDefinedException();
                    } else {
                        return o;
                    }
                });
                mc = i.counter;
                i.counter++;

            } else {
                beanClassMap.compute(cl, (c, o) -> {
                    if (o == null) {
                        return bs;
                    } else if (o instanceof BeanSetup) {
                        throw new MultipleBeanOfSameTypeDefinedException();
                    } else {
                        // We already have some multiple beans installed
                        throw new MultipleBeanOfSameTypeDefinedException();
                    }
                });
            }
        }
    }

}
