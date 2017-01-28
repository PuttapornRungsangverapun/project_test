package project;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Hashtable;

public class App {
	Hashtable<String, Handler> socketTable;
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
		int senderId, recieverId, callId;
		Object sleep;
		boolean ready;
		String waitResult;

		public Handler(Socket socket) {
			this.socket = socket;
			callId = socketTable.size();
			socketTable.put(callId + "", this);
			sleep = new Object();
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
						if ((!temp[0].equals("request_call")) && (!temp[0].equals("reject"))) {
							send(bos, "Need request_call");
							break;
						}

						String cmd = temp[0];
						if (cmd.equals("request_call")) {
							if (!temp[1].matches("\\d+")) {
								send(bos, "Id is not number");
								break;
							}
							if (!temp[2].matches("\\d+")) {
								send(bos, "Id is not number");
								break;
							}
							senderId = Integer.parseInt(temp[1]);
							recieverId = Integer.parseInt(temp[2]);
							PreparedStatement preparedStatement = connection.prepareStatement(
									"insert into call_history(call_sender_id,call_receiver_id,call_status) values(?,?,?)");
							preparedStatement.setInt(1, senderId);
							preparedStatement.setInt(2, recieverId);
							preparedStatement.setString(3, "waiting");
							preparedStatement.executeUpdate();
							send(bos, "waiting:" + recieverId);
							while (!ready) {
								synchronized (sleep) {
									sleep.wait();
								}
							}

							if (waitResult.equals("reject")) {
								send(bos, "reject:" + recieverId);
								break;
							}

						} else if (cmd.equals("reject")) {
							if (!temp[1].matches("\\d+")) {
								send(bos, "Id is not number");
								break;
							}
							if (!temp[2].matches("\\d+")) {
								send(bos, "Id is not number");
								break;
							}
							senderId = Integer.parseInt(temp[1]);
							int callId = Integer.parseInt(temp[2]);
							PreparedStatement preparedStatement = connection
									.prepareStatement("update call_history set call_status=? where call_id=?");
							preparedStatement.setString(1, "reject");
							preparedStatement.setInt(2, callId);
							preparedStatement.executeUpdate();
							send(bos, "cancel:" + callId);

							// for waiting caller
							socketTable.get(callId+"").waitResult = "reject";
							socketTable.get(callId+"").ready = true;
							synchronized (socketTable.get(callId+"").sleep) {
								socketTable.get(callId+"").sleep.notify();
							}
							
						}
					}

					bos.flush();
					System.out.println(n + ":" + data[0]);
				}

				bis.close();
				bos.flush();
				bos.close();
			} catch (Exception e) {
				e.printStackTrace();
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
