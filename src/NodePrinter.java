package item3;

import java.io.PrintStream;
import java.util.StringTokenizer;
/**
 * 
 * @author 201602042_ÀÌ¼öÁ¤
 *
 */
public class NodePrinter {
   private PrintStream ps;
   
   public static NodePrinter getPrinter(PrintStream ps) {
      return new NodePrinter(ps);
   }
   
   private NodePrinter(PrintStream ps) {
      this.ps = ps;
   }
   
   private void printNode(ListNode listNode) {
      if(listNode.equals(ListNode.EMPTYLIST)) {
         ps.print("( )");
         return;
      }
      
      if(listNode.equals(ListNode.ENDLIST)) {
         return;
      }
      
      printNode(listNode.car());
      if(listNode.cdr().equals(ListNode.EMPTYLIST)){
         ps.print(" ");
      }
      
      printNode(listNode.cdr());   
   }
   
   private void printNode(QuoteNode quoteNode) {
      if(quoteNode.nodeInside() == null)
         return;
         
      ps.print("'");
      printNode(quoteNode.nodeInside());
      }   
      
   
   /**
    * 201602042
    */
   private void printNode(Node node) {
      if(node == null)
         return;
      
      if(node instanceof ListNode) {
         ps.print(" (");
         printNode((ListNode)node);
         ps.print(" )");
      }
      
      else if(node instanceof QuoteNode) {
         printNode((QuoteNode)node);
      }
      else if(node instanceof BooleanNode) {
         String temp = node.toString();
         StringTokenizer st = new StringTokenizer(temp, " ");
         ps.print(" " + st.nextToken());
      }
      else if(node instanceof IdNode) {
         String temp = node.toString();
         StringTokenizer st = new StringTokenizer(temp, " ");
         st.nextToken();   
         
         ps.print(" "+st.nextToken());
      
      }
      else if(node instanceof BinaryNode) {
         String temp = node.toString();
         StringTokenizer st = new StringTokenizer(temp, " ");
      
         
         ps.print(" " + st.nextToken());
      }
      else if(node instanceof FunctionNode){
         String temp = node.toString();
         StringTokenizer st = new StringTokenizer(temp, " ");
         
         ps.print(" "+ st.nextToken());
      }
      else {
         String temp = node.toString();
         StringTokenizer st = new StringTokenizer(temp, " ");
         st.nextToken();
         st.nextToken();
      
         ps.print(" "+ st.nextToken());
      }   
      
   
   }
   
   public void prettyPrint(Node node) {
      printNode(node);
   }

}
