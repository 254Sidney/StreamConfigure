import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/* Represent a common configuration in ffserver.conf */
class Common {
	private Map<String, String> commonVal;

	public Common() {
		commonVal = new LinkedHashMap<String, String>();
		commonVal.clear();
	}

	public void put(String key, String val) {
		commonVal.put(key, val);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Set<Entry<String, String>> sets = commonVal.entrySet();
		for (Entry<String, String> entry : sets) {
			sb.append(entry.getKey());
			sb.append(" ");
			if (entry.getValue() == null)
				sb.append("");
			else
				sb.append(entry.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}
}

/* Represent a Stream section in ffserver.conf */
class Stream {
	private Map<String, String> streamVal;

	public Stream() {
		streamVal = new LinkedHashMap<String, String>();
		streamVal.clear();
	}

	public void addVal(String key, String value) {
		streamVal.put(key, value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Set<Entry<String, String>> sets = streamVal.entrySet();
		for (Entry<String, String> entry : sets) {
			sb.append(entry.getKey());
			sb.append(" ");
			if (entry.getValue() == null)
				sb.append("");
			else
				sb.append(entry.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}
}

/* Represent a Feed section in ffserver.conf */
class Feed {
	private Map<String, String> feedVal;

	public Feed() {
		feedVal = new LinkedHashMap<String, String>();
		feedVal.clear();
	}

	public void addVal(String key, String value) {
		feedVal.put(key, value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Set<Entry<String, String>> sets = feedVal.entrySet();
		for (Entry<String, String> entry : sets) {
			sb.append(entry.getKey());
			sb.append(" ");
			if (entry.getValue() == null)
				sb.append("");
			else
				sb.append(entry.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}
}

/* Represent a ffserver.conf */
public class FFserverCfg {
	private Common commonCfg;
	private Map<String, Feed> feeds;
	private Map<String, Stream> streams;

	public FFserverCfg() {
		commonCfg = new Common();
		feeds = new LinkedHashMap<String, Feed>();
		streams = new LinkedHashMap<String, Stream>();
	}

	void addCommon(String key, String val) {
		commonCfg.put(key, val);
	}

	void addFeed(String name, Feed feed) {
		feeds.put(name, feed);
	}

	void addStream(String name, Stream stream) {
		streams.put(name, stream);
	}

	void addFeedSection(String name) {
		Feed feed = new Feed();
		feed.addVal("File ", "/tmp/" + name + ".ffm");
		feed.addVal("FileMaxSize ", "20000K");
		feed.addVal("ACL allow ", "127.0.0.1");
		feed.addVal("Truncate ", null);
		feeds.put(name + ".ffm", feed);
	}

	void addStreamSection(String name) {
		Stream stream = new Stream();
		stream.addVal("Feed ", name + ".ffm");
		stream.addVal("Format ", "rtp");
		stream.addVal("VideoFrameRate ", "24");
		stream.addVal("VideoBitRate ", "1000");
		stream.addVal("VideoSize ", "720x480");
		stream.addVal("VideoBitRateTolerance ", "1000");
		stream.addVal("VideoGopSize ", "12");
		stream.addVal("StartSendOnKey ", null);
		stream.addVal("VideoQMin ", "3");
		stream.addVal("VideoQMax ", "31");
		stream.addVal("AVOptionVideo flags ", "+global_header");
		stream.addVal("AVOptionVideo qmain ", "3");
		stream.addVal("AVOptionVideo qmax ", "31");
		stream.addVal("AVOptionVideo qdiff ", "4");
		stream.addVal("NoAudio ", null);
		streams.put(name + ".rtp", stream);
	}

	void addCommonSection() {

	}

	/*
	 * 看看给定的流是不是已经在配置文件里了， 这里只需要判断有没有对应的Stream就可以了， 因为我认为Stream和feed一定是成对出现的。
	 * 
	 * @param name
	 * 
	 * @return true-exist; false-not exist
	 */
	boolean isExist(String name) {
		return streams.containsKey(name + ".rtp");
	}

	/**
	 * write this ffserver.conf to file
	 * 
	 * @param pw
	 *            which you want to save this configure
	 * @return void
	 */
	public void writeCfg(PrintWriter pw) {
		pw.println("########## Common ##########");
		pw.println(commonCfg.toString());

		pw.println("########## Feed ##########");
		Set<Entry<String, Feed>> feedSets = feeds.entrySet();
		for (Entry<String, Feed> entry : feedSets) {
			pw.println("<Feed " + entry.getKey() + ">");
			pw.print(entry.getValue().toString());
			pw.println("</Feed>");
			pw.println(" ");
		}

		pw.println("########## Stream ##########");
		Set<Entry<String, Stream>> streamSets = streams.entrySet();
		for (Entry<String, Stream> entry : streamSets) {
			pw.println("<Stream " + entry.getKey() + ">");
			pw.print(entry.getValue().toString());
			pw.println("</Stream>");
			pw.println(" ");
		}
	}

	/**
	 * print this ffserver.conf
	 * 
	 * @param void
	 * @return void
	 */
	public void printCfg() {
		System.out.println("########## Common ##########");
		System.out.println(commonCfg.toString());

		System.out.println("########## Feed ##########");
		Set<Entry<String, Feed>> feedSets = feeds.entrySet();
		for (Entry<String, Feed> entry : feedSets) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue().toString());
		}

		System.out.println("########## Stream ##########");
		Set<Entry<String, Stream>> streamSets = streams.entrySet();
		for (Entry<String, Stream> entry : streamSets) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue().toString());
		}
	}
}
