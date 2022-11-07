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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import app.packed.operation.Provider;

/**
 *
 */
public class S0SAccept2 {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("DAV");
        ChannelManager mn = new ChannelManager();
        PackedSelectorProvider psp = new PackedSelectorProvider(mn);
        
        ServerSocketChannel ssc = psp.openServerSocketChannel();
        
        Thread t = new Thread(new Foo(ssc));
        t.start();
        Thread.sleep(1000);

        mn.closeAll();
        //t.interrupt();

        Thread.sleep(100000);
        
        System.out.println("Bte");
    }

    // ListenToSocket();

    // SocketChannelProcessor
    
    
    public class MyBean {
        // Must always manual bind to a socket...
        // So if want to create Multiple... we need a provider
        Provider<Socket> socketProvider;
        
    }

    public record Foo(ServerSocketChannel c) implements Runnable {

        
        /** {@inheritDoc} */
        @Override
        public void run() {
            try {
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
