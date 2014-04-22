package net.lnmcc.streamserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

public class Parser {
	private String ffCfgPath;
	private File ffCfgFile;
	private FFserverCfg ffservercfg;
	private FFserver ffserver;

	private State commonState;
	private State feedState;
	private State streamState;
	private State state;

	private static Parser parser = null;

	public synchronized static Parser getParser(FFserver ffserver,
			String ffCfgPath) {
		if (parser == null)
			parser = new Parser(ffserver, ffCfgPath);
		return parser;
	}

	private Parser(FFserver ffserver, String path) {
		commonState = new CommonState(this);
		feedState = new FeedState(this);
		streamState = new StreamState(this);
		state = commonState;
		ffservercfg = new FFserverCfg();

		if (path == null) {
			// use default
			ffCfgPath = String.valueOf("/etc/ffserver.conf");
		} else {
			ffCfgPath = path;
		}
		this.ffserver = ffserver;
	}

	public void printCfg() {
		ffservercfg.printCfg();
	}

	State getCommonState() {
		return commonState;
	}

	State getFeedState() {
		return feedState;
	}

	State getStreamState() {
		return streamState;
	}

	void setState(State st) {
		state = st;
	}

	void addCommonItem(String key, String val) {
		ffservercfg.addCommonItem(key, val);
	}

	void addFeed(String name, Feed feed) {
		ffservercfg.addFeedSection(name, feed);
	}

	void addStream(String name, Stream stream) {
		ffservercfg.addStreamSection(name, stream);
	}

	void buildFeedSection(String name) {
		ffservercfg.buildFeedSection(name);
	}

	void buildStreamSection(String name) {
		ffservercfg.buildStreamSection(name);
	}

	void classify(String line) {
		state.classify(line);
	}

