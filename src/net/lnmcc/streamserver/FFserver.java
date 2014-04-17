package net.lnmcc.streamserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FFserver {
	private String cfgPath;
	private String ffmpegCfgPath;
	private Process ps = null;
	private boolean needExit = false;
	private Map<String, FFmpeg> ffmpegs;
	private Object syncSS = new Object(); // start stop sync object
	private Object syncAD = new Object(); // add delete sync object
	private static FFserver ffserver = null;

	private FFserver(String cfgPath, String ffmpegCfgPath) {
		ffmpegs = new HashMap<String, FFmpeg>();
		this.cfgPath = cfgPath;
		this.ffmpegCfgPath = ffmpegCfgPath;
		getAllFFmpegs(ffmpegCfgPath);
	}

	/* 当server第一次启动的时候从文件中回复ffmpeg的配置 */
	private void getAllFFmpegs(String ffmpegCfgPath) {
		BufferedReader br = null;
		try {
			File file = new File(ffmpegCfgPath);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file)));

			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public synchronized static FFserver getFFserver(String cfgPath,
			String ffmpegCfgPath) {

		if (ffserver == null)
			ffserver = new FFserver(cfgPath, ffmpegCfgPath);
		return ffserver;
	}

	boolean isExist(String name) {
		return ffmpegs.containsKey(name);
	}

	private String getIdentity(String from) {
		if (!from.startsWith("rtsp://")) {
			throw new IllegalArgumentException("Error rtsp url");
		}

		String str = from.substring(from.indexOf("rtsp://") + 7,
				from.lastIndexOf('/'));
		String identity = str.replace('.', '-').replace(':', '_');
		return identity;
	}

	public boolean addFFmpeg(String from, String to) {
		synchronized (syncAD) {

			String identity = getIdentity(from);
			if (isExist(identity))
				return true;

			FFmpeg ffmpeg = new FFmpeg(from, to);
			ffmpegs.put(identity, ffmpeg);
			// ffmpeg.start();
			return true;
		}
	}

	public boolean deleteFFmpeg(String from) {
		synchronized (syncAD) {

			String identity = getIdentity(from);
			if (!isExist(identity))
				return true;

			FFmpeg ffmpeg = ffmpegs.get(identity);
			if (ffmpeg.isRunning()) {
				System.out.println("FFmpeg is running, stop it first");
				return false;
			}
			// ffmpeg.stop();
			ffmpegs.remove(identity);
			return true;
		}
	}

	boolean startFFmpeg(String from) {
		synchronized (syncAD) {
			String identity = getIdentity(from);

			if (!ffmpegs.containsKey(identity)) {
				System.out.println("You have not add the stream, add first");
				return false;
			}

			FFmpeg ffmpeg = ffmpegs.get(identity);
			if (!ffmpeg.isRunning())
				ffmpeg.start();

			return true;
		}
	}

	boolean stopFFmpeg(String from) {
		synchronized (syncAD) {

			String identity = getIdentity(from);
			if (!ffmpegs.containsKey(identity)) {
				System.out.println("You have not add the stream, add first");
				return false;
			}

			FFmpeg ffmpeg = ffmpegs.get(identity);
			if (ffmpeg.isRunning())
				ffmpeg.stop();

			return true;
		}
	}

	private void stopAllFFmpeg() {
		synchronized (syncAD) {
			Collection<FFmpeg> vals = ffmpegs.values();
			for (FFmpeg ffmpeg : vals) {
				ffmpeg.stop();
			}
		}
	}

	private void startAllFFmpeg() {
		synchronized (syncAD) {
			Collection<FFmpeg> vals = ffmpegs.values();
			for (FFmpeg ffmpeg : vals) {
				ffmpeg.start();
			}
		}
	}

	/**
	 * 启动ffserver, 同时也会启动所有注册的ffmpeg
	 */
	public void start() {
		synchronized (syncSS) {
			if (ps != null) {
				ps.destroy();
				ps = null;
			}

			needExit = false;

			Thread t = new Thread(new Runnable() {
				String[] cmd = new String[] { "ffserver", "-f", cfgPath };

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
							System.out.println("Restarting ffserver ... ...");
							Thread.sleep(1000);
						}

						ps = null;

					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						needExit = true;

						if (ps != null) {
							ps.destroy();
							ps = null;
						}
					}
				}
			});
			t.start();

			while (needExit == false && ps == null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			startAllFFmpeg();
		}
	}

	/**
	 * 停止ffserver,同时也会停止所有已经注册的ffmpeg
	 */
	public void stop() {

		synchronized (syncSS) {
			stopAllFFmpeg();
			needExit = true;

			if (ps != null) {
				ps.destroy();
			}

			while (ps != null) {
				try {
					Thread.sleep(1000);
					// ps.destroy();
					// FIXME
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// public static void main(String[] args) {
	// FFserver ffserver = FFserver.getFFserver();
	// ffserver.setPath(String
	// .valueOf("/home/sijiewang/Projects/stream-media-test/ff.conf"));
	//
	// ffserver.start();
	//
	// ffserver.addFFmpeg(
	// "rtsp://192.168.2.191:554/user=admin&password=admin&channel=1&stream=0.sdp",
	// "http://localhost:8090/192-168-2-191_554.ffm");
	//
	// try {
	// Thread.sleep(5 * 1000);
	// ffserver.stopAllFFmpeg();
	// Thread.sleep(5 * 1000);
	// ffserver.startAllFFmpeg();
	//
	// Thread.sleep(60 * 1000);
	// ffserver.deleteFFmpeg("rtsp://192.168.2.191:554/user=admin&password=admin&channel=1&stream=0.sdp");
	// ffserver.stop();
	// } catch (InterruptedException ex) {
	// ex.printStackTrace();
	// }
	// System.out.println("Main exit");
	// }
}
