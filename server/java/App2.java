package project;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class App2 {
	Hashtable<String, Handler> socketTable;
	Connection connection;
	private static final Log log = LogFactory.getLog(App2.class);

	public App2() throws Exception {
		socketTable = new Hashtable<>();

		this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat", "root", "092259816");

		ServerSocket server = new ServerSocket(1234);
		log.info("waiting...");

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
			new App2();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class Handler extends Thread {

		private Socket socket;
		private int step;
		int senderId, recieverId[], callId;
		Object sleep;
		boolean ready;
		String waitResult;
		BufferedInputStream fbis;
		BufferedOutputStream fbos;
		BufferedInputStream bis;
		BufferedOutputStream bos;
		boolean alive = true;
		String friendCallId;

		public Handler(Socket socket) {
			this.socket = socket;

			PreparedStatement preparedStatement;
			try {
				preparedStatement = connection.prepareStatement("SELECT max(call_id)  FROM log_socket");
				ResultSet result = preparedStatement.executeQuery();
				result.next();
				callId = result.getInt(1);
				callId += 1;

				preparedStatement = connection.prepareStatement("insert into log_socket(call_id,ip) values(?,?)");
				preparedStatement.setInt(1, callId);
				preparedStatement.setString(2, socket.getInetAddress().getHostAddress());
				preparedStatement.executeUpdate();

				System.out.println("Gencallid :" + callId);
				socketTable.put(callId + "", this);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// callId = socketTable.size();
			// socketTable.put(callId + "", this);
			sleep = new Object();
		}

		public void hangup() {
			alive = false;
		}

		public void hangup(String callId) {
			hangup();
			log.info(this.callId + ":hangup:" + friendCallId);
			socketTable.get(callId + "").hangup();
		}

		@Override
		public void run() {

			try {
				// if(socketTable.size()!=2){return;}

				log.info("Incoming connection..." + socket.getRemoteSocketAddress().toString());
				bis = new BufferedInputStream(socket.getInputStream());
				bos = new BufferedOutputStream(socket.getOutputStream());

				// byte[] data = new byte[1024 * 128];
				// int n;
				byte[] buffer = new byte[100 * 1024];
				byte[] data = new byte[100 * 1024];
				int n, buffersize = 0;
				while ((n = bis.read(data)) != -1) {
					log.info("n :" + n);
					if (!alive) {
						log.info("break");
						break;
					}

					try {
						if (step == 0) {
							String request = new String(data, 0, n);
							String[] temp = request.split(":");
							if ((!temp[0].equals("request_call")) && (!temp[0].equals("reject"))
									&& (!temp[0].equals("accept"))) {
								send(bos, "Invalid command");
								break;
							}

							String cmd = temp[0];
							if (cmd.equals("request_call")) {

								boolean flag = true;
								for (int i = 1; i < temp.length; i++)
									if (!temp[i].trim().matches("\\d+")) {
										send(bos, "Id is not number " + temp[i]);
										flag = false;
										break;
									}
								if (flag == false) {
									break;
								}
								senderId = Integer.parseInt(temp[1]);
								recieverId = new int[temp.length - 2];
								for (int i = 2; i < temp.length; i++) {
									recieverId[i - 2] = Integer.parseInt(temp[i]);
								}
								System.out.println("recieverid : " + recieverId.length);

								if (recieverId.length == 1) {
									PreparedStatement preparedStatement = connection.prepareStatement(
											"insert into call_history(call_id,call_sender_id,call_receiver_id,call_status) values(?,?,?,?)");
									preparedStatement.setInt(1, callId);
									preparedStatement.setInt(2, senderId);
									preparedStatement.setInt(3, recieverId[0]);
									preparedStatement.setString(4, "waiting");
									preparedStatement.executeUpdate();
								} else {

								}
								// send(bos, "test:" +
								// System.currentTimeMillis());// send

								PreparedStatement preparedStatement = connection
										.prepareStatement("SELECT user_username FROM users WHERE user_id=?");
								preparedStatement.setString(1, senderId + "");
								ResultSet result = preparedStatement.executeQuery();
								result.next();
								String username_friend = result.getString(1);

								// noti
								sendNoti(senderId + "", recieverId[0] + "", "Incoming Call:" + username_friend,
										"call_from" + senderId + ":" + this.callId);
								send(bos, "waiting:" + recieverId[0] + ":" + this.callId);
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

									friendCallId = tempResult[1];

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

								// System.out.println(callId);

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
								friendCallId = temp[2];
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

							buffersize = 0;// reduce delay
							System.arraycopy(data, 0, buffer, buffersize, n);
							buffersize += n;

							byte[] temp = new byte[4];
							int count = 0;
							System.arraycopy(buffer, count, temp, 0, temp.length);
							count += temp.length;
							int callId = ByteBuffer.wrap(temp).getInt();

							temp = new byte[1];
							System.arraycopy(buffer, count, temp, 0, temp.length);
							count += temp.length;
							int type = ByteBuffer.wrap(temp).get();

							temp = new byte[8];
							System.arraycopy(buffer, count, temp, 2, 6);// 345678
							count += 6;
							long timeStamp = ByteBuffer.wrap(temp).getLong();

							temp = new byte[4];
							System.arraycopy(buffer, count, temp, 2, 2);
							int length = ByteBuffer.wrap(temp).getInt();
							count += 2;

							// System.out.println(callId + ":" + type + ":" +
							// timeStamp + ":" + length + ":" + (n - count)
							// + ":" + buffersize);

							log.info("call_id : " + callId + " type : " + type + " timestamp : " + timeStamp
									+ " length : " + length);

							if (callId < 0 || callId > 100000 || type < 0 || type > 128 || length < 0
									|| length > 200000) {
								// System.out.println("broken pakage");
								log.info("Broken pakage");
								continue;
							}
							if (type == 123) {

								hangup(friendCallId);
							}
							byte[] payLoad = new byte[length];
							System.arraycopy(buffer, count, payLoad, 0, length);

							// int packageSize = count + length;
							//
							// temp = new byte[buffer.length];
							// System.arraycopy(buffer, packageSize, temp, 0,
							// buffersize - packageSize);
							// buffersize -= packageSize;
							// buffer = temp;

							fbos.write(payLoad);
							fbos.flush();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					// System.out.println(this.callId + ":" + n);
					// log.info("call_id : " + this.callId + " length : " + n);
				}

				bis.close();
				bos.flush();
				bos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// System.out.println("END");
			log.info("END");
			// socketTable.clear();
		}

//		private void send(BufferedOutputStream os, String message, int n) throws IOException {
//			os.write(message.getBytes(), 0, n);
//			os.flush();
//		}

		private void send(BufferedOutputStream os, String message) throws IOException {
			os.write(message.getBytes());
			os.flush();
		}

		private void sendNoti(String user_id, String user_id_friend, String topic, String message) {
			try {
				log.info("send noti : " + user_id + " to :" + user_id_friend);
				String token_noti;
				PreparedStatement preparedStatement = connection
						.prepareStatement("select token_body from tokens_notification where user_id = ?");
				preparedStatement.setString(1, user_id_friend);
				ResultSet result = preparedStatement.executeQuery();
				if (result.next()) {
					token_noti = result.getString(1);
					Process p = Runtime.getRuntime().exec(new String[] { "curl", "-X", "POST", "--header",
							"Authorization: key=AAAA7E_TXOo:APA91bG8g5-jIpjhROAm_MZZBzxQWOlzbiTBPDy43InqvVIsHzTI442Y9KU4mlpnR2u15dQo76w1w2xK2viTAd3enIQh11ryx0ONoP9P4kU1VOkqFMvWguDAyTAWWLcjRg0ysqOfpsar4EqeWTb5_NDAk4nC_9nO6A",
							"--Header", "Content-Type: application/json", "https://fcm.googleapis.com/fcm/send", "-d",
							"{\"to\":\"" + token_noti + "\",\"data\":{\"title\":\"" + topic + "\",\"body\":\"" + message
									+ "\",\"tag\":\"" + user_id_friend + "\"},\"priority\":10}" });

					String line;
					BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
					while ((line = input.readLine()) != null) {
						System.out.println(line);
					}
					input.close();

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}
