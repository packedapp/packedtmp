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
package app.packed.bean.mirror.old;

import java.util.List;

import app.packed.inject.mirror.Dependency;

/**
 *
 */
public interface BeanMemberMirror extends BeanElementMirror {

    /**
     * Returns the Class object representing the class or interface that declares the member or constructor represented by
     * this Member.
     *
     * @return an object representing the declaring class of the underlying member
     */
    public Bean2Mirror declaringBean(); // bean()

    List<Dependency> dependencies(); // What are we having injected... Giver det mening for functions????

    /**
     * Returns {@code true} if this member was introduced by the Packed; returns {@code false} otherwise.
     *
     * @return true if and only if this member was introduced by the Packed.
     */
    public boolean isSynthetic();
    
    /** {@return the simple name of the underlying member.} */
    public String name();
}
