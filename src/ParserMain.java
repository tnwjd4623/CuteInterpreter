package item3;

import java.util.Scanner;

/**
 * 
 * @author 201602042_�̼���
 *
 */
public class ParserMain {
	private static Scanner scan;

	public static final void main(String... args) throws Exception{
		scan = new Scanner(System.in);
		String s;
		
		while(true) {
			System.out.print("> ");
			s = scan.nextLine();
			System.out.print("$");	
			CuteParser cuteParser = new CuteParser(s);
			NodePrinter.getPrinter(System.out).prettyPrint(cuteParser.parseExpr());
		}
	}

}
