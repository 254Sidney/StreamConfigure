package net.lnmcc.streamserver;

public class StreamServer {
	final private FFserver ffserver;
	final private String ffCfgPath;
	final private Parser parser;

	public StreamServer(String ffCfgPath) {
		this.ffCfgPath = ffCfgPath;
		ffserver = FFserver.getFFserver(ffCfgPath);
		parser = Parser.getParser(ffserver, ffCfgPath);
	}

	public void start() {
		ffserver.start();
	}

	public void stop() {
		ffserver.stop();
	}

	public void addStream(String rtspUrl) {
		parser.parse();
		parser.addStream(rtspUrl);
	}

	public void stopStream() {

	}

	public void deleteStream(String rtspUrl) {
		parser.parse();
		parser.deleteStream(rtspUrl);
	}

	public static void main(String[] args) {
		final String ffCfgPath = new String(
				"/home/sijiewang/Projects/stream-media-test/ff.conf");
		final String rtspUrl1 = "rtsp://192.168.2.191:554/user=admin&password=admin&channel=1&stream=0.sdp";
		final String rtspUrl2 = "rtsp://192.168.2.211:5554/tv.rtp";

		StreamServer streamServer = new StreamServer(ffCfgPath);
		new ShutDownClear(streamServer);

		streamServer.start();
		streamServer.addStream(rtspUrl1);

		try {
			Thread.sleep(30 * 1000);
			streamServer.addStream(rtspUrl2);
			Thread.sleep(30 * 1000);
			streamServer.deleteStream(rtspUrl1);
			Thread.sleep(30 * 1000);
			streamServer.deleteStream(rtspUrl2);

			// streamServer.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}

class ShutDownClear {
	private StreamServer sv;

	public ShutDownClear(StreamServer sv) {
		this.sv = sv;
		doClear();
	}

	private void doClear() {
		Runtime r = Runtime.getRuntime();
		r.addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				sv.stop();
			}
		}));
	}
}
