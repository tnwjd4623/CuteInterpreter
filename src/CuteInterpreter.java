package item3;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import item3.ListNode;
import item3.BinaryNode;
import item3.FunctionNode;


public class CuteInterpreter {
	private Map<String, Node> VARIABLES = new HashMap<>();
	private static Scanner scan;
	
	public void insertTable(String id, Node value) {
		
		VARIABLES.put(id, value);
	}
	public Node lookupTable (String id) {
		if(VARIABLES.containsKey(id))
			return VARIABLES.get(id);
		
		return BooleanNode.FALSE_NODE;
	}
	
	//lambda의 인자별로 변경 ex) ((lambda (x y) (+ x y)) 1 2 )
	//var : 변경시킬 인자 리스트 ex) x y
	//chan : 안에 넣을 값 리스트 ex) 1 2
	public ListNode funcChange(ListNode func, ListNode var, ListNode chan){
		if(var.car() == null) return func;
		else{
			String variable = ((IdNode)var.car()).getId();
			Node input = (Node)chan.car();
			func = change(func, variable, input);
			return funcChange(func, var.cdr(), chan.cdr());
		}
		//var과 chan을 재귀로 돌림
	}
	
	//실제로 var에 chan을 집어넣으며 변경시키는 부분
	public ListNode change(ListNode func, String var, Node chan){
		if(func.car() == null) return func;
		if(func.car() instanceof IdNode){
			//define으로 정의된 함수나 값이 아닌 경우
			if(lookupTable(((IdNode)func.car()).getId()) == BooleanNode.FALSE_NODE){
				if(((IdNode)func.car()).getId().equals(var)){//idnode의 값이 var인 경우 chan으로 교체
					if(chan instanceof IdNode){
						if(lookupTable(((IdNode)chan).getId()) == BooleanNode.FALSE_NODE)
							return ListNode.cons(chan, change(func.cdr(), var, chan));
						else{
							Node c_chan = lookupTable(((IdNode)chan).getId());
							return ListNode.cons(c_chan, change(func.cdr(), var, chan));
						}
					}
					else
						return ListNode.cons(chan, change(func.cdr(), var, chan));
				}
				else//다를 경우 그대로 둠
					return ListNode.cons(func.car(), change(func.cdr(), var, chan));
			}
			else{//define으로 정의가 된 함수나 값인 경우
				Node value = lookupTable(((IdNode)func.car()).getId());
				Node c_func = null;
				if(value instanceof ListNode){//define으로 정의된 것이 listnode일 경우
					if(((ListNode)value).car() instanceof FunctionNode){//value의 car가 functionNode일 경우
						FunctionNode lambda = (FunctionNode)(((ListNode)value).car());
						if(lambda.value == FunctionNode.FunctionType.LAMBDA){//그 값이 lambda일 경우
							c_func = ListNode.cons((ListNode)value, func.cdr());//lambda형식으로 식을 다시 만들어주는 과정
							c_func = runExper(c_func);//lambda식을 한번 실행시킴
							c_func = change((ListNode)c_func, var, chan);//실행시켜서 나온 값을 다시 원래 식에서 변경시키는 과정
							return (ListNode)c_func;
						}
					}
				}
				else{//리스트 노드가 아닌 경우
					c_func = runExper(value);
				}
				return ListNode.cons(c_func, change((ListNode)func.cdr(), var, chan));//실행 결과값을 넣어줌
			}
		}
		else if(func.car() instanceof ListNode){
			ListNode c_func = change((ListNode)func.car(), var, chan);//리스트의 안도 바꾸어줌
			return ListNode.cons(c_func, change((ListNode)func.cdr(), var, chan));
		}
		else{
			return ListNode.cons(func.car(), change(func.cdr(), var, chan));
		}
	}
	
	private void errorLog(String err) {
		System.out.println(err);
	}
	
