package net.lnmcc.streamserver;

import java.util.List;

public class StreamServer {

	final private String ffCfgPath = new String(
			"/home/sijiewang/Projects/stream-media-test/ff.conf");
	final private String ffmpegCfgPath = new String(
			"/home/sijiewang/Projects/stream-media-test/ffmpeg.conf");
	private Parser parser = null;
	private FFserver ffserver = null;

	public StreamServer() {
		ffserver = FFserver.getFFserver(ffCfgPath, ffmpegCfgPath);
		parser = Parser.getParser(ffserver, ffCfgPath);

		ffserver.start();
	}

	public void startFFserver() {
		// FIXME: check the configure file
		parser.parse();
		ffserver.start();
	}

	public void stopFFserver() {
		ffserver.stop();
	}

	/**
	 * 增加一个新的流，如果已经加过了就不要在加入配置文件，我会取rtsp流地址的ip:host作为该流的唯一标志。
	 * 增加一个新的流会导致重启ffserver和所有已注册的ffmpeg。
	 * ffmpeg的输出地址一定是http://localhost:8090/{identity}.ffm 。
	 * 
	 * @param rtspUrl
	 *            rtsp的流地址 比如：
	 *            rtsp://192.168.2.191:554/user=admin&password=admin
	 *            &channel=1&stream=0.sdp
	 * @param audio
	 *            是否有声音
	 * @return 输出流地址
	 */
	public String addStream(String rtspUrl, boolean audio) {
		parser.parse();
		return parser.addStream(rtspUrl, audio);
	}

	/**
	 * 停止一个流只是取消该流在ffserver中的注册并关闭对应的ffmpeg。不会把ffserver.conf中对应的section删除。
	 * 
	 * @param rtspUrl
	 */
	/*
	 * public void stopStream(String rtspUrl) { parser.stopStream(rtspUrl); }
	 * 
	 * public void startStream(String rtspUrl) { parser.startStream(rtspUrl); }
	 */
	/**
	 * 删除一个流首先会停止这个流，然后把与该流相关的信息从ffserver.conf中删除。 这个方法会导致ffserver重启。
	 * ffserver会自动重启他所有注册过的ffmpeg 。
	 * 
	 * @param rtspUrl
	 */
	public void deleteStream(String rtspUrl) {
		parser.parse();
		parser.deleteStream(rtspUrl);
	}

	/**
	 * 获取所有正在流或的流媒体地址
	 * 
	 * @return 流地址，JSON字符串
	 */
	public String getAllStreams() {
		StringBuilder sb = new StringBuilder();
		List<String> rtsps = ffserver.getAllStreams();

		sb.append("{ \"urls\" : [");

		for (String str : rtsps) {
			sb.append("\"");
			sb.append(str);
			sb.append("\",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("] }");

		return sb.toString();
	}

	public static void main(String[] args) {
		final String ffCfgPath = new String(
				"/home/sijiewang/Projects/stream-media-test/ff.conf");
		final String ffmpegCfgPath = new String(
				"/home/sijiewang/Projects/stream-media-test/ffmpeg.conf");
		final String rtspUrl1 = "rtsp://192.168.2.191:554/user=admin&password=admin&channel=1&stream=0.sdp";
		final String rtspUrl2 = "rtsp://192.168.2.211:5554/tv.rtp";

		StreamServer streamServer = new StreamServer();
		new ShutDownClear(streamServer);

		streamServer.startFFserver();
		// streamServer.addStream(rtspUrl1);
		// streamServer.addStream(rtspUrl2);

		try {
			Thread.sleep(50 * 1000); // streamServer.deleteStream(rtspUrl1);
			streamServer.stopFFserver();
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
				sv.stopFFserver();
			}
		}));
	}

}