	/**
	 * write this ffserver.conf to file
	 * 
	 * @param path
	 *            which you want to save this configure
	 * @return void
	 */
	public void writeCfg(String path) {
		PrintWriter pw = null;
		File dstFile;
		try {
			dstFile = new File(path);
			if (!dstFile.exists())
				dstFile.createNewFile();

			pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
					dstFile)), true);
			/* empty the file */
			pw.print(String.valueOf(""));
			ffservercfg.writeCfg(pw);

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	/**
	 * 增加一个新的流，如果已经加过了就不要在加入配置文件，会取rtsp流地址的ip:host作为该流的唯一标志。
	 * 增加一个新的流会导致重启ffserver和所有已注册的ffmpeg重启。
	 * ffmpeg的输出地址一定是http://localhost:8090/{identity}.ffm 。
	 * 
	 * @param rtspUrl
	 *            rtsp的流地址 比如：
	 *            rtsp://192.168.2.191:554/user=admin&password=admin
	 *            &channel=1&stream=0.sdp
	 * @return String 转换后的流地址
	 */
	public String addStream(String rtspUrl) {

		if (!rtspUrl.startsWith("rtsp://")) {
			throw new IllegalArgumentException("Error rtsp url");
		}

		String str = rtspUrl.substring(rtspUrl.indexOf("rtsp://") + 7,
				rtspUrl.lastIndexOf('/'));
		String identity = str.replace('.', '-').replace(':', '_');

		/* 对应的流信息已经在ffserver.conf中了 */
		if (ffservercfg.isExist(identity)) {
			System.out.println("Exist");

			/* 查询这个流是否已经在跑了 */
			if (!ffserver.isExist(identity)) {
				/* 注册一个新的ffmpeg用于新的流 */
				ffserver.startFFmpeg(rtspUrl, "http://localhost:8090/"
						+ identity + ".ffm");
				// ffserver.startFFmpeg(rtspUrl);
			}

		} else {
			buildFeedSection(identity);
			buildStreamSection(identity);
			/* Restart ffserver, use new configure file */
			ffserver.stop();
			writeCfg(ffCfgPath);
			ffserver.start();

			/* 注册一个新的ffmpeg用于新的流 */
			ffserver.startFFmpeg(rtspUrl, "http://localhost:8090/" + identity
					+ ".ffm");
			// ffserver.startFFmpeg(rtspUrl);
		}

		String result = null;
		InetAddress addr = null;
		String ip = null;

		try {
			NetworkInterface ifr = NetworkInterface.getByName("eth0");
			Enumeration<InetAddress> ei = ifr.getInetAddresses();
			while (ei.hasMoreElements()) {
				addr = ei.nextElement();
				if (addr instanceof Inet4Address) {
					ip = addr.getHostAddress().toString();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		result = new String("rtsp://" + ip + ":5554/" + identity + ".rtp");
		System.out.println("New rtsp address: " + result);

		return result;
	}

	/**
	 * 删除一个流首先会停止这个流，然后把与该流相关的信息从ffserver.conf中删除。 这个方法会导致ffserver重启。
	 * ffserver会自动重启他说有注册过的ffmpeg 。
	 * 
	 * @param rtspUrl
	 */
	public void deleteStream(String rtspUrl) {
		if (!rtspUrl.startsWith("rtsp://")) {
			throw new IllegalArgumentException("Error rtsp url");
		}

		ffserver.stopFFmpeg(rtspUrl);

		String str = rtspUrl.substring(rtspUrl.indexOf("rtsp://") + 7,
				rtspUrl.lastIndexOf('/'));
		String identity = str.replace('.', '-').replace(':', '_');

		if (!ffservercfg.isExist(identity))
			return;

		ffservercfg.deleteFeedSection(identity + ".ffm");
		ffservercfg.deleteStreamSection(identity + ".rtp");
		writeCfg(ffCfgPath);
		ffserver.stop();
		ffserver.start();
	}

	/**
	 * 停止一个流只是取消该流在ffmserver中的注册并关闭对应的ffmpeg。不会把ffserver.conf中对应的section删除。
	 * 
	 * @param rtspUrl
	 */
	public void stopStream(String rtspUrl) {
		if (!rtspUrl.startsWith("rtsp://")) {
			throw new IllegalArgumentException("Error rtsp url");
		}

		ffserver.stopFFmpeg(rtspUrl);
	}

	/**
	 * 启动一个流，前提是ffserver.conf中对应的section，否则需要先addStream
	 * 
	 * @param rtspUrl
	 */
	public void startStream(String rtspUrl) {
		if (!rtspUrl.startsWith("rtsp://")) {
			throw new IllegalArgumentException("Error rtsp url");
		}

		addStream(rtspUrl);
	}

	public void parse() {
		ffCfgFile = new File(ffCfgPath);
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(ffCfgFile));
			String line = null;

			while ((line = reader.readLine()) != null) {
				// skip comment line and space line
				if (!line.startsWith(String.valueOf("##")) && !line.isEmpty()
						&& !line.matches("^\\s*\n$") && !line.matches("\\s*$"))
					classify(line);
			}

			reader.close();

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	// public static void main(String[] args) {
	// final String rtspUrl =
	// "rtsp://192.168.2.191:554/user=admin&password=admin&channel=1&stream=0.sdp";
	// Parser parser = new
	// Parser("/home/sijiewang/MyDisk/Projects/stream-media-test/ff.conf");
	// parser.parse();
	// // parser.printCfg();
	// parser.addNewStream(rtspUrl);
	//
	// //
	// parser.addNewStream(String.valueOf("rtsp://192.168.2.211:5554/tv.rtp"));
	// //
	// parser.writeCfg(String.valueOf("/home/sijiewang/MyDisk/Projects/stream-media-test/ff.conf"));
	//
	// try {
	// Thread.sleep(20 * 1000);
	// parser.stopStream(rtspUrl);
	// Thread.sleep(2 * 1000);
	// parser.deleteStream(rtspUrl);
	// Thread.sleep(2 * 1000);
	// parser.addNewStream(rtspUrl);
	//
	// } catch (InterruptedException ex) {
	// ex.printStackTrace();
	// }
	// }
}
