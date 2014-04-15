import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FFserver {
	private String cfgPath;
	private Process ps = null;
	private boolean needExit = false;

	public FFserver(String path) {
		cfgPath = path;
	}

	public void start() {
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
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	public void stop() {
		needExit = true;

		if (ps != null) {
			ps.destroy();
			ps = null;
		}
	}

	public static void main(String[] args) {
		FFserver ffserver = new FFserver(
				String.valueOf("/home/sijiewang/Projects/stream-media-test/ff.conf"));
		ffserver.start();
		try {
			Thread.sleep(15000);
			ffserver.stop();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		System.out.println("Main exit");
	}
}
