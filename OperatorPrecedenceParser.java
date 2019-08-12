import java.util.*;
class OperatorPrecedenceParser {
	static char precedenceTable[][] = {
		{'0','*','/','+','-','(',')','x','$'},
		{'*','>','>','>','>','<','>','<','>'},
		{'/','>','>','>','>','<','>','<','>'},
		{'+','<','<','>','>','<','>','<','>'},
		{'-','<','<','>','>','<','>','<','>'},
		{'(','<','<','<','<','<','=','<','E'},
		{')','>','>','>','>','E','>','E','>'},
		{'x','>','>','>','>','E','>','E','>'},
		{'$','<','<','<','<','<','E','<','A'}
	};
	static String productions[]={"(E)","E+E","E-E","E*E","E/E","x"};
	static Stack<Character> stack=new Stack<Character>();
	public static void main(String[] args) {
		System.out.println("\nGrammar : ");
		for(int i=0;i<productions.length;i++)
			System.out.println("E->"+productions[i]);
		System.out.println("\nPrecedence Table : ");	
		for(int i=0;i<precedenceTable.length;i++) {
			for(int j=0;j<precedenceTable[i].length;j++) 
				System.out.print(precedenceTable[i][j]+" ");
			System.out.println();
		}
		Scanner sc=new Scanner(System.in);		
		System.out.println("\nEnter the input string : ");		
		String input=sc.next();
		input=input+"$";
		char ipbuffer[]=input.toCharArray();		
		stack.push('$'); // INITIAL STACK TOP
		int i=0;
		while(i<input.length()) {
			System.out.println("Relation between "+stack.peek()+" & "+ipbuffer[i]+" is : "+getRelation(stack.peek(), ipbuffer[i]));
			char relation=getRelation(stack.peek(), ipbuffer[i]);
			if(relation=='<') {
				// SHIFT
				System.out.println("Perform Shifting!!");
				stack.push(relation);
				stack.push(ipbuffer[i++]);
				System.out.println(stack.toString());
			}
			else if(relation=='>') {
				// REDUCTION
				System.out.println("Perform Reduction!!");
				// GET HANDLE
				String handle=getHandle();
				if(productionExists(handle)) {
					System.out.println("Relation between "+stack.peek()+" & "+ipbuffer[i]+" is : "+getRelation(stack.peek(), ipbuffer[i]));
					char tempRelation=getRelation(stack.peek(),ipbuffer[i]);
					if(tempRelation=='<') {
						System.out.println("Perform Shifting in reduce!!");
						stack.push(tempRelation);
						stack.push('E');
						stack.push(ipbuffer[i++]);
						System.out.println(stack.toString());
					}
					else if(tempRelation=='>') {
						// REDUCTION AGAIN
						System.out.println("Perform Reduction in reduce!!");
						stack.push('E');
						String tempHandle=getHandle();
						if(productionExists(tempHandle)) {
							System.out.println("Relation between "+stack.peek()+" & "+ipbuffer[i]+" is : "+getRelation(stack.peek(), ipbuffer[i]));
							char c=getRelation(stack.peek(),ipbuffer[i]);
							if(c=='A') {
								System.out.println("\n--- String is valid!!! ---");
								break;
							}
							else if(c=='E') {
								System.out.println("\n--- String is invalid!!! ---");
								break;
							}
							stack.push('E');
							stack.push(ipbuffer[i++]);
							System.out.println(stack.toString());
						}
						else {
							System.out.println("Handle not found in any of the productions!!\n\n--- String is invalid!!! ---");
							break;
						}
					}
					else if(tempRelation=='=') {
						System.out.println("Perform Shifting for = in reduce!!");
						stack.push('E');
						stack.push(ipbuffer[i++]);
						System.out.println(stack.toString());
					}
					else if(tempRelation=='E') {
						System.out.println("\n--- String is invalid!!! ---");
						break;
					}
					else if(relation=='A') {
						System.out.println("\n--- String is valid!!! ---");
						break;
					}		
				}
				else {
					System.out.println("Handle not found in any of the productions!!\n\n--- String is invalid!!! ---");
					break;
				}
			}
			else if(relation=='=') {
				System.out.println("Perform Shifting for = !!");
				stack.push(ipbuffer[i++]);
				System.out.println(stack.toString());
			}
			else if(relation=='E') {
				System.out.println("\n--- String is invalid!! ---");
				break;
			}
			else if(relation=='A') {
				System.out.println("\n--- String is valid!!! ---");
				break;
			}
		}
	}
	static char getRelation(char tos,char fsib) {
		char relation=' ';		
		for(int i=1;i<9;i++) {
			for(int j=1;j<9;j++)
				if(tos==precedenceTable[i][0] && fsib==precedenceTable[0][j])
					relation=precedenceTable[i][j];
		}		
		return relation;
	}
	static String getHandle() {
		String handle="";
		while(stack.peek()!='<') // POP TILL FIRST '<'
			handle=handle+stack.pop();
		stack.pop();    // '<' POPPED 
		// REVERSE TO GET HANDLE
		StringBuilder rev=new StringBuilder(); 
        rev.append(handle); 
        rev = rev.reverse(); 
		System.out.println("Handle : "+rev);
		return rev.toString();
	}
	static boolean productionExists(String handle) {
		boolean productionExists=false;
		for(int j=0;j<productions.length;j++) {
			if(handle.equals(productions[j])) {	
				System.out.println("Production Found!!!");
				productionExists=true;
				break;
			}
		}
		return productionExists;
	}
}
