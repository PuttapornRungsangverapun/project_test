package project;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class App3group2 {
	Hashtable<String, List<Handler>> socketTable;
	Connection connection;
	private static final Log log = LogFactory.getLog(App3group2.class);

	public App3group2() throws Exception {
		socketTable = new Hashtable<>();

		this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat", "root", "092259816");

		ServerSocket server = new ServerSocket(4000);
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
			new App3group2();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class Handler extends Thread {

		private Socket socket;
		private int step;
		int senderId, recieverId, callId;
		int groupCallId;
		Object sleep;
		BufferedInputStream bis;
		BufferedOutputStream bos;

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
				List<Handler> handlers = new ArrayList<>();
				handlers.add(this);
				socketTable.put(callId + "", handlers);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			sleep = new Object();
		}

		@Override
		public void run() {

			try {
				log.info("Incoming connection..." + socket.getRemoteSocketAddress().toString());
				bis = new BufferedInputStream(socket.getInputStream());
				bos = new BufferedOutputStream(socket.getOutputStream());

				new Thread(new Runnable() {

					@Override
					public void run() {
						while (true) {
							while (bufferSize == 0) {
								try {
									Thread.sleep(100); 
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
//								try {
//									synchronized (sync) {
//										sync.wait();
//									}
//								} catch (InterruptedException e) {
//									e.printStackTrace();
//								}
							}
							
							short[] dump = null;
							synchronized (buffer) {
								dump = new short[bufferSize];
								System.arraycopy(buffer, 0, dump, 0, bufferSize);
								bufferSize = 0;
							}

							byte[] out = new byte[dump.length * 2];

							for (int i = 0; i < dump.length; i++) {
								byte[] tmp = ByteBuffer.allocate(2).putShort(dump[i]).array();
								System.arraycopy(tmp, 0, out, i * 2, 2);
							}

							try {
								bos.write(out);
							} catch (IOException e) {
								e.printStackTrace();
								break;
							}
							log.info("send back to user: " + out.length);
						}

					}
				}).start();

				byte[] buffer = new byte[100 * 1024];
				byte[] data = new byte[100 * 1024];
				int n, buffersize = 0;
				while ((n = bis.read(data)) != -1) {
					try {
						if (step == 0) {
							String request = new String(data, 0, n);
							log.info(request);
							String[] temp = request.split(":");
							if ((!temp[0].equals("request_call")) && (!temp[0].equals("reject"))
									&& (!temp[0].equals("accept"))) {
								send(bos, "Invalid command");
								break;
							}

							String cmd = temp[0];
							if (cmd.equals("request_call")) {
								groupCallId = callId;
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
								recieverId = Integer.parseInt(temp[2]);
								// recieverId = new int[temp.length - 1];
								// for (int i = 1; i < temp.length; i++) {
								// recieverId[i - 1] =
								// Integer.parseInt(temp[i]);
								// }
								// System.out.println("recieverid : " +
								// recieverId.length);

								// if (recieverId.length == 1) {
								PreparedStatement preparedStatement = connection.prepareStatement(
										"insert into call_history(call_id,call_sender_id,call_receiver_id,call_status) values(?,?,?,?)");
								preparedStatement.setInt(1, callId);
								preparedStatement.setInt(2, senderId);
								preparedStatement.setInt(3, recieverId);
								preparedStatement.setString(4, "waiting");
								preparedStatement.executeUpdate();

								// send(bos, "test:" +
								// System.currentTimeMillis());// send
								// noti

								preparedStatement = connection.prepareStatement(
										"SELECT users.user_id FROM users LEFT JOIN groups_users ON users.user_id=groups_users.user_id WHERE groups_users.group_id=?");
								preparedStatement.setInt(1, recieverId);
								ResultSet result = preparedStatement.executeQuery();
								while (result.next()) {
									if (result.getInt(1) != senderId) {
										sendNoti(senderId + "", result.getInt(1) + "", "Incoming Call",
												"call_group_from" + senderId + ":" + this.callId);
									}
								}

								send(bos, "waiting:" + recieverId + ":" + this.callId);

								while (socketTable.get(this.callId + "").size() <= 1) {
									synchronized (sleep) {
										sleep.wait();
									}

									// if (waitResult.equals("reject")) {
									// send(bos, "reject:" + recieverId);
									// ready=false;
									// }
									// else{
									// break;
									// }
								}

								// else if (waitResult.startsWith("start")) {
								// String[] tempResult = waitResult.split(":");
								// send(bos, tempResult[0] + ":" +
								// tempResult[2]);
								// String callId = tempResult[1];
								// fbis = socketTable.get(callId + "").bis;
								// fbos = socketTable.get(callId + "").bos;
								List<String> inGroup = new ArrayList<>();
								for (Handler s : socketTable.get(this.callId + "")) {
									inGroup.add(s.callId + "");
								}

								log.info("In group there are " + String.join(",", inGroup));
								step = 1;
								send(bos, "start:" + this.callId + ":" + System.currentTimeMillis());
								// }

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

								System.out.println(callId);

								// for waiting caller
								// socketTable.get(callId + "").waitResult =
								// "reject";
								// socketTable.get(callId + "").ready = true;
								// synchronized (socketTable.get(callId +
								// "").sleep) {
								// socketTable.get(callId + "").sleep.notify();
								// }

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
								groupCallId = callId;
								Long time = System.currentTimeMillis();
								PreparedStatement preparedStatement = connection
										.prepareStatement("update call_history set call_status=? where call_id=?");
								preparedStatement.setString(1, "accpet");
								preparedStatement.setInt(2, callId);
								preparedStatement.executeUpdate();
								send(bos, "start:" + time);
								// fbis = socketTable.get(callId + "").bis;
								// fbos = socketTable.get(callId + "").bos;
								step = 1;

								// for waiting caller
								// socketTable.get(callId +
								// "").get(0).waitResult = "start:" +
								// this.callId + ":" + time;
								// socketTable.get(callId + "").ready = true;
								socketTable.get(groupCallId + "").add(this);
								synchronized (socketTable.get(groupCallId + "").get(0).sleep) {
									socketTable.get(groupCallId + "").get(0).sleep.notify();
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

							if (callId < 0 || type < 0 || type > 99 || length < 0 || length > 100000) {
								System.out.println("broken pakage");
								continue;
							}
							System.out.println(callId + ":" + type + ":" + timeStamp + ":" + length + ":" + (n - count)
									+ ":" + buffersize);

							byte[] payLoad = new byte[length];
							System.arraycopy(buffer, count, payLoad, 0, length);

							int packageSize = count + length;

							temp = new byte[buffer.length];
							System.arraycopy(buffer, packageSize, temp, 0, buffersize - packageSize);
							buffersize -= packageSize;
							buffer = temp;

							for (Handler handler : socketTable.get(groupCallId + "")) {
								if (handler != this) {
									log.info(this.callId + " send payload to " + handler.callId);
									// handler.bos.write(payLoad);
									// handler.bos.flush();
									handler.putData(payLoad, payLoad.length);
									// handler.sumWithCurrentData(payLoad);
								}
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
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

		short[] buffer = new short[100 * 1024];
		int bufferSize = 0;
		Object sync = new Object();

		public void putData(byte[] data, int length) {
			log.info("Put data to" + this.callId + ":" + length);

			short[] dataShort = new short[length / 2];

			length = dataShort.length;

			for (int i = 0; i < length; i++) {
				byte[] tmp = new byte[2];
				System.arraycopy(data, i * 2, tmp, 0, 2);
				dataShort[i] = ByteBuffer.wrap(tmp).getShort();
			}

			synchronized (buffer) {
				for (int i = 0; i < Math.max(bufferSize, length); i++) {
					short sample1 = 0;
					short sample2 = 0;

					if (i < bufferSize) {
						sample1 = buffer[i];
					}

					if (i < length) {
						sample2 = dataShort[i];
					}

					// System.out.println("BUF_SIZE = " + sample1 + ":" +
					// sample2);

					float samplef1 = sample1 / 32768.0f;
					float samplef2 = sample2 / 32768.0f;
					float mixed = samplef1 + samplef2;

					// hard clipping
					if (mixed > 1.0f) {
						// mixed = 1.0f;
					}
					if (mixed < -1.0f) {
						// mixed = -1.0f;
					}

					short outputSample = (short) (mixed * 32768.0f);
					// System.out.println("OUT_SAMPLE = " + sample1 + " + "
					// +sample2 + " = " + outputSample);

					buffer[i] = outputSample;
				}

				// buffer = dataShort;
				bufferSize = Math.max(bufferSize, length);
			}

			synchronized (sync) {
				sync.notify();
			}
		}

	}
}
