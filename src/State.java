package item3;

import static item3.TokenType.FALSE;
import static item3.TokenType.INT;
import static item3.TokenType.TRUE;
import static item3.TransitionOutput.GOTO_ACCEPT_ID;
import static item3.TransitionOutput.GOTO_ACCEPT_INT;
import static item3.TransitionOutput.GOTO_ACCEPT_TF;
import static item3.TransitionOutput.GOTO_EOS;
import static item3.TransitionOutput.GOTO_FAILED;
import static item3.TransitionOutput.GOTO_MATCHED;
import static item3.TransitionOutput.GOTO_SIGN;
import static item3.TransitionOutput.GOTO_START;
enum State {
	/**
	 * 초기 상태
	 */
	START {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				/**
				 * 문자 = ID 상태로 전이
				 */
				case LETTER:
					context.append(v);
					return GOTO_ACCEPT_ID;
				/**
				 * 숫자 = INT 상태로 전이
				 */
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_INT;
					
				/**
				 * 특수문자 (<>()*+-.....) 검사 
				 * #일 경우는 따로 True, False 검사
				 */
				case SPECIAL_CHAR:
					if(v=='#'){
						context.append(v);
						return GOTO_ACCEPT_TF;
					}			
					else if(v=='+'){
						context.append(v);
						context.getCharStream().pushBack(v);
						return GOTO_SIGN;
					}
					else if(v=='-'){
						context.append(v);
						context.getCharStream().pushBack(v);
						return GOTO_SIGN;
					}
					else if(v=='<'||v=='>'||v=='('||v==')'||v=='*'||v=='/'||v=='='||v=='\''){
						context.append(v);
						return GOTO_MATCHED(TokenType.fromSpecialCharactor(v), context.getLexime());
					}
					else {
						return GOTO_FAILED;
					}
				/**
				 * 공백 처리 (모든 토큰은 공백으로 구분되어져 있음)
				 */
				case WS:
					return GOTO_START;
					
				/**
				 * stream 끝날 때
				 */
				case END_OF_STREAM:
					return GOTO_EOS;
				default:
					throw new AssertionError();
			}
			
		}
	},
	/**
	 * True / False 검사 
	 */
	ACCEPT_TF {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch(ch.type()){
			/**
			 * T 혹은 F일 때만 상태전이, 그 외에는 모두 실패 상황
			 */
				case LETTER:
					if(v=='T'){
						context.append(v);
						return GOTO_MATCHED(TRUE, context.getLexime());
					}
					else if(v=='F'){
						context.append(v);
						return GOTO_MATCHED(FALSE, context.getLexime());
					}
					else{
						return GOTO_FAILED;
					}
				case DIGIT:
					return GOTO_FAILED;
				case SPECIAL_CHAR:
					return GOTO_FAILED;
				case WS:
					return GOTO_FAILED;
				case END_OF_STREAM:
					return GOTO_FAILED;
					
				default:throw new AssertionError();	
			}
		}
	},
	/**
	 * ID 검사, 
	 * ID가 Keyword 일 수도 있으니 Token.ofName으로 검사
	 */
	ACCEPT_ID {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_ID;
					/**
					 * ID다음에 ?표시가 나오면 Question, keyword인지 검사해야함
					 */
				case SPECIAL_CHAR:
					if(v=='?'){
						context.append(v);
						return GOTO_MATCHED(Token.ofName(context.getLexime()));
					}
					else
						return GOTO_FAILED;
				case WS:
				case END_OF_STREAM:
					return GOTO_MATCHED(Token.ofName(context.getLexime()));
				
				default:
					throw new AssertionError();
			}
		}
	},
	/**
	 * INT상태 
	 */
	ACCEPT_INT {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			switch ( ch.type() ) {
				case LETTER:
					return GOTO_FAILED;
				case DIGIT:
					context.append(ch.value());
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR:
					return GOTO_FAILED;
				case WS:
				case END_OF_STREAM:
					return GOTO_MATCHED(INT, context.getLexime());
				default:
					throw new AssertionError();
			}
		}
	},
	/**
	 * Sign을 받았을 때 상태, 일단 Cache에 전 글자를 붙여놓고
	 * old value, new value를 나누어서 다음 글자를 고려하면서 검사
	 */
	SIGN {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char old_v = ch.value();
			ch = context.getCharStream().nextChar();
			char new_v = ch.value();
			
			switch ( ch.type() ) {
				case LETTER:
					return GOTO_FAILED;
				case DIGIT:
					context.append(new_v);
					return GOTO_ACCEPT_INT;
				case WS: 
					return GOTO_MATCHED(TokenType.fromSpecialCharactor(old_v), context.getLexime());
				case SPECIAL_CHAR:
					return GOTO_FAILED;
				case END_OF_STREAM:
					return GOTO_FAILED;
				default:
					throw new AssertionError();
			}
		}
	},
	/**
	 * 최종 상태 (ACCEPT)
	 */
	MATCHED {
		@Override
		public TransitionOutput transit(ScanContext context) {
			throw new IllegalStateException("at final state");
		}
	},
	/**
	 * REJECT
	 */
	FAILED{
		@Override
		public TransitionOutput transit(ScanContext context) {
			throw new IllegalStateException("at final state");
		}
	},
	EOS {
		@Override
		public TransitionOutput transit(ScanContext context) {
			return GOTO_EOS;
		}
	};
	
	abstract TransitionOutput transit(ScanContext context);
}
