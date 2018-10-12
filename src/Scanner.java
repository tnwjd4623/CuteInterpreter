package item3;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
/**
 * 
 * @author 201602042
 *
 */
public class Scanner {
    // return tokens as an Iterator
    public static Iterator<Token> scan(String s) {
        ScanContext context = new ScanContext(s);
        return new TokenIterator(context);
    }

    // return tokens as a Stream 
    public static Stream<Token> stream(String s){
        Iterator<Token> tokens = scan(s);
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(tokens, Spliterator.ORDERED), false);
    }
}