	public Node runExper(Node rootExpr) {
		if(rootExpr == null)
			return null;
		
		if(rootExpr instanceof IdNode)
			return lookupTable(((IdNode) rootExpr).getId());
		else if(rootExpr instanceof IntNode)
			return rootExpr;
		else if(rootExpr instanceof BooleanNode)
			return rootExpr;
		else if(rootExpr instanceof ListNode)
			return runList((ListNode) rootExpr);
		else
			errorLog("run Expr error");
		return null;
	}
	private Node runList(ListNode list) {
		if(list.equals(ListNode.EMPTYLIST))
			return list;
		if(list.car() instanceof FunctionNode) {
			return runFunction((FunctionNode)list.car(), list.cdr());
		}
		if(list.car() instanceof BinaryNode) {
			return runBinary(list);
		}
		if(list.cdr().car() != null){//인자가 두개 이상인 경우
			if(list.car() instanceof IdNode){
				if(lookupTable(((IdNode)list.car()).getId()) == BooleanNode.FALSE_NODE)
					return list;
				else{ //ex) ( x 1 )과 같은 경우(x는 lambda로 define된 값)
					Node value = lookupTable(((IdNode)list.car()).getId());
					ListNode c_func = ListNode.cons(value, list.cdr());
					return runExper(c_func);
				}
			}
			else if(((ListNode)list.car()).car() instanceof FunctionNode){ // lambda = function
				FunctionNode f_node = (FunctionNode)((ListNode)list.car()).car();
				if(f_node.value == FunctionNode.FunctionType.LAMBDA){
					//lambda 식과 넣을 값이 같이 있는 경우
					//ex) ((lambda (x) (+ x 1))1)
					ListNode lambda = (ListNode)list.car();
					ListNode change_list = list.cdr();//변수 안에 넣을 값
					ListNode func_param = ListNode.EMPTYLIST; //함수가 파라미터로 들어오면
					
					if( change_list.car() instanceof IdNode) {
						if(lookupTable(((IdNode)change_list.car()).getId())!=BooleanNode.FALSE_NODE) {
							Node change_value = lookupTable(((IdNode)change_list.car()).getId());
							//함수를 인자로 부를 때
							if(change_value instanceof ListNode) {
								if(((ListNode)change_value).car() instanceof FunctionNode) {
									FunctionNode f_change_value = (FunctionNode)((ListNode)change_value).car();
									
									if(f_change_value.value == FunctionNode.FunctionType.LAMBDA) {
										func_param = (ListNode) change_value;
										change_list = change_list.cdr();
									}
								}
							}
						}
					}
					ListNode var = (ListNode)(lambda.cdr().car());//변수 종류
					ListNode func = (ListNode)lambda.cdr().cdr().car();//함수(ex)(+ x 1))
					
					if( func.car() instanceof IdNode) {
						if(func.cdr().car() instanceof IdNode) 
							return runList(ListNode.cons(func_param, change_list));
						
						else 
							return runList(ListNode.cons(func_param, func.cdr()));
					}
					
					func = funcChange(func, var, change_list);
					
					
					if(func.car() instanceof FunctionNode)
						return runExper(func);
					else if(func.car() instanceof BinaryNode){
						if (func.cdr().car() instanceof IdNode){
							if(lookupTable(((IdNode)func.cdr().car()).getId()) == BooleanNode.FALSE_NODE)
								return func;
							else
								return runExper(func);
						}
						else if(func.cdr().cdr().car() instanceof IdNode){
							if(lookupTable(((IdNode)func.cdr().cdr().car()).getId()) == BooleanNode.FALSE_NODE)
								return func;
							else
								return runExper(func);
						}
						else
							return runExper(func);
					}
					if(change_list.car() instanceof IntNode)
						return runExper(func);
					else
						return func;
					
				}
			}
		}
		return list;
	}
	private Node runFunction(FunctionNode operator, ListNode operand) {
		switch (operator.value) {
		
			case CAR:
				Node car = operand.car();
				
				if(operand.car() instanceof IdNode) {
					IdNode id = (IdNode) operand.car();
					Node value = lookupTable(id.getId());
					
					if(value instanceof QuoteNode) {
						ListNode list = ListNode.cons(value, ListNode.EMPTYLIST);
						car = runFunction(operator, list);
					}
					else
						car = runFunction(operator, (ListNode)value);
				}
				// QuoteNode라면 nodeInside 반환
				else if(operand.car() instanceof QuoteNode) {
					operand = (ListNode) runQuote(operand);	
					car = operand.car();
				}
				
				// Function이 중첩될 때
				else if(((ListNode) operand.car()).car() instanceof FunctionNode) {
					car = runFunction((FunctionNode)((ListNode) operand.car()).car(), 
							((ListNode) operand.car()).cdr());
					ListNode l = ListNode.cons(car, ListNode.EMPTYLIST);
					car = runFunction(operator, l);
				
				}

				return car;
			
			//CAR과 동일
			case CDR:
				Node cdr = operand.cdr();
				
				if(operand.car() instanceof IdNode) {
					IdNode id = (IdNode) operand.car();
					Node value = lookupTable(id.getId());
					
					if(value instanceof QuoteNode){
						ListNode list = ListNode.cons(value, ListNode.EMPTYLIST);

						cdr = runFunction(operator, list);
					}
					else
						cdr = runFunction(operator, (ListNode)value);
				}
				else if(operand.car() instanceof QuoteNode) {
					operand = (ListNode) runQuote(operand);
					QuoteNode quote = new QuoteNode(operand.cdr());
					return quote;
				}
				else if(((ListNode) operand.car()).car() instanceof FunctionNode) {
					cdr = runFunction((FunctionNode)((ListNode) operand.car()).car(), 
							((ListNode) operand.car()).cdr());
					ListNode l = ListNode.cons(cdr, ListNode.EMPTYLIST);
					cdr = runFunction(operator,l);				
				}
				
				return cdr;
				
			case CONS:
				//입력 값의 car 과 cdr
				Node head = operand.car();
				ListNode tail = operand.cdr();
				
				
				if(operand.car() instanceof IdNode ) {
					IdNode id = (IdNode) operand.car();
					Node value = lookupTable(id.getId());
					
					if(value instanceof QuoteNode) {
						ListNode list = ListNode.cons(value, ListNode.EMPTYLIST);
						head = runQuote(list);
					}
					else
						head = value;
				}
				//입력 값이 QuoteNode라면 각각 NodeInside 값으로 바꿔줌
				else if(operand.car() instanceof QuoteNode) {
					head =  runQuote((ListNode) operand);
				}
				else if(operand.car() instanceof ListNode) {
					if(((ListNode) operand.car()).car() instanceof FunctionNode)
					head = runFunction((FunctionNode) ((ListNode) operand.car()).car(),
							((ListNode) operand.car()).cdr());
					//return head;
				}
				
				
				if(operand.cdr().car() instanceof IdNode) {
					IdNode id = (IdNode) operand.cdr().car();
					Node value = lookupTable(id.getId());
					ListNode list = ListNode.cons(value, ListNode.EMPTYLIST);
					
					if(value instanceof QuoteNode) 
						tail = (ListNode) runQuote(list);
						
					else 
						tail = list;
				}
				else if(operand.cdr().car() instanceof QuoteNode) {
					tail = (ListNode) runQuote(tail);
							
				}
				else if(operand.cdr().car() instanceof ListNode) {
					if(((ListNode) operand.cdr().car()).car() instanceof FunctionNode) 
						tail = ListNode.cons(runFunction((FunctionNode) ((ListNode) operand.cdr().car()).car(), 
								((ListNode)operand.cdr().car()).cdr()), ListNode.EMPTYLIST);
					
				}
				//반환은 QuoteNode로 
				ListNode newList = ListNode.cons(head, tail);
				QuoteNode result = new QuoteNode(newList);
				
				return result;
				
			case NULL_Q:
				if(operand.car() instanceof IdNode) {
					IdNode id = (IdNode) operand.car();
					Node value = lookupTable(id.getId());
					
					if(value instanceof QuoteNode) {
						ListNode list = ListNode.cons(value, ListNode.EMPTYLIST);
						return runFunction(operator, list);
					}
				}
				operand = (ListNode) runQuote(operand);
	
				if(operand.car()==null) {
					return BooleanNode.TRUE_NODE;
				}
				
				return BooleanNode.FALSE_NODE;
					
			case ATOM_Q:
				
				if(operand.car() instanceof IdNode) {
					IdNode id = (IdNode) operand.car();
					Node value = lookupTable(id.getId());
					
					if(value instanceof QuoteNode){
						ListNode list = ListNode.cons(value, ListNode.EMPTYLIST);
						return runFunction(operator, list);
					}
				}
				
				else if(operand.car() instanceof QuoteNode) {
					if(runQuote(operand) instanceof ListNode)
						return BooleanNode.FALSE_NODE;
				}
				
				return BooleanNode.TRUE_NODE;
				
			case EQ_Q:
	            Node firstNode;
	            Node secondNode;
	            
	            if(operand.car() instanceof IdNode) {
	               IdNode id = (IdNode) operand.car();
	               Node value = lookupTable(id.getId());
	               
	               if(value instanceof QuoteNode) {
	                  ListNode list = ListNode.cons(value, ListNode.EMPTYLIST);
	                  firstNode = runQuote(list);
	               }
	               else 
	                  firstNode = value;
	            }
	            /* ex) ( eq? 3 3 ) */
	            else if(operand.car() instanceof IntNode) {
	               IntNode id = (IntNode) operand.car();
	               firstNode = id;
	            }
	            
	            else {
	               firstNode = runQuote(operand);
	            }
	            
	            if(operand.cdr().car() instanceof IdNode) {
	               IdNode id = (IdNode) operand.cdr().car();
	               Node value = lookupTable(id.getId());
	               
	               if(value instanceof QuoteNode) {
	                  ListNode list = ListNode.cons(value, ListNode.EMPTYLIST);
	                  secondNode = runQuote(list);
	               }
	               else 
	                  secondNode = value;
	            }
	            else if(operand.cdr().car() instanceof IntNode) {
	               IntNode id= (IntNode) operand.cdr().car();
	               secondNode= id;
	            }
	            else {
	               secondNode = runQuote(operand.cdr());
	            }
	            
	            if(firstNode instanceof ListNode && secondNode instanceof ListNode) {
	               ListNode f = (ListNode)firstNode;
	               ListNode s = (ListNode)secondNode;
	               
	               while(f.cdr()!=null && s.cdr()!=null) {
	                  if(!(f.car().equals(s.car())))
	                     return BooleanNode.FALSE_NODE;
	                  
	                  f = f.cdr();
	                  s = s.cdr();
	               }
	               
	               if(f.cdr()==null) {
	                  if(s.cdr()!=null)
	                     return BooleanNode.FALSE_NODE;
	               }
	               if(s.cdr()==null) {
	                  if(f.cdr()!=null)
	                     return BooleanNode.FALSE_NODE;
	               }
	               return BooleanNode.TRUE_NODE;
	            }
	            else if(firstNode instanceof IdNode) {
	                     if( secondNode instanceof IdNode) {
	                        if(((IdNode)firstNode).equals(secondNode))
	                           return BooleanNode.TRUE_NODE;
	                     }
	                     if(!(secondNode instanceof IdNode))
	                        return BooleanNode.FALSE_NODE;
	                  }
	                  else if(secondNode instanceof IdNode) {
	                     if(!(firstNode instanceof IdNode))
	                        return BooleanNode.FALSE_NODE;
	                  }
	                  else if(firstNode instanceof ListNode) {
	                     if(!(secondNode instanceof ListNode))
	                        return BooleanNode.FALSE_NODE;
	                  }
	                  else if(secondNode instanceof ListNode) {
	                     if(!(firstNode instanceof ListNode))
	                        return BooleanNode.FALSE_NODE;
	                  }
	            else if(firstNode.equals(secondNode)) {
	               return BooleanNode.TRUE_NODE;
	            }
	            return BooleanNode.FALSE_NODE;
			
			case COND:
				
				Node headNode = operand.car();
				ListNode tailNode = operand.cdr();
				
				if(operand.car() instanceof IdNode) {
					IdNode id = (IdNode) operand.car();
					Node value = lookupTable(id.getId());
					
					if(value instanceof QuoteNode) {
						ListNode list = ListNode.cons(value, ListNode.EMPTYLIST);
						headNode = runQuote(list);
					}
					else
						headNode = value;
				}
				
				if(operand.cdr().car() instanceof IdNode) {
					IdNode id = (IdNode) operand.cdr().car();
					Node value = lookupTable(id.getId());
					
					if(value instanceof QuoteNode) {
						ListNode list = ListNode.cons(value, ListNode.EMPTYLIST);
						ListNode l = (ListNode) runQuote(list);
						
						tailNode = l;				
					}
					else
						tailNode = (ListNode)value;
					

				}
				
				if(headNode instanceof ListNode) {
					ListNode h = (ListNode) headNode;
					
					while(h.cdr()!=null || h.car() !=null ) {
						if(h.car() instanceof IdNode) {
							IdNode id = (IdNode) h.car();
							if(lookupTable(id.getId()) == BooleanNode.TRUE_NODE){
								return h.cdr().car();
							}
						}
						else if(h.car() instanceof BooleanNode) {
							if(h.car() == BooleanNode.TRUE_NODE){
								if(h.cdr().car() instanceof IdNode){
									Node re_value = lookupTable(((IdNode)h.cdr().car()).getId());
									return re_value;
								}
								else
									return h.cdr().car();	
							}
						}			
						h = h.cdr();
					}			
				}
				if(tailNode.car() instanceof BooleanNode) {
					if(tailNode.car() == BooleanNode.TRUE_NODE)
						return tailNode.cdr().car();
				}
				
			{
				FunctionNode f = new FunctionNode();
				f.setValue(TokenType.COND);

				return runFunction(f, tailNode);
			}

		case NOT:
				Node EQ_firstOperand = operand.car();
		
				if(EQ_firstOperand instanceof IdNode) {
					IdNode id = (IdNode) EQ_firstOperand;
					Node value = lookupTable(id.getId());
					
					if(value instanceof QuoteNode) {
						ListNode list = ListNode.cons(value, ListNode.EMPTYLIST);
						return runFunction(operator, list);
					}
					else if(value instanceof BooleanNode) {
						if( value == BooleanNode.FALSE_NODE)
							return BooleanNode.TRUE_NODE;
						else
							return BooleanNode.FALSE_NODE;
					}
				}
				if(EQ_firstOperand instanceof ListNode) {
					ListNode f = (ListNode) EQ_firstOperand;
					BooleanNode tmp;
					if(f.car() instanceof FunctionNode) {
						tmp = (BooleanNode) runFunction((FunctionNode)f.car(), f.cdr());
					}
					else 
						tmp = (BooleanNode) runBinary(f);
	
					if(tmp==BooleanNode.FALSE_NODE) {
						return BooleanNode.TRUE_NODE;
					}			
				}
				else if(EQ_firstOperand instanceof BooleanNode) {
					BooleanNode tmp = (BooleanNode) EQ_firstOperand;
					
					if(tmp==BooleanNode.FALSE_NODE) {
						return BooleanNode.TRUE_NODE;
					}
				}
									
				return BooleanNode.FALSE_NODE;
				
			case DEFINE:
				IdNode id = (IdNode) operand.car();
				Node value = operand.cdr().car();
				
				if( value instanceof IntNode) {
					insertTable(id.getId(), value);
					break;
				}
				else if(value instanceof QuoteNode) {
					insertTable(id.getId(), value);
					break;
				}
				
				else if(value instanceof BooleanNode){
					insertTable(id.getId(), value);
					break;
				}
				
				else if(((ListNode) value).car() instanceof FunctionNode ) {
					FunctionNode node = (FunctionNode)((ListNode) value).car();
					if(node.value != FunctionNode.FunctionType.LAMBDA)
						value = runFunction((FunctionNode)node, ((ListNode) value).cdr() );
				}
				
				else if(((ListNode) value).car() instanceof BinaryNode) {
					value = runBinary((ListNode) value);
				}
				
				insertTable(id.getId(), value);
				break;
			
			default:
				break;
		}
		return null;
	}
	private Node runBinary(ListNode list) {
		BinaryNode operator = (BinaryNode) list.car();
		ListNode operand = list.cdr();
	
		switch(operator.value) {
			case PLUS:
				Node firstOperand = operand.car();
				Node secondOperand = operand.cdr();
				IntNode result = new IntNode("0");
				int sum = 0;
			
				
				if(firstOperand instanceof IdNode) {
					Node n = lookupTable(((IdNode) firstOperand).getId());
					IntNode f = (IntNode) n;
					sum += f.getValue();
					
				}
				else if(firstOperand instanceof IntNode) {
					IntNode f = (IntNode) firstOperand;
					sum += f.getValue();
				}
				else if(firstOperand instanceof ListNode) {
					ListNode f = (ListNode) firstOperand;
					IntNode tmp = (IntNode) runBinary(f);
					
					sum += tmp.getValue();
				}
				
				
				if(secondOperand instanceof IntNode) {
					IntNode s = (IntNode) secondOperand;
					sum += s.getValue();
				}
				
				else if(secondOperand instanceof ListNode && secondOperand!=null) {
					ListNode s = (ListNode) secondOperand;
					IntNode tmp;
					
					if(s.car() instanceof IdNode) {
						IdNode id = (IdNode)s.car();
						Node value = lookupTable(id.getId());
						tmp = (IntNode)value;
						sum+=tmp.getValue();
					}
					else if(s.car() instanceof BinaryNode) {
						tmp = (IntNode) runBinary(s);
						sum += tmp.getValue();
					}
					else if(s.car() instanceof ListNode) {
						tmp = (IntNode) runBinary((ListNode) s.car());
						sum += tmp.getValue();
					}
					else {
						tmp = (IntNode) s.car();
						sum += tmp.getValue();
					}		
					
				}
						
				result = new IntNode(sum+"");
				return result;		
				
			case MINUS:
				Node MINUS_firstOperand = operand.car();
				Node MINUS_secondOperand = operand.cdr();
				IntNode MINUS_result = new IntNode("0");
				int sub = 0;
			
				
				if(MINUS_firstOperand instanceof IdNode) {
					Node n = lookupTable(((IdNode) MINUS_firstOperand).getId());
					IntNode f = (IntNode) n;
					sub += f.getValue();
				}
				else if(MINUS_firstOperand instanceof IntNode) {
					IntNode f = (IntNode) MINUS_firstOperand;
					sub += f.getValue();
				}
				else if(MINUS_firstOperand instanceof ListNode) {
					ListNode f = (ListNode) MINUS_firstOperand;
					IntNode tmp = (IntNode) runBinary(f);
					
					sub += tmp.getValue();
				}
				
			
				if(MINUS_secondOperand instanceof IntNode) {
					IntNode s = (IntNode) MINUS_secondOperand;
					sub -= s.getValue();
				}
				
				else if(MINUS_secondOperand instanceof ListNode && MINUS_secondOperand!=null) {
					ListNode s = (ListNode) MINUS_secondOperand;
					IntNode tmp;
					
					
					if(s.car() instanceof IdNode) {
						IdNode id = (IdNode)s.car();
						Node value = lookupTable(id.getId());
						tmp = (IntNode)value;
						sub -= tmp.getValue();
						
					}
					else if(s.car() instanceof BinaryNode) {
						tmp = (IntNode) runBinary(s);
						sub -= tmp.getValue();
					}
					else if(s.car() instanceof ListNode) {
						tmp = (IntNode) runBinary((ListNode) s.car());
						sub -= tmp.getValue();
					}
					else {
						tmp = (IntNode) s.car();
						sub -= tmp.getValue();
					}		
					
				}
						
				MINUS_result = new IntNode(sub+"");
				return MINUS_result;
				
			case DIV:
				Node DIV_firstOperand = operand.car();
				Node DIV_secondOperand = operand.cdr();
				IntNode DIV_result;
				int div = 1;
			
				
				if(DIV_firstOperand instanceof IdNode) {
					Node n = lookupTable(((IdNode) DIV_firstOperand).getId());
					IntNode f = (IntNode) n;
					div *= f.getValue();
				}
				else if(DIV_firstOperand instanceof IntNode) {
					IntNode f = (IntNode) DIV_firstOperand;
					div *= f.getValue();
				}
				else if(DIV_firstOperand instanceof ListNode) {
					ListNode f = (ListNode) DIV_firstOperand;
					IntNode tmp = (IntNode) runBinary(f);
					
					div *= tmp.getValue();
				}
				

				if(DIV_secondOperand instanceof IntNode) {
					IntNode s = (IntNode) DIV_secondOperand;
					div *= s.getValue();
				}
				
				else if(DIV_secondOperand instanceof ListNode && DIV_secondOperand!=null) {
					ListNode s = (ListNode) DIV_secondOperand;
					IntNode tmp;
					
					
					if(s.car() instanceof IdNode) {
						IdNode id = (IdNode) s.car();
						Node value = lookupTable(id.getId());
						tmp = (IntNode) value;
						div /= tmp.getValue();
						
					}
					else if(s.car() instanceof BinaryNode) {
						tmp = (IntNode) runBinary(s);
						div /= tmp.getValue();
					}
					else if(s.car() instanceof ListNode) {
						tmp = (IntNode) runBinary((ListNode) s.car());
						div /= tmp.getValue();
					}
					else {
						tmp = (IntNode) s.car();
						div /= tmp.getValue();
					}		
					
				}
						
				DIV_result = new IntNode(div+"");
				return DIV_result;
				
			case TIMES:
				Node TIMES_firstOperand = operand.car();
				Node TIMES_secondOperand = operand.cdr();
				IntNode TIMES_result = new IntNode("0");
				int times = 1;
			
				
				
				if(TIMES_firstOperand instanceof IdNode) {
					Node n = lookupTable(((IdNode) TIMES_firstOperand).getId());
					IntNode f = (IntNode) n;
					times *= f.getValue();
				}
				else if(TIMES_firstOperand instanceof IntNode) {
					IntNode f = (IntNode) TIMES_firstOperand;
					times *= f.getValue();
				}
				else if(TIMES_firstOperand instanceof ListNode) {
					ListNode f = (ListNode) TIMES_firstOperand;
					IntNode tmp = (IntNode) runBinary(f);
					
					times *= tmp.getValue();
				}
				
				
	
				if(TIMES_secondOperand instanceof IntNode) {
					IntNode s = (IntNode) TIMES_secondOperand;
					times *= s.getValue();
				}
				
				else if(TIMES_secondOperand instanceof ListNode && TIMES_secondOperand!=null) {
					ListNode s = (ListNode) TIMES_secondOperand;
					IntNode tmp;
					
					
					if(s.car() instanceof IdNode) {
						IdNode id = (IdNode) s.car();
						Node value = lookupTable(id.getId());
						tmp = (IntNode) value;
						times *= tmp.getValue();
					}
					else if(s.car() instanceof BinaryNode) {
						tmp = (IntNode) runBinary(s);
						times *= tmp.getValue();
					}
					else if(s.car() instanceof ListNode) {
						tmp = (IntNode) runBinary((ListNode) s.car());
						times *= tmp.getValue();
					}
					else {
						tmp = (IntNode) s.car();
						times *= tmp.getValue();
					}		
					
				}
						
				TIMES_result = new IntNode(times+"");
				return TIMES_result;
				
			case LT:
				Node LT_firstOperand = operand.car();
				Node LT_secondOperand = operand.cdr();
				IntNode first_tmp = new IntNode("0");
				IntNode second_tmp = new IntNode("0");			
				
				if(LT_firstOperand instanceof IdNode) {
					Node n = lookupTable(((IdNode) LT_firstOperand).getId());
					IntNode f = (IntNode) n;
					first_tmp = f;
				}
				else if(LT_firstOperand instanceof IntNode) {
					IntNode f = (IntNode) LT_firstOperand;
					first_tmp = f;
				}
				else if(LT_firstOperand instanceof ListNode) {
					ListNode f = (ListNode) LT_firstOperand;
					IntNode tmp = (IntNode) runBinary(f);
					
					first_tmp = tmp;
				}
				
		
				if(LT_secondOperand instanceof IntNode) {
					IntNode s = (IntNode) LT_secondOperand;
					second_tmp = s;
				}
				
				else if(LT_secondOperand instanceof ListNode && LT_secondOperand!=null) {
					ListNode s = (ListNode) LT_secondOperand;
					IntNode tmp;
					
					
					if(s.car() instanceof IdNode) {
						IdNode id = (IdNode)s.car();
						Node value = lookupTable(id.getId());
						tmp = (IntNode) value;
						second_tmp = tmp;
					}
					else if(s.car() instanceof BinaryNode) {
						tmp = (IntNode) runBinary(s);
						second_tmp = tmp;
					}
					else if(s.car() instanceof ListNode) {
						tmp = (IntNode) runBinary((ListNode) s.car());
						second_tmp = tmp;
					}
					else {
						tmp = (IntNode) s.car();
						second_tmp = tmp;
					}		
					
				}
						
				if( first_tmp.getValue() < second_tmp.getValue()) {
					return BooleanNode.TRUE_NODE;
				}
				
				return BooleanNode.FALSE_NODE;
				
			case GT:
				Node GT_firstOperand = operand.car();
				Node GT_secondOperand = operand.cdr();
				IntNode first_tmp1 = new IntNode("0");
				IntNode second_tmp1 = new IntNode("0");			
				
				
				if(GT_firstOperand instanceof IdNode) {
					Node n = lookupTable(((IdNode) GT_firstOperand).getId());
					IntNode f = (IntNode) n;
					first_tmp1 = f;
				}
				else if(GT_firstOperand instanceof IntNode) {
					IntNode f = (IntNode) GT_firstOperand;
					first_tmp1 = f;
				}
				else if(GT_firstOperand instanceof ListNode) {
					ListNode f = (ListNode) GT_firstOperand;
					IntNode tmp = (IntNode) runBinary(f);
					
					first_tmp1 = tmp;
				}
				
				
				if(GT_secondOperand instanceof IntNode) {
					IntNode s = (IntNode) GT_secondOperand;
					second_tmp1 = s;
				}
				
				else if(GT_secondOperand instanceof ListNode && GT_secondOperand!=null) {
					ListNode s = (ListNode) GT_secondOperand;
					IntNode tmp;
					
					
					if(s.car() instanceof IdNode) {
						IdNode id = (IdNode) s.car();
						Node value = lookupTable(id.getId());
						tmp = (IntNode) value;
						second_tmp1 = tmp;
					}
					else if(s.car() instanceof BinaryNode) {
						tmp = (IntNode) runBinary(s);
						second_tmp1 = tmp;
					}
					else if(s.car() instanceof ListNode) {
						tmp = (IntNode) runBinary((ListNode) s.car());
						second_tmp1 = tmp;
					}
					else {
						tmp = (IntNode) s.car();
						second_tmp1 = tmp;
					}		
					
				}
						
				if( first_tmp1.getValue() > second_tmp1.getValue()) {
					return BooleanNode.TRUE_NODE;
				}
				
				return BooleanNode.FALSE_NODE;
				
			case EQ:
				Node EQ_firstOperand = operand.car();
				Node EQ_secondOperand = operand.cdr();
				IntNode first_tmp3 = new IntNode("0");
				IntNode second_tmp3 = new IntNode("0");			
				
				
				if(EQ_firstOperand instanceof IdNode) {
					Node n = lookupTable(((IdNode) EQ_firstOperand).getId());
					IntNode f = (IntNode) n;
					first_tmp3 = f;
				}
				else if(EQ_firstOperand instanceof IntNode) {
					IntNode f = (IntNode) EQ_firstOperand;
					first_tmp3 = f;
				}
				else if(EQ_firstOperand instanceof ListNode) {
					ListNode f = (ListNode) EQ_firstOperand;
					IntNode tmp = (IntNode) runBinary(f);
					
					first_tmp3 = tmp;
				}
				
				
				if(EQ_secondOperand instanceof IntNode) {
					IntNode s = (IntNode) EQ_secondOperand;
					second_tmp3 = s;
				}
				
				else if(EQ_secondOperand instanceof ListNode && EQ_secondOperand!=null) {
					ListNode s = (ListNode) EQ_secondOperand;
					IntNode tmp;
					
					
					if(s.car() instanceof IdNode) {
						IdNode id = (IdNode) s.car();
						Node value = lookupTable(id.getId());
						tmp = (IntNode) value;
						second_tmp3 = tmp;
					}
					else if(s.car() instanceof BinaryNode) {
						tmp = (IntNode) runBinary(s);
						second_tmp3 = tmp;
					}
					else if(s.car() instanceof ListNode) {
						tmp = (IntNode) runBinary((ListNode) s.car());
						second_tmp3 = tmp;
					}
					else {
						tmp = (IntNode) s.car();
						second_tmp3 = tmp;
					}		
					
				}
						
				if( first_tmp3.getValue().equals(second_tmp3.getValue())) {
					return BooleanNode.TRUE_NODE;
				}
				
				return BooleanNode.FALSE_NODE;
				
			default:
				break;
		}
		return null;
	}
	
	private Node runQuote(ListNode node) {
		return ((QuoteNode)node.car()).nodeInside();
	}
	
	public static void main(String[] args) {
		scan = new Scanner(System.in);
		String s;
		CuteInterpreter i = new CuteInterpreter();
		
		while(true) {
			System.out.print("> ");
			s = scan.nextLine();
			System.out.print("$");	
			CuteParser cuteParser = new CuteParser(s);
			Node parseTree = cuteParser.parseExpr();
		
			Node resultNode = i.runExper(parseTree);
			NodePrinter.getPrinter(System.out).prettyPrint(resultNode);
			System.out.println("");
		}
	}

}
