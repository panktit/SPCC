import java.util.*;
import java.io.*;
class Ins {
	String label;
	String opcode;
	String op;

	public Ins(String label, String opcode, String op) {
		this.label = label;
		this.opcode = opcode;
		this.op = op;
	}

	public String toString() {
		if(this.label==null) {
			if(this.op==null)
				return "\t"+this.opcode;
			else
				return "\t"+this.opcode+"\t"+this.op;
		}
		else {
			if(this.op==null)
			return this.label + "\t" + this.opcode;
		else
			return this.label + "\t" + this.opcode + "\t" + this.op;
		}	
	}
}

class MacroName {
	int mntp,mdtp;
	String name;
	public MacroName(int mntp,String name,int mdtp) {
		this.mntp=mntp;
		this.name=name;
		this.mdtp=mdtp;
	}
	public String toString() {
		return this.mntp+"\t"+this.name+"\t\t"+this.mdtp;
	}
}

class MacroDefinition {
	int mdtp;
	String ins;
	public MacroDefinition(int mdtp,String ins) {
		this.mdtp=mdtp;
		this.ins=ins;
	}
	public String toString() {
		if(this.ins.indexOf("null")!=-1)
			return this.mdtp+"\t"+this.ins.substring(0,ins.indexOf("null"));
		else
			return this.mdtp+"\t"+this.ins;
	}
}
class Argument {
	int id;
	String arg;
	String value;
	public Argument(int id,String arg,String value) {
		this.id=id;
		this.arg=arg;
		this.value=value;
	}
	public String toString() {
		if(this.value==null)
			return this.id+"\t"+this.arg;
		else
			return this.id+"\t"+this.arg+"\t\t"+this.value;
	}
}
class MacroPreprocessor {	
	static ArrayList<Ins> input;
	static ArrayList<MacroName> mnt;
	static ArrayList<MacroDefinition> mdt;
	static ArrayList<Argument> ala;
	static ArrayList<Ins> ic;
	static ArrayList<Ins> op;
	static int mntc=1;
	static int mdtc=1;
	static int id=0;

