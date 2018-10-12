package item3;

public class IdNode implements ValueNode{
	
	String idString;
	
	public IdNode(String text) {
		idString = text;
	}
	
	@Override
	public String toString() {
		return "ID: " + idString;
	}
	
	@Override
	public boolean equals(Object obj) {
		IdNode node = (IdNode)obj;
		if(idString.equals(node.idString)) {
			return true;
		}
		else
			return false;
	}
	public String getId() {
		return idString;
	}

}
