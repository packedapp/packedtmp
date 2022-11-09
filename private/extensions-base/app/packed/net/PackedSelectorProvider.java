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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

/**
 *
 */
public final class PackedSelectorProvider extends SelectorProvider {

    final SelectorProvider defaultProvider = SelectorProvider.provider();

    private final ChannelManager net;

    PackedSelectorProvider(ChannelManager net) {
        this.net = requireNonNull(net);
    }

    /** {@inheritDoc} */
    @Override
    public DatagramChannel openDatagramChannel() throws IOException {
        // vi kan tilfoeje kanallen
        // tilfoeje den til mappet

        // check if closed and then destroy and remove it.
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public DatagramChannel openDatagramChannel(ProtocolFamily family) throws IOException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Pipe openPipe() throws IOException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public AbstractSelector openSelector() throws IOException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public ServerSocketChannel openServerSocketChannel() throws IOException {
        ServerSocketChannel ssc = defaultProvider.openServerSocketChannel();
        PackedServerSocketChannel pssc = new PackedServerSocketChannel(this, ssc);
        net.openSocketsOrChannels.add(ssc);
        if (net.isClosed) {
            ssc.close(); // will automatically remove it
        }
        return pssc;
    }

    /** {@inheritDoc} */
    @Override
    public SocketChannel openSocketChannel() throws IOException {
        throw new UnsupportedOperationException();
    }
}
