import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ParserCfg {
	private String ffCfgPath;
	private File ffCfgFile;
	private FFserverCfg ffservercfg;

	private State commonState;
	private State feedState;
	private State streamState;
	private State state;

	public ParserCfg(String path) {
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

	void addCommon(String key, String val) {
		ffservercfg.addCommon(key, val);
	}

	void addFeed(String name, Feed feed) {
		ffservercfg.addFeed(name, feed);
	}

	void addStream(String name, Stream stream) {
		ffservercfg.addStream(name, stream);
	}

	void classify(String line) {
		state.classify(line);
	}

	void addFeedSection(String name) {
		ffservercfg.addFeedSection(name);
	}

	void addStreamSection(String name) {
		ffservercfg.addStreamSection(name);
	}

	void addCommonSection() {
		ffservercfg.addCommonSection();
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
	 * 增加一个新的流，如果已经加过了就不要在加入配置文件，我会取rtsp流地址的ip:host作为该流的唯一标志
	 * 
	 * @param rtspUrl
	 *            rtsp的流地址
	 * @return
	 */
	public void addNewStream(String rtspUrl) {
		if (!rtspUrl.startsWith("rtsp://")) {
			throw new IllegalArgumentException("Error rtsp url");
		}

		String str = rtspUrl.substring(rtspUrl.indexOf("rtsp://") + 7,
				rtspUrl.lastIndexOf('/'));
		String identity = str.replace('.', '-').replace(':', '_');
		if (ffservercfg.isExist(identity)) {
			System.out.println("Exist"); // FIXME
		} else {
			addFeedSection(identity);
			addStreamSection(identity);
			writeCfg(ffCfgPath);
		}
		
		/*Restart ffserver*/
		
	}

	public void parse() {
		ffCfgFile = new File(ffCfgPath);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(ffCfgFile));
			String line = null;

			while ((line = reader.readLine()) != null) {
				// skip comment line and space line
				if (!line.startsWith(String.valueOf("#")) && !line.isEmpty()
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

	public static void main(String[] args) {
		ParserCfg parser = new ParserCfg(
				"/home/sijiewang/MyDisk/Projects/stream-media-test/ff.conf");
		parser.parse();
		// parser.printCfg();
		parser.addNewStream(String
				.valueOf("rtsp://192.168.2.191:554/user=admin&password=admin&channel=1&stream=0.sdp"));
		
		parser.addNewStream(String
				.valueOf("rtsp://192.168.2.211:5554/tv.rtp"));
		// parser.writeCfg(String.valueOf("/home/sijiewang/MyDisk/Projects/stream-media-test/ff.conf"));

	}
}
