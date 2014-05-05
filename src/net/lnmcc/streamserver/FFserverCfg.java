package net.lnmcc.streamserver;

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

	public void addItem(String key, String value) {
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

	public void addItem(String key, String value) {
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
	private Object syncFeeds = new Object();
	private Object syncStreams = new Object();

	public FFserverCfg() {
		commonCfg = new Common();
		feeds = new LinkedHashMap<String, Feed>();
		streams = new LinkedHashMap<String, Stream>();
	}

	void addCommonItem(String key, String val) {
		commonCfg.put(key, val);
	}

	void addFeedSection(String name, Feed feed) {
		synchronized (syncFeeds) {
			feeds.put(name, feed);
		}
	}

	void deleteFeedSection(String name) {
		synchronized (syncFeeds) {
			feeds.remove(name);
		}
	}

	void addStreamSection(String name, Stream stream) {
		synchronized (syncStreams) {
			streams.put(name, stream);
		}
	}

	void deleteStreamSection(String name) {
		synchronized (syncStreams) {
			streams.remove(name);
		}
	}

	void buildCommonSection() {
		addCommonItem("Port ", "8090");
		addCommonItem("MaxClients ", "1000");
		addCommonItem("MaxHttpConnections ", "2000");
		addCommonItem("BindAddress ", "0.0.0.0");
		addCommonItem("RTSPPort ", "5554");
		addCommonItem("MaxBandwidth ", "10000000");
		addCommonItem("CustomLog ", "-");
	}

	void buildFeedSection(String name) {
		Feed feed = new Feed();
		feed.addItem("File ", "/tmp/" + name + ".ffm");
		feed.addItem("FileMaxSize ", "20000K");
		feed.addItem("ACL allow ", "127.0.0.1");
		feed.addItem("Truncate ", null);
		addFeedSection(name + ".ffm", feed);
	}

	void buildStreamSection(String name, boolean audio) {
		Stream stream = new Stream();
		stream.addItem("Format ", "rtp");
		stream.addItem("Feed ", name + ".ffm");
		stream.addItem("VideoCodec ", "libx264");
		stream.addItem("VideoFrameRate ", "24");
		stream.addItem("VideoBitRate ", "2000k");
		stream.addItem("VideoBufferSize ", "10240");
		stream.addItem("VideoSize ", "720x480");
		stream.addItem("AVOptionVideo crf ", "18");
		stream.addItem("AVOptionVideo preset ", "ultrafast");
		stream.addItem("AVOptionVideo flags ", "+loop");
		stream.addItem("AVOptionVideo qmin ", "10");
		stream.addItem("AVOptionVideo qmax ", "51");
		stream.addItem("AVOptionVideo qdiff ", "4");
		stream.addItem("AVOptionVideo flags ", "+global_header");
		//stream.addItem("PixelFormat ", "yuv420p");

		if (audio) {
			stream.addItem("AudioCodec ", "libmp3lame");
			stream.addItem("AudioBitRate ", "512k");
			stream.addItem("AudioChannels ", "2");
			stream.addItem("AVOptionAudio flags ", "+global_header");
		} else {
			stream.addItem("NoAudio ", null);
		}
		addStreamSection(name + ".rtp", stream);
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
