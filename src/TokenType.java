package item3;

public enum TokenType {
	INT,
	ID, QUESTION,
	TRUE, FALSE, NOT,
	PLUS, MINUS, TIMES, DIV,
	LT, GT, EQ, APOSTROPHE,
	L_PAREN, R_PAREN,
	DEFINE, LAMBDA, COND, QUOTE,
	CAR, CDR, CONS, 
	ATOM_Q, NULL_Q, EQ_Q;
	
	/**
	 * ch 가 특수 문자일 경우에만 토큰 검사
	 */
	static TokenType fromSpecialCharactor(char ch){
		switch ( ch ){
		case'(': {
			return L_PAREN;
		}
		case')': {
			return R_PAREN;
		}
		case'+': {
			return PLUS;
		}
		case'-': {
			return MINUS;
		}
		case'*': {
			return TIMES;
		}
		case'/': {
			return DIV;
		}
		case'<': {
			return LT;
		}
		case'>': {
			return GT;
		}
		case'=': {
			return EQ;
		}
		case'\'': {
			return APOSTROPHE;
		}	
			default:
				throw new IllegalArgumentException("unregistered char: "+ch);
		}
	}
}

