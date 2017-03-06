package project;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class App {
	SourceDataLine line;

	public App() throws Exception {
		ServerSocket server = new ServerSocket(1234);
		System.out.println("waiting...");

		AudioFormat af = new AudioFormat(16000, 8, 1, false, false);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
		line = (SourceDataLine) AudioSystem.getLine(info);
		line.open(af);
		line.start();

		try {
			while (true) {
				new Handler(server.accept()).start();
			}
		} finally {
			server.close();
		}
	}

	public static void main(String[] args) {
		try {
			new App();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class Handler extends Thread {

		private Socket socket;
		BufferedInputStream bis;
		BufferedOutputStream bos;

		public Handler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {

			try {
				System.out.println("Incoming connection..." + socket.getInetAddress().getHostAddress());
				bis = new BufferedInputStream(socket.getInputStream());
				bos = new BufferedOutputStream(socket.getOutputStream());

				byte[] buffer = new byte[100 * 1024];
				byte[] data = new byte[100 * 1024];
				int n, buffersize = 0;

				while ((n = bis.read(data)) != -1) {
					buffersize = 0;// reduce delay
					System.arraycopy(data, 0, buffer, buffersize, n);
					buffersize += n;

					byte[] temp = new byte[4];
					int count = 0;
					System.arraycopy(buffer, count, temp, 0, temp.length);
					count += temp.length;
					int callId = ByteBuffer.wrap(temp).getInt();

					System.arraycopy(buffer, count, temp, 0, temp.length);
					count += temp.length;
					int type = ByteBuffer.wrap(temp).getInt();

					temp = new byte[8];
					System.arraycopy(buffer, count, temp, 0, temp.length);
					count += temp.length;
					long timeStamp = ByteBuffer.wrap(temp).getLong();
					
					System.out.println(System.currentTimeMillis() - timeStamp);

					temp = new byte[4];
					System.arraycopy(buffer, count, temp, 0, temp.length);
					int length = ByteBuffer.wrap(temp).getInt();
					count += temp.length;

					if (callId < 0 || callId > 99 || type < 0 || type > 99 || length < 0 || length > 3000) {
						System.out.println("broken pakage");
						continue;
					}
					System.out.println(callId + ":" + type + ":" + timeStamp + ":" + length + ":" + (n - count) + ":"
							+ buffersize);

					byte[] payLoad = new byte[length];
					System.arraycopy(buffer, count, payLoad, 0, length);

					int packageSize = count + length;

					temp = new byte[buffer.length];
					System.arraycopy(buffer, packageSize, temp, 0, buffersize - packageSize);
					buffersize -= packageSize;
					buffer = temp;

					if (buffersize < 1500) {
						line.write(payLoad, 0, payLoad.length);
					}
					// fbos.write(payLoad);
					// fbos.flush();
				}

				bis.close();
				bos.flush();
				bos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("END");
		}
	}
}
