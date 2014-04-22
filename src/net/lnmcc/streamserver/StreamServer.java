package net.lnmcc.streamserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class StreamServer extends HttpServlet {

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
	 * @return
	 */
	public String addStream(String rtspUrl) {
		parser.parse();
		return parser.addStream(rtspUrl);
	}

	/**
	 * 停止一个流只是取消该流在ffserver中的注册并关闭对应的ffmpeg。不会把ffserver.conf中对应的section删除。
	 * 
	 * @param rtspUrl
	 */
	public void stopStream(String rtspUrl) {
		parser.stopStream(rtspUrl);
	}

	public void startStream(String rtspUrl) {
		parser.startStream(rtspUrl);
	}

	/**
	 * 删除一个流首先会停止这个流，然后把与该流相关的信息从ffserver.conf中删除。 这个方法会导致ffserver重启。
	 * ffserver会自动重启他说有注册过的ffmpeg 。
	 * 
	 * @param rtspUrl
	 */
	public void deleteStream(String rtspUrl) {
		parser.parse();
		parser.deleteStream(rtspUrl);
	}
	
	public String getAllStreams() {
		StringBuilder sb = new StringBuilder();
		List<String> rtsps = ffserver.getAllStreams();
		
		sb.append("{ \"urls\" : [");
		
		for(String str : rtsps) {
			sb.append("\"");
			sb.append(str);
			sb.append("\",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("] }");
		
		return sb.toString();
	}

	@Override
	public void init() throws ServletException {
		System.out.println("Init ...");
	}

	@Override
	public void destroy() {
		System.out.println("Destroy");
		stopFFserver();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		System.out.println("doGet ...");
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String newAddress = null;
		String result = null;
		
		System.out.println("doPost ...");
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				req.getInputStream()));
		String jsonStr = "";
		String line;
		while ((line = reader.readLine()) != null) {
			jsonStr += line;
		}
		System.out.println(jsonStr);

		JSONObject obj;
		String cmd;
		String param;

		try {
			obj = JSONObject.fromObject(jsonStr);
			cmd = obj.getString("command");
			param = obj.getString("param");
			System.out.println("command: " + cmd);
			System.out.println("param: " + param);
			
			if(cmd.equalsIgnoreCase("add")) {
				newAddress = addStream(param);
				result = "{\"url\":" + "\"" + newAddress + "\""  + "}";
				resp.getWriter().print(result);
				
			} else if (cmd.equalsIgnoreCase("delete")) {
				deleteStream(param);
			} else if(cmd.equalsIgnoreCase("stop")) {
				stopStream(param);
			} else if(cmd.equalsIgnoreCase("query")) {
				result = getAllStreams();
				resp.getWriter().print(result);
			}

		} catch (JSONException e) {
			e.printStackTrace();
			result = "{\"url\":\"\"}";
			resp.getWriter().print(result);
			return;
		}

	}
	/*
	 * public static void main(String[] args) { final String ffCfgPath = new
	 * String( "/home/sijiewang/Projects/stream-media-test/ff.conf"); final
	 * String ffmpegCfgPath = new String(
	 * "/home/sijiewang/Projects/stream-media-test/ffmpeg.conf"); final String
	 * rtspUrl1 =
	 * "rtsp://192.168.2.191:554/user=admin&password=admin&channel=1&stream=0.sdp"
	 * ; final String rtspUrl2 = "rtsp://192.168.2.211:5554/tv.rtp";
	 * 
	 * StreamServer streamServer = new StreamServer(ffCfgPath, ffmpegCfgPath);
	 * new ShutDownClear(streamServer);
	 * 
	 * streamServer.startFFserver(); //streamServer.addStream(rtspUrl1);
	 * //streamServer.addStream(rtspUrl2);
	 * 
	 * try { Thread.sleep(50 * 1000); //streamServer.deleteStream(rtspUrl1);
	 * streamServer.stopFFserver(); } catch (Exception ex) {
	 * ex.printStackTrace(); } } }
	 * 
	 * class ShutDownClear { private StreamServer sv;
	 * 
	 * public ShutDownClear(StreamServer sv) { this.sv = sv; doClear(); }
	 * 
	 * private void doClear() { Runtime r = Runtime.getRuntime();
	 * r.addShutdownHook(new Thread(new Runnable() {
	 * 
	 * @Override public void run() { sv.stopFFserver(); } })); }
	 */
}
