package item3;


/**
 * 
 * @author 201602042
 *
 */
class ScanContext {
	private final CharStream input;
	private StringBuilder builder;
	
	ScanContext(String s){
		this.input = CharStream.from(s);
		this.builder = new StringBuilder();
	}
	
	CharStream getCharStream() {
		return input;
	}
	
	String getLexime() {
		String str = builder.toString();
		builder.setLength(0);
		return str;
	}
	
	void append(char ch) {
		builder.append(ch);
	}
}
