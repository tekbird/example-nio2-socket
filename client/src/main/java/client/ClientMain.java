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
						Thread writerThread = new Thread(new Runnable() {

							@Override
							public void run() {

								startWrite(channel);
							}

						});
						writerThread.start();
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
				byte[] input = generateCode();
				channel.write(ByteBuffer.wrap(input));
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Logger.error("failed to write: {}", e.getMessage());
			}
		}
	}

	public static byte[] generateCode() {

		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String fullalphabet = alphabet + alphabet.toLowerCase() + "123456789";
		Random random = new Random();

		char code = fullalphabet.charAt(random.nextInt(9));

		return String.valueOf(code).getBytes();

	}

	protected static void startRead(AsynchronousSocketChannel channel) {
		final ByteBuffer buf = ByteBuffer.allocate(2048);

		channel.read(buf, channel, new CompletionHandler<Integer, AsynchronousSocketChannel>() {

			@Override
			public void completed(Integer result, AsynchronousSocketChannel attachment) {
				byte[] data = new byte[result];
				buf.flip();
				for (int i = 0; i < result; i++) {
					data[i] = buf.get(i);
				}
				Logger.info("read {} bytes {}", result, new String(data));
				startRead(channel);
			}

			@Override
			public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
				Logger.error("failed to read: {}", exc.getMessage());
			}

		});
	}
}
