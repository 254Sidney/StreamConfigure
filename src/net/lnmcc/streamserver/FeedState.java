package net.lnmcc.streamserver;

public class FeedState implements State {
	private Parser parsercfg;
	private Feed feed;

	public FeedState(Parser pc) {
		parsercfg = pc;
	}

	private String getName(String line) {
		String sub = line.substring(line.indexOf('<') + 1, line.indexOf('>'));
		String[] str = sub.split(" ");
		String name = str[1].trim();
		return name;
	}

	@Override
	public void classify(String line) {

		if (line.startsWith(String.valueOf("<Feed"))) {
			String name = getName(line);
			feed = new Feed();
			parsercfg.addFeedSection(name, feed);
		} else if (line.startsWith(String.valueOf("</Feed"))) {
			parsercfg.setState(parsercfg.getCommonState());
			feed = null;
		} else if (line.startsWith(String.valueOf("<Stream"))) {
			throw new IllegalStateException(String.valueOf("Error config format"));

		} else if (line.startsWith(String.valueOf("</Stream"))) {
			throw new IllegalStateException(String.valueOf("Error config format"));

		} else {
			String[] str = line.split("\\s+");
			if (str.length == 1) {
				String key = str[0].trim();
				feed.addItem(key, null);
			} else if (str.length == 2) {
				String key = str[0].trim();
				String value = str[1].trim();
				feed.addItem(key, value);
			} else if (str.length == 3) {
				String key = str[0].trim() + " " + str[1].trim();
				String value = str[2].trim();
				feed.addItem(key, value);
			}
		}
	}
}
