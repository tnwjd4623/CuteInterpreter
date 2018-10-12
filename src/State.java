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
	 * �ʱ� ����
	 */
	START {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				/**
				 * ���� = ID ���·� ����
				 */
				case LETTER:
					context.append(v);
					return GOTO_ACCEPT_ID;
				/**
				 * ���� = INT ���·� ����
				 */
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_INT;
					
				/**
				 * Ư������ (<>()*+-.....) �˻� 
				 * #�� ���� ���� True, False �˻�
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
				 * ���� ó�� (��� ��ū�� �������� ���еǾ��� ����)
				 */
				case WS:
					return GOTO_START;
					
				/**
				 * stream ���� ��
				 */
				case END_OF_STREAM:
					return GOTO_EOS;
				default:
					throw new AssertionError();
			}
			
		}
	},
	/**
	 * True / False �˻� 
	 */
	ACCEPT_TF {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch(ch.type()){
			/**
			 * T Ȥ�� F�� ���� ��������, �� �ܿ��� ��� ���� ��Ȳ
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
	 * ID �˻�, 
	 * ID�� Keyword �� ���� ������ Token.ofName���� �˻�
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
					 * ID������ ?ǥ�ð� ������ Question, keyword���� �˻��ؾ���
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
	 * INT���� 
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
	 * Sign�� �޾��� �� ����, �ϴ� Cache�� �� ���ڸ� �ٿ�����
	 * old value, new value�� ����� ���� ���ڸ� ����ϸ鼭 �˻�
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
	 * ���� ���� (ACCEPT)
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
