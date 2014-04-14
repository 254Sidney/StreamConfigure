import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.html.HTMLEditorKit.Parser;

class Common {
	private Map<String, String> commonVal;

	public Common() {
		commonVal = new HashMap<String, String>();
		commonVal.clear();
	}
}

class Stream {
	private Map<String, String> streamVal;

	public Stream() {
		streamVal = new HashMap<String, String>();
		streamVal.clear();
	}
}

class Feed {
	private Map<String, String> feedVal;

	public Feed() {
		feedVal = new HashMap<String, String>();
		feedVal.clear();
	}
}

class FfCfg {
	private Common commonCfg;
	private List<Feed> feeds;
	private List<Stream> streams;

	public FfCfg() {
		commonCfg = new Common();
		feeds = new ArrayList<Feed>();
		streams = new ArrayList<Stream>();
	}
}

public class ParserCfg {
	private String ffCfgPath;
	private File ffCfgFile;

	public ParserCfg(String path) {
		if (path == null) {
			// use default
			ffCfgPath = String.valueOf("/etc/ffserver.conf");
		} else {
			ffCfgPath = path;
		}
	}

	public void parse() {
		ffCfgFile = new File(ffCfgPath);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(ffCfgFile));
			String line = null;

			while ((line = reader.readLine()) != null) {
				System.out.println(line);
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
		ParserCfg parser = new ParserCfg(null);
		parser.parse();
	}
}
