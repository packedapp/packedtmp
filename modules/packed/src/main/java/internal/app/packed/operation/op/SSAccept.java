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
package internal.app.packed.operation.op;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 *
 */
public class SSAccept {

    public static void main(String[] args) throws IOException, InterruptedException {
        Thread t = new Thread(new Foo());
        Thread.sleep(1000);

        t.interrupt();
        
        
        Thread.sleep(100000);
    }

    // ListenToSocket();

    // SocketChannelProcessor

    public static class Foo implements Runnable {

        /** {@inheritDoc} */
        @Override
        public void run() {
            ServerSocketChannel c = null;
            try {
                c = ServerSocketChannel.open();
                System.out.println(c.isBlocking());
                c.bind(new InetSocketAddress("localhost", 7000));

                while (true) {
                    SocketChannel channel = c.accept();

                    // Where should I delegate the channel to

                    System.out.println("ACCEPTED " + channel);
                }
            } catch (IOException e) {
                System.out.println(c.isBlocking());
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }
}
