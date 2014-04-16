package net.lnmcc.streamserver;

public class StreamState implements State {
	private Parser parserCfg;
	private Stream stream;

	public StreamState(Parser pc) {
		parserCfg = pc;
	}

	private String getName(String line) {
		String sub = line.substring(line.indexOf('<') + 1, line.indexOf('>'));
		String[] str = sub.split(" ");
		String name = str[1].trim();
		return name;
	}

	@Override
	public void classify(String line) {
		// System.out.println("Stream " + line);

		if (line.startsWith(String.valueOf("<Stream"))) {
			stream = new Stream();
			String name = getName(line);
			parserCfg.addStream(name, stream);
		} else if (line.startsWith(String.valueOf("</Stream"))) {
			parserCfg.setState(parserCfg.getCommonState());
			stream = null;
		} else if (line.startsWith(String.valueOf("<Feed"))) {
			throw new IllegalStateException(
					String.valueOf("Error config format"));

		} else if (line.startsWith(String.valueOf("</Feed"))) {
			throw new IllegalStateException(
					String.valueOf("Error config format"));

		} else {
			String[] str = line.split("\\s+");
			if (str.length == 1) {
				String key = str[0].trim();
				stream.addItem(key, null);
			} else if (str.length == 2) {
				String key = str[0].trim();
				String value = str[1].trim();
				stream.addItem(key, value);
			} else if (str.length == 3) {
				String key = str[0].trim() + " " + str[1].trim();
				String value = str[2].trim();
				stream.addItem(key, value);
			}
		}
	}
}
