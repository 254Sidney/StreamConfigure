import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class Common {
	private Map<String, String> commonVal;

	public Common() {
		commonVal = new HashMap<String, String>();
		commonVal.clear();
	}

	public void put(String key, String val) {
		commonVal.put(key, val);
	}

	public void print() {
		Set<Entry<String, String>> sets = commonVal.entrySet();
		for (Entry<String, String> entry : sets) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
	}
}

class Stream {
	private Map<String, String> streamVal;

	public Stream() {
		streamVal = new HashMap<String, String>();
		streamVal.clear();
	}

	public void addVal(String key, String value) {
		streamVal.put(key, value);
	}

	public void print() {
		Set<Entry<String, String>> sets = streamVal.entrySet();
		for (Entry<String, String> entry : sets) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
	}
}

class Feed {
	private Map<String, String> feedVal;

	public Feed() {
		feedVal = new HashMap<String, String>();
		feedVal.clear();
	}

	public void addVal(String key, String value) {
		feedVal.put(key, value);
	}

	public void print() {
		Set<Entry<String, String>> sets = feedVal.entrySet();
		for (Entry<String, String> entry : sets) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
	}
}

class FfCfg {
	private Common commonCfg;
	private Map<String, Feed> feeds;
	private Map<String, Stream> streams;

	public FfCfg() {
		commonCfg = new Common();
		feeds = new HashMap<String, Feed>();
		streams = new HashMap<String, Stream>();
	}

	public void addCommon(String key, String val) {
		commonCfg.put(key, val);
	}

	public void addFeed(String name, Feed feed) {
		feeds.put(name, feed);
	}

	public void addStream(String name, Stream stream) {
		streams.put(name, stream);
	}

	public void printCfg() {
		System.out.println("====================Common=======================");
		commonCfg.print();

		System.out.println("====================Feed=========================");
		Set<Entry<String, Feed>> feedSets = feeds.entrySet();
		for (Entry<String, Feed> entry : feedSets) {
			System.out.println(entry.getKey());
			entry.getValue().print();
			System.out.println();
		}

		System.out.println("====================Stream=======================");
		Set<Entry<String, Stream>> streamSets = streams.entrySet();
		for (Entry<String, Stream> entry : streamSets) {
			System.out.println(entry.getKey());
			entry.getValue().print();
			System.out.println();
		}
	}
}

public class ParserCfg {
	private String ffCfgPath;
	private File ffCfgFile;
	private FfCfg ffservercfg;

	private State commonState;
	private State feedState;
	private State streamState;
	private State state;

	public ParserCfg(String path) {
		commonState = new CommonState(this);
		feedState = new FeedState(this);
		streamState = new StreamState(this);
		state = commonState;

		ffservercfg = new FfCfg();

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

	public State getCommonState() {
		return commonState;
	}

	public State getFeedState() {
		return feedState;
	}

	public State getStreamState() {
		return streamState;
	}

	public void setState(State st) {
		state = st;
	}

	public void addCommon(String key, String val) {
		ffservercfg.addCommon(key, val);
	}

	public void addFeed(String name, Feed feed) {
		ffservercfg.addFeed(name, feed);
	}

	public void addStream(String name, Stream stream) {
		ffservercfg.addStream(name, stream);
	}

	public void classify(String line) {
		state.classify(line);
	}

	public void parse() {
		ffCfgFile = new File(ffCfgPath);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(ffCfgFile));
			String line = null;

			while ((line = reader.readLine()) != null) {
				// skip comment line and space line
				if (!line.startsWith(String.valueOf("#")) && !line.isEmpty())
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
				"/Users/sijiewang/Projects/StreamConfigure/ffserver.conf");
		parser.parse();
		parser.printCfg();
	}
}
