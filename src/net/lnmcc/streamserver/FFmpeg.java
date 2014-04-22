package net.lnmcc.streamserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class FFmpeg implements Serializable {

	private static final long serialVersionUID = 1L;
	private String from;
	private String to;
	transient private Process ps = null;
	transient private boolean running = false;

	public FFmpeg(String from, String to) {
		this.from = from;
		this.to = to;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {

		in.defaultReadObject();
		ps = null;
		running = false;
	}

	public void start() {
		synchronized (this) {

			if (ps != null) {
				ps.destroy();
				ps = null;
			}
			running = true;

			Thread t = new Thread(new Runnable() {
				String[] cmd = new String[] { "/usr/local/bin/ffmpeg", "-i", from, to };

				@Override
				public void run() {
					try {
						while (running == true) {
							ps = Runtime.getRuntime().exec(cmd);
							BufferedReader br = new BufferedReader(
									new InputStreamReader(ps.getErrorStream()));

							String line;
							while ((line = br.readLine()) != null) {
								System.out.println(line);
							}

							ps.waitFor();
							Thread.sleep(1000);
						}

						ps = null;

					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						running = false;

						if (ps != null) {
							ps.destroy();
							ps = null;
						}
					}
				}
			});
			t.start();

			while (running == true && ps == null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public void stop() {
		synchronized (this) {
			running = false;

			if (ps != null) {
				ps.destroy();
			}

			while (ps != null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean isRunning() {
		synchronized (this) {
			return running;
		}
	}
	// public static void main(String[] args) {
	// FFmpeg ffmpeg = new FFmpeg(
	// "rtsp://192.168.2.191:554/user=admin&password=admin&channel=1&stream=0.sdp",
	// "http://localhost:8090/192-168-2-191_554.ffm");
	// ffmpeg.start();
	// try {
	// Thread.sleep(60 * 1000);
	// ffmpeg.stop();
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// System.out.println("Main exit");
	// }
}
