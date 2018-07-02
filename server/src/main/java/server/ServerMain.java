package server;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import org.pmw.tinylog.Logger;

public class ServerMain {

	public static void main(String[] args) throws IOException {
		InetSocketAddress sockAddr = new InetSocketAddress("localhost", 24);
		AsynchronousServerSocketChannel serverSock = AsynchronousServerSocketChannel.open().bind(sockAddr);

		serverSock.accept(serverSock,
				new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {

					@Override
					public void completed(AsynchronousSocketChannel sockChannel,
							AsynchronousServerSocketChannel serverSock) {
						
						Logger.info("a connection is accepted, start to accept next connection");
						serverSock.accept(serverSock, this);
						Logger.info("start to read message from the client");
						startRead(sockChannel);

					}

					@Override
					public void failed(Throwable exc, AsynchronousServerSocketChannel serverSock) {
						Logger.error("failed to accept a connection: {}", exc.getMessage());
					}

				});
		
		System.in.read();
	}
	
	private static void startRead( AsynchronousSocketChannel sockChannel ) {
        final ByteBuffer buf = ByteBuffer.allocate(2048);
        sockChannel.read( buf, sockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel >() {

            /**
             * some message is read from client, this callback will be called
             */
            @Override
            public void completed(Integer result, AsynchronousSocketChannel channel  ) {

            	if(result == -1) {
            		try {
						channel.close();
						Logger.info("result is -1, closing channel");
					} catch (IOException e) {
						Logger.error("failed to close channel");
					}
            		return;
            	}
                
                Logger.info("read some message from client");
                
                buf.flip();
                
                // echo the message
                startWrite( channel, buf );
                
                //start to read next message again
                startRead( channel );
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel channel ) {
                Logger.error("failed to read: {}", exc.getMessage());
            }
        });
    }
	
	private static void startWrite( AsynchronousSocketChannel sockChannel, final ByteBuffer buf) {
        sockChannel.write(buf, sockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel >() {

            @Override
            public void completed(Integer result, AsynchronousSocketChannel channel) {                 
            	Logger.info("finish to write message to client, nothing to do");
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel channel) {
            	Logger.error("failed to write");
            }
            
        });
    }
}
