package item3;

import java.util.Iterator;

import item3.Token;
import item3.TokenType;
import item3.BinaryNode;
import item3.BooleanNode;
import item3.FunctionNode;
import item3.IdNode;
import item3.IntNode;
import item3.ListNode;
import item3.Node;
/**
 * 
 * @author 201602042
 *
 */
public class CuteParser {
	private Iterator<Token> tokens;
	private static Node END_OF_LIST = new Node(){};
	
	public CuteParser(String s) {
		tokens = Scanner.scan(s);	
	}
	private Token getNextToken() {
		if (!tokens.hasNext())
			return null;
		return tokens.next();
	}
	
	public Node parseExpr() {
		Token t = getNextToken();
		if (t == null) {
			System.out.println("No more token");
			return null;
		}
		TokenType tType = t.type();
		String tLexeme = t.lexme();
		
		switch (tType) {
		case ID:
			IdNode idNode = new IdNode(tLexeme);
			return idNode;
			
		case INT:
			IntNode intNode = new IntNode(tLexeme);
			if (tLexeme == null)
				System.out.println("???");
			
			return intNode;
			
		case DIV:
		case EQ:
		case MINUS:
		case GT:
		case PLUS:
		case TIMES:
		case LT:
			BinaryNode binaryNode = new BinaryNode();
			binaryNode.setValue(tType);
			return binaryNode;
		// FunctionNode 키워드가 FunctionNode에 해당
		case ATOM_Q:
		case CAR:
		case CDR:
		case COND:
		case CONS:
		case DEFINE:
		case EQ_Q:
		case LAMBDA:
		case NOT:
		case NULL_Q:
			FunctionNode functionNode = new FunctionNode();
			functionNode.setValue(tType);
			return functionNode;
			
		case FALSE:
			return BooleanNode.FALSE_NODE;
			
		case TRUE:
			return BooleanNode.TRUE_NODE;
			
		case L_PAREN:
			return parseExprList();
		
		case R_PAREN:
			return END_OF_LIST ;
		
		case APOSTROPHE:
			return new QuoteNode(parseExpr());
			
		case QUOTE:
			return new QuoteNode(parseExpr());
		default:
			// head의 next를 만들고 head를 반환하도록 작성
			System.out.println("Parsing Error!");
			return null;
		}
	}
	
	private ListNode parseExprList() {
		Node head = parseExpr();
		
		if(head == null)
			return null;
		
		if(head == END_OF_LIST)
			return ListNode.ENDLIST;
		
		ListNode tail = parseExprList();
			if(tail == null)
				return null;
			
		return ListNode.cons(head, tail);
	}
}
