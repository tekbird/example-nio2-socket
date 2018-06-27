package client;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Random;

import org.pmw.tinylog.Logger;

public class ClientMain {

	public static void main(String[] args) throws IOException {

		AsynchronousSocketChannel sockChannel = AsynchronousSocketChannel.open();

		sockChannel.connect(new InetSocketAddress("localhost", 24), sockChannel,
				new CompletionHandler<Void, AsynchronousSocketChannel>() {
					@Override
					public void completed(Void result, AsynchronousSocketChannel channel) {

						startRead(channel);
						startWrite(channel);
					}

					@Override
					public void failed(Throwable exc, AsynchronousSocketChannel channel) {
						Logger.info("failed to connect to server: {}", exc.getMessage());
					}

				});

		System.in.read();
	}

	protected static void startWrite(AsynchronousSocketChannel channel) {
		while (true) {
			try {
				byte input = generateCode();
				channel.write(ByteBuffer.wrap(new byte[] { input }));
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Logger.error("failed to write: {}", e.getMessage());
			}
		}
	}

	public static byte generateCode() {

		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String fullalphabet = alphabet + alphabet.toLowerCase() + "123456789";
		Random random = new Random();

		char code = fullalphabet.charAt(random.nextInt(9));

		return (byte) (code >> 2);

	}

	protected static void startRead(AsynchronousSocketChannel channel) {
		final ByteBuffer buf = ByteBuffer.allocate(2048);

		channel.read(buf, channel, new CompletionHandler<Integer, AsynchronousSocketChannel>() {

			@Override
			public void completed(Integer result, AsynchronousSocketChannel attachment) {
				byte[] data = new byte[result];
				buf.get(data, 0, result);
				Logger.info("read {} bytes {}", result, String.format("%02X", data[0]));
				startRead(channel);
			}

			@Override
			public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
				Logger.error("failed to read: {}", exc.getMessage());
			}

		});
	}
}
