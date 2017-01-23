package project;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Hashtable;

public class App {
	Hashtable<String, Socket> socketTable;
	Connection connection;

	public App() throws Exception {
		socketTable = new Hashtable<>();

		this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat", "root", "1234");

		ServerSocket server = new ServerSocket(1234);
		System.out.println("waiting...");

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
		private int step;

		public Handler(Socket socket) {
			this.socket = socket;
			socketTable.put(socketTable.size() + "", socket);
		}

		@Override
		public void run() {
			try {
				// if(socketTable.size()!=2){return;}

				System.out.println("Incoming connection...");
				BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
				BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

				byte[] data = new byte[10240];
				int n;

				while ((n = bis.read(data)) != -1) {
					String request = new String(data, 0, n);

					if (step == 0) {
						String[] temp = request.split(":");
						if (!temp[0].equals("request_call")) {
							send(bos, "Need request_call");
							break;
						}

						if (!temp[1].matches("\\d+")) {
							send(bos, "Id is not number");
							break;
						}
						if (!temp[3].matches("\\d+")) {
							send(bos, "Id is not number");
							break;
						}
						send(bos, "calling:" + temp[1]);
					}

					bos.flush();
					System.out.println(n + ":" + data[0]);
				}

				bis.close();
				bos.flush();
				bos.close();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}

			System.out.println("END");
		}

		private void send(BufferedOutputStream os, String message, int n) throws IOException {
			os.write(message.getBytes(), 0, n);
			os.flush();
		}

		private void send(BufferedOutputStream os, String message) throws IOException {
			os.write(message.getBytes());
			os.flush();
		}

	}
}
