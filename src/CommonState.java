public class CommonState implements State {
	private ParserCfg parserCfg;

	public CommonState(ParserCfg pc) {
		parserCfg = pc;
	}

	@Override
	public void classify(String line) {

		if (line.startsWith(String.valueOf("<Feed"))) {
			parserCfg.setState(parserCfg.getFeedState());
			parserCfg.classify(line);

		} else if (line.startsWith(String.valueOf("</Feed"))) {
			throw new IllegalStateException(
					String.valueOf("Error config format"));

		} else if (line.startsWith(String.valueOf("<Stream"))) {
			parserCfg.setState(parserCfg.getStreamState());
			parserCfg.classify(line);

		} else if (line.startsWith(String.valueOf("</Strem"))) {
			throw new IllegalStateException(
					String.valueOf("Error config format"));

		} else {
			String[] str = line.split("\\s+");
			String str0 = str[0].trim();
			String str1 = str[1].trim();
			parserCfg.addCommon(str0, str1);
		}
	}
}
