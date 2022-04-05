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

/**
 *
 */

// The lifecycle of it is managed by Packed...

// Maybe interface??? 

// Ideen er at runtime'n instantiere kanallen, som bliver holdt inde i maven...

//@ExtensionMember(NetExtension.class)
public interface ManagedServerSocketChannel {
    
    public static ManagedServerSocketChannel open(Option... options) {
        throw new UnsupportedOperationException();
    }
    
    public interface Option {
        
    }
}
/// Interface med Config
/// Listener
//