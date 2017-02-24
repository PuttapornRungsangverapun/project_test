package project;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
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
		BufferedInputStream fbis;
		BufferedOutputStream fbos;
		BufferedInputStream bis;
		BufferedOutputStream bos;

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
				bis = new BufferedInputStream(socket.getInputStream());
				bos = new BufferedOutputStream(socket.getOutputStream());

				byte[] data = new byte[1024 * 128];
				int n;

				while ((n = bis.read(data)) != -1) {

					if (step == 0) {
						String request = new String(data, 0, n);
						String[] temp = request.split(":");
						if ((!temp[0].equals("request_call")) && (!temp[0].equals("reject"))
								&& (!temp[0].equals("accept"))) {
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
							send(bos, "waiting:" + recieverId + ":" + this.callId);// send
																					// noti
							while (!ready) {
								synchronized (sleep) {
									sleep.wait();
								}
							}

							if (waitResult.equals("reject")) {
								send(bos, "reject:" + recieverId);
								break;
							} else if (waitResult.startsWith("start")) {
								String[] tempResult = waitResult.split(":");
								send(bos, tempResult[0] + ":" + tempResult[2]);
								String callId = tempResult[1];
								fbis = socketTable.get(callId + "").bis;
								fbos = socketTable.get(callId + "").bos;
								step = 1;
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
							socketTable.get(callId + "").waitResult = "reject";
							socketTable.get(callId + "").ready = true;
							synchronized (socketTable.get(callId + "").sleep) {
								socketTable.get(callId + "").sleep.notify();
							}

						} else if (cmd.equals("accept")) {
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
							Long time = System.currentTimeMillis();
							PreparedStatement preparedStatement = connection
									.prepareStatement("update call_history set call_status=? where call_id=?");
							preparedStatement.setString(1, "accpet");
							preparedStatement.setInt(2, callId);
							preparedStatement.executeUpdate();
							send(bos, "start:" + time);
							fbis = socketTable.get(callId + "").bis;
							fbos = socketTable.get(callId + "").bos;
							step = 1;

							// for waiting caller
							socketTable.get(callId + "").waitResult = "start:" + this.callId + ":" + time;
							socketTable.get(callId + "").ready = true;
							synchronized (socketTable.get(callId + "").sleep) {
								socketTable.get(callId + "").sleep.notify();
							}

						}
						bos.flush();
					} else if (step == 1) {
						if (n >= 20) {

							byte[] temp = new byte[4];
							int count = 0;
							System.arraycopy(data, count, temp, 0, temp.length);
							count += temp.length;
							int callId = ByteBuffer.wrap(temp).getInt();
							System.arraycopy(data, count, temp, 0, temp.length);
							count += temp.length;
							int type = ByteBuffer.wrap(temp).getInt();
							temp = new byte[8];
							System.arraycopy(data, count, temp, 0, temp.length);
							count += temp.length;
							long timeStamp = ByteBuffer.wrap(temp).getLong();
							temp = new byte[4];
							System.arraycopy(data, count, temp, 0, temp.length);
							int length = ByteBuffer.wrap(temp).getInt();
							count += temp.length;

							System.out.println(callId + ":" + type + ":" + timeStamp + ":" + length);
							byte[] payLoad = new byte[length];
							System.arraycopy(data, count, payLoad, 0, length);

							fbos.write(payLoad);
							fbos.flush();
						}
					}

					System.out.println(this.callId + ":" + n);
				}

				bis.close();
				bos.flush();
				bos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("END");
			socketTable.clear();
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
