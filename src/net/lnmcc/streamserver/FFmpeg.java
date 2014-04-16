package net.lnmcc.streamserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FFmpeg {
	private String from;
	private String to;
	private Process ps = null;
	private boolean needExit = false;

	public FFmpeg(String from, String to) {
		this.from = from;
		this.to = to;
	}

	public void start() {
		synchronized (this) {

			if (ps != null) {
				ps.destroy();
				ps = null;
			}
			needExit = false;

			Thread t = new Thread(new Runnable() {
				String[] cmd = new String[] { "ffmpeg", "-i", from, to };

				@Override
				public void run() {
					try {
						while (needExit == false) {
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
					} catch (Exception e) {
						e.printStackTrace();
					}
					ps = null;
				}
			});
			t.start();
		}
	}

	public void stop() {
		synchronized (this) {
			needExit = true;

			if (ps != null) {
				ps.destroy();
			}
		}
	}

	public static void main(String[] args) {
		FFmpeg ffmpeg = new FFmpeg(
				"rtsp://192.168.2.191:554/user=admin&password=admin&channel=1&stream=0.sdp",
				"http://localhost:8090/192-168-2-191_554.ffm");
		ffmpeg.start();
		try {
			Thread.sleep(60 * 1000);
			ffmpeg.stop();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Main exit");
	}
}
