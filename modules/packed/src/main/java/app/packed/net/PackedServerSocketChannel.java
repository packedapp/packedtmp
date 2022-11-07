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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 *
 */
public final class PackedServerSocketChannel extends ServerSocketChannel {

    final ServerSocketChannel delegateTo;

    /**
     * @param provider
     * @throws IOException 
     */
    protected PackedServerSocketChannel(PackedSelectorProvider provider, ServerSocketChannel delegateTo) throws IOException {
        super(provider);
        this.delegateTo = delegateTo;
    }

    /** {@inheritDoc} */
    public SocketChannel accept() throws IOException {
        try {
            return delegateTo.accept();
        } catch (Throwable t) {
            if (!delegateTo.isOpen()) {
                close();
            }
            throw t;
        }
    }

    public ServerSocketChannel bind(SocketAddress local, int backlog) throws IOException {
        try {
            delegateTo.bind(local, backlog);
        } catch (Throwable t) {
            if (!delegateTo.isOpen()) {
                close();
            }
            throw t;
        }
        return this;
    }

    public SocketAddress getLocalAddress() throws IOException {
        try {
            return delegateTo.getLocalAddress();
        } catch (Throwable t) {
            if (!delegateTo.isOpen()) {
                close();
            }
            throw t;
        }
    }

    public <T> T getOption(SocketOption<T> name) throws IOException {
        try {
            return delegateTo.getOption(name);
        } catch (Throwable t) {
            if (!delegateTo.isOpen()) {
                close();
            }
            throw t;
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void implCloseSelectableChannel() throws IOException {
        delegateTo.close();
    }

    /** {@inheritDoc} */
    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        try {
            delegateTo.configureBlocking(block);
        } catch (Throwable t) {
            if (!delegateTo.isOpen()) {
                close();
            }
            throw t;
        }
    }

    public <T> ServerSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        try {
            return delegateTo.setOption(name, value);
        } catch (Throwable t) {
            if (!delegateTo.isOpen()) {
                close();
            }
            throw t;
        }
    }

    public ServerSocket socket() {
        return delegateTo.socket();
    }

    public Set<SocketOption<?>> supportedOptions() {
        return delegateTo.supportedOptions();
    }

    public String toString() {
        return delegateTo.toString();
    }
}
