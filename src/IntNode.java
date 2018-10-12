package item3;

public class IntNode implements ValueNode{
	private Integer value;
	
	@Override
	public String toString() {
		return "INT : "+value;
	}
	
	public IntNode(String text) {
		this.value = new Integer(text);
	}
	
	@Override
	public boolean equals(Object obj) {
		IntNode node = (IntNode)obj;
		if(value.equals(node.value)) {
			return true;
		}
		else
			return false;
	}
	public Integer getValue() {
		return value;
	}
	
}