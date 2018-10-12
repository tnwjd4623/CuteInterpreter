package item3;


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
/**
 * 
 * @author 201602042
 *
 */
class CharStream {
	private final Reader reader;
	private Character cache;
	
	
	static CharStream from(String s) {
		return new CharStream(new StringReader(s));
	}
	
	CharStream(StringReader stringReader) {
		this.reader = stringReader;
		this.cache = null;
	}
	
	Char nextChar() {
		if ( cache != null ) {
			char ch = cache;
			cache = null;
			
			return Char.of(ch);
		}
		else {
			try {
				int ch = reader.read();
				if ( ch == -1 ) {
					return Char.end();
				}
				else {
					return Char.of((char)ch);
				}
			}
			catch ( IOException e ) {
				throw new ScannerException("" + e);
			}
		}
	}
	
	void pushBack(char ch) {
		cache = ch;
	}
}