	public static void main(String[] args) throws IOException {
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter name of input file :");
		String file=sc.next();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String str;
		System.out.println("\nInput File :\n");
		while ((str = br.readLine()) != null)
			System.out.println(str);

		input=new ArrayList<>();
		mnt=new ArrayList<>();
		mdt=new ArrayList<>();
		ala=new ArrayList<>();
		ic=new ArrayList<>();
		op=new ArrayList<>();

		Ins next = new Ins(null, null, null);
		// READING FILE
		BufferedReader br1 = new BufferedReader(new FileReader(file));
		String ip;

		while ((ip = br1.readLine()) != null) {
			String[] tokens = ip.trim().split("\\s+");

			// CLASSIFYING INSTRUCTION INTO LABEL,OPCODE,OPERANDS
			if (tokens.length == 1)
				next = new Ins(null, tokens[0], null);
			else if (tokens.length == 2)
				next=new Ins(null,tokens[0],tokens[1]);
			else if (tokens.length == 3)
				next=new Ins(tokens[0],tokens[1],tokens[2]);
			input.add(next);
		}
		// System.out.println("\nInput ArrayList :");
		// for(Ins i:input)
		// 	System.out.println(i);
		System.out.println("\n\n------ PASS 1 BEGINS ------");
		for(int i=0;i<input.size();i++) {
			// MACRO DEFINITION
			if(input.get(i).opcode.equalsIgnoreCase("MACRO")) {
				// ADDING ENTRY IN MNT
				mnt.add(new MacroName(mntc++,input.get(i+1).opcode,mdtc));
				// GO TO NEXT INSTRUCTION
				i++;
				// PREPARING ALA & ADDING PROTOTYPE TO MDT		
				String arguments[]=input.get(i).op.trim().split("\\s+|,");
				for(String s : arguments)
					ala.add(new Argument(id++,s,null));
				mdt.add(new MacroDefinition(mdtc++,input.get(i).opcode+"\t"+input.get(i).op));
				i++;				
				// ADDING ENTRIES IN MDT
				while(!input.get(i).opcode.equalsIgnoreCase("MEND")) {
					String temp="";
					String operands[]=input.get(i).op.trim().split("\\s+|,");
					for(String op:operands) {
						for(Argument a : ala) {
							if(op.equalsIgnoreCase(a.arg)) {
								op="#"+String.valueOf(a.id);
								break;
							}
							
						}
						temp+=","+op;
					}
					input.get(i).op=temp.substring(1);
					mdt.add(new MacroDefinition(mdtc++,input.get(i).opcode+"\t"+input.get(i).op));
					i++;
				}
				// ADD MEND
				mdt.add(new MacroDefinition(mdtc++,input.get(i).opcode+"\t"+input.get(i).op));
			}
			// IF NOT A MACRO DEFINITION, ADD TO INTERMEDIATE CODE FILE AS IT IS
			else {
				ic.add(input.get(i));
			}
		}
		// PRINTING PASS 1 OUTPUT
		System.out.println("\nMacro Name Table : \n\nMNTC\tMACRO NAME\tMDTC");
		for(MacroName mn : mnt) 
			System.out.println(mn);
		System.out.println("\nArgument List Array : \n\nID\tArgument");
		for(Argument a:ala) 
			System.out.println(a);
		System.out.println("\nMacro Definition Table : \n\nMDTC\tCard");
		for(MacroDefinition md : mdt) 
			System.out.println(md);
		System.out.println("\nIntermediate Code : \n");
		for(Ins i : ic) 
			System.out.println(i);
		System.out.println("\n\n------ PASS 1 ENDS ------");
		System.out.println("\n\n------ PASS 2 BEGINS ------");
		for(Ins ip1 : ic) {
			int pointer=-1;
			// SEARCH MNT
			for(MacroName mn : mnt) {
				if(ip1.opcode.equalsIgnoreCase(mn.name)) {
					pointer=mn.mdtp;
					break;
				}				
			}
			// MACRO CALL
			if(pointer!=-1) {
				// SET UP ALA
				String prototype=mdt.get(pointer-1).ins; // Because in ArrayList, insertion starts from 0 itself
				String prototype_tokens[]=prototype.split("\\s+|,");
				String input_tokens[]=ip1.op.split("\\s+|,");
				// CHECK FOR PROPER CALL
				if(input_tokens.length!=prototype_tokens.length-1) {
					System.out.println("Error in calling function!");
				}
				else {
					for(int i=1;i<prototype_tokens.length;i++) {
						for(Argument a : ala) {
							if(a.arg.equalsIgnoreCase(prototype_tokens[i])) {
								a.value=input_tokens[i-1];
								break;								
							}
						}
					}
				}
				// SUBSTITUTE ACTUAL PARAMETERS FROM ALA
				String defin=mdt.get(pointer).ins; // Getting the first instruction after prototype
				String tokens[]=defin.split("\\s+|,");
				String opcode=tokens[0];
				do  {
					String operands="";
					for(int i=1;i<tokens.length;i++) {
						if(tokens[i].charAt(0)=='#') {
							id=Integer.parseInt(tokens[i].substring(1));
							for(Argument a : ala) {
								if(id==a.id) {
									tokens[i]=a.value;
									break;
								}
							}					
						}
						operands+=tokens[i]+",";				
					}
					operands=operands.substring(0,operands.length()-1);
					op.add(new Ins(ip1.label,opcode, operands));
					defin=mdt.get(++pointer).ins;
					tokens=defin.split("\\s+|,");
					opcode=tokens[0];
				} while(!opcode.equalsIgnoreCase("MEND"));
			}
			// IF NOT A MACRO CALL, ADD TO OUTPUT FILE AS IT IS
			else {
				op.add(ip1);
			}
		}
		// PRINTING PASS 2 OUTPUT
		System.out.println("\nMacro Definition Table : \n\nMDTC\tCard");
		for(MacroDefinition md : mdt) 
			System.out.println(md);
		System.out.println("\nArgument List Array : \n\nID\tArgument\tValue");
		for(Argument a:ala) 
			System.out.println(a);
		System.out.println("\nFinal Output : \n");
		for(Ins i : op) 
			System.out.println(i);
		System.out.println("\n\n------ PASS 2 ENDS ------");
		sc.close();
		br.close();
		br1.close();
	}
}
