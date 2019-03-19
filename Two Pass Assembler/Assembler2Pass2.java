import java.util.*;
import java.io.*;

class MachineOpcode {
	String opcode;
	int len;
	String hexcode;
	String type;

	public MachineOpcode(String opcode, int len, String hexcode, String type) {
		this.opcode = opcode;
		this.len = len;
		this.hexcode = hexcode;
		this.type = type;
	}

	public String toString() {
		return this.opcode + "\t" + this.len + "\t" + this.hexcode + "\t\t" + this.type;
	}
}

class Ins {
	String label;
	String opcode;
	String op1;
	String op2;

	public Ins(String label, String opcode, String op1, String op2) {
		this.label = label;
		this.opcode = opcode;
		this.op1 = op1;
		this.op2 = op2;
	}

	public String toString() {
		return this.label + "\t" + this.opcode + "\t" + this.op1 + "," + this.op2;
	}
}

class Symbol {
	String name, type;
	int id, length, value;

	public Symbol(int id, String name, int value, int length, String type) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.value = value;
		this.length = length;
	}

	public String toString() {
		return this.id + "\t" + this.name + "\t" + this.value + "\t" + this.length + "\t" + this.type;
	}
}

class Literal {
	String name, type;
	int id, length, value;

	public Literal(int id, String name, int value, int length, String type) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.value = value;
		this.length = length;
	}

	public String toString() {
		return this.id + "\t" + this.name + "\t" + this.value + "\t" + this.length + "\t" + this.type;
	}
}

class OutputIns {
	int lc;
	String opcode, op1, op2;

	public OutputIns(int lc, String opcode, String op1, String op2) {
		this.lc = lc;
		this.opcode = opcode;
		this.op1 = op1;
		this.op2 = op2;
	}

	public String toString() {
		if (opcode==null)
			return Integer.toString(this.lc);
		if (opcode.equalsIgnoreCase("LTORG"))
			return this.lc + "\t-------";
		if (opcode.equalsIgnoreCase("START") || opcode.equalsIgnoreCase("USING")) {
			if (op2 == null) {
				//this.lc = -1;
				return "\t" + this.opcode + "\t" + this.op1;
			} else {
				//this.lc = -1;
				return "\t" + this.opcode + "\t" + this.op1 + "," + this.op2;
			}
		}
		if (op1 == null)
			return this.lc + "\t" + this.opcode;
		if (op2 == null)
			return this.lc + "\t" + this.opcode + "\t" + this.op1;
		else
			return this.lc + "\t" + this.opcode + "\t" + this.op1 + "," + this.op2;
	}
}
class Register {
	int regNumber,value;
	public Register(int regNumber,int value) {
		this.regNumber=regNumber;
		this.value=value;
	}

	public String toString() {
		return this.regNumber+" -> "+this.value;
	}
}
class Assembler2Pass2 {
	static ArrayList<String> pot;
	static ArrayList<MachineOpcode> mot;
	static ArrayList<Symbol> st;
	static ArrayList<Literal> lt;
	static ArrayList<OutputIns> optp1;
	static ArrayList<OutputIns> optp2;
	static ArrayList<Register> rt;

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter input file name :");
		String file = sc.next();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String str;
		System.out.println("\nInput File :\n");
		while ((str = br.readLine()) != null)
			System.out.println(str);
		// INITIALIZE VARIABLES
		Ins next = new Ins(null, null, null, null);
		int lc = 0, sid = 0, lid = 0;
		Integer baseReg = 0;

		// INITIALIZE TABLES
		pot = new ArrayList<>();
		mot = new ArrayList<>();
		st = new ArrayList<>();
		lt = new ArrayList<>();
		optp1 = new ArrayList<>();
		optp2 = new ArrayList<>();
		rt=new ArrayList<>();

		// DEFINE MOT & POT
		Collections.addAll(pot, "START", "USING", "EQU", "LTORG", "DC", "DS", "END");

		mot.add(new MachineOpcode("A", 4, "5A", "RX"));
		mot.add(new MachineOpcode("L", 4, "58", "RX"));
		mot.add(new MachineOpcode("SR", 2, "1B", "RR"));
		mot.add(new MachineOpcode("ST", 4, "50", "RX"));
		mot.add(new MachineOpcode("C", 4, "59", "RX"));
		mot.add(new MachineOpcode("LA", 4, "41", "RX"));
		mot.add(new MachineOpcode("BNE", 4, "477", "RX"));
		mot.add(new MachineOpcode("BR", 2, "07F", "RR"));
		mot.add(new MachineOpcode("LR", 2, "18", "RR"));
		mot.add(new MachineOpcode("AR", 2, "1A", "RR"));

		BufferedWriter wrMOT = new BufferedWriter(new FileWriter("mot.txt"));
		BufferedWriter wrPOT = new BufferedWriter(new FileWriter("pot.txt"));
		BufferedWriter wrST = new BufferedWriter(new FileWriter("st.txt"));
		BufferedWriter wrLT = new BufferedWriter(new FileWriter("lt.txt"));
		BufferedWriter wrIC = new BufferedWriter(new FileWriter("outputPass1.txt"));
		BufferedWriter wrFC = new BufferedWriter(new FileWriter("outputPass2.txt"));

		System.out.println("\nPseudo Opcode Table :\n\nOpcode");
		for (String string : pot) {
			System.out.println(string);
			wrPOT.write(string);
			wrPOT.newLine();
		}

		System.out.println("\nMachine Opcode Table :\n\nOpcode\tLength\tHex Code\tType");
		for (MachineOpcode m : mot) {
			System.out.println(m);
			wrMOT.write(m.toString());
			wrMOT.newLine();
		}
		System.out.println("\n\n------ PASS 1 BEGINS ------");
		// READING FILE
		BufferedReader br1 = new BufferedReader(new FileReader(file));
		String ip;

		while ((ip = br1.readLine()) != null) {
			String[] tokens = ip.trim().split("\\s+");

			// CLASSIFYING INSTRUCTION INTO LABEL,OPCODE,OP1,OP2
			if (tokens.length == 1) {
				next = new Ins(null, tokens[0], null, null);
			} else if (tokens.length == 2) {
				String operands[] = tokens[1].trim().split(",", 2);
				if (operands.length == 2) {
					next = new Ins(null, tokens[0], operands[0], operands[1]);
				} else {
					next = new Ins(null, tokens[0], operands[0], null);
				}
				if (tokens[0].equalsIgnoreCase("DC") || tokens[0].equalsIgnoreCase("DS"))
					next = new Ins(null, tokens[0], tokens[1], null);
			} else if (tokens.length == 3) {
				String operands[] = tokens[2].trim().split(",", 2);
				if (operands.length == 2) {
					next = new Ins(tokens[0], tokens[1], operands[0], operands[1]);
				} else {
					next = new Ins(tokens[0], tokens[1], operands[0], null);
				}
				if (tokens[1].equalsIgnoreCase("DC") || tokens[1].equalsIgnoreCase("DS"))
					next = new Ins(tokens[0], tokens[1], tokens[2], null);
			}

			// GET TYPE OF INSTRUCTION & PROCESS
			switch (getType(next.opcode)) {
			case 0:
				System.out.println("***** Invalid Opcode!! *****");
				break;
			case 1:
				switch (next.opcode) {
				case "DC":
					// SEPERATE F'
					String operand = next.op1.substring(2, next.op1.length() - 1);
					// IF MULTIPLE VALUES
					String values[] = operand.split(",");
					// GET NUMBER OF LOCATIONS TO BE ASSIGNED
					int clen = 4 * values.length;
					st.add(new Symbol(++sid, next.label, lc, clen, "R"));
					optp1.add(new OutputIns(lc, next.opcode, next.op1, next.op2));
					lc += clen;
					break;
				case "DS":
					// SEPERATE F
					String size = next.op1.substring(0, next.op1.length() - 1);
					// GET NUMBER OF LOCATIONS TO BE ASSIGNED
					int slen = 4 * Integer.parseInt(size);
					st.add(new Symbol(++sid, next.label, lc, slen, "R"));
					optp1.add(new OutputIns(lc, next.opcode, next.op1, next.op2));
					lc += slen;
					break;
				case "START":
					// OPERAND OF START=LC
					lc = Integer.parseInt(next.op1);
					st.add(new Symbol(++sid, next.label, lc, 1, "R"));
					optp1.add(new OutputIns(lc, next.opcode, next.op1, next.op2));
					break;
				case "USING":
					optp1.add(new OutputIns(lc, next.opcode, next.op1, next.op2));
					baseReg = Integer.parseInt(next.op2);
					break;
				case "EQU":
					// IF * IS PRESENT, VALUE IS CURRENT LC VALUE
					if (next.op1.equals("*"))
						st.add(new Symbol(++sid, next.label, lc, 1, "A"));
					// ELSE VALUE IS OPERAND
					else
						st.add(new Symbol(++sid, next.label, Integer.parseInt(next.op1), 1, "A"));
					break;
				case "LTORG":
					optp1.add(new OutputIns(lc, next.opcode, next.op1, next.op2));
					// CHECK IF LITERALS ARE NOT ASSIGNED VALUE
					boolean flag2 = false;
					for (Literal l : lt) {
						if (l.value == -1) {
							flag2 = true;
						}
					}

					// IF NOT ASSIGNED, UPDATE LC AND ASSIGN VALUES
					if (flag2) {
						while (true) {
							if (lc % 8 == 0)
								break;
							lc++;
						}
						for (Literal l : lt) {
							if (l.value == -1) {
								l.value = lc;
								lc += l.length;
							}
						}
					}

					break;
				case "END":
					optp1.add(new OutputIns(lc, next.opcode, next.op1, next.op2));
					// CHECK IF LITERALS ARE NOT ASSIGNED VALUE
					boolean flag1 = false;
					for (Literal l : lt) {
						if (l.value == -1) {
							flag1 = true;
						}
					}

					// IF NOT ASSIGNED, UPDATE LC AND ASSIGN VALUES
					if (flag1) {
						while (true) {
							if (lc % 8 == 0)
								break;
							lc++;
						}
						for (Literal l : lt) {
							if (l.value == -1) {
								l.value = lc;
								lc += l.length;
							}
						}
					}
					break;
				}
				break;
			case 2:
				int mlen = 0;
				for (MachineOpcode m : mot) {
					if ((m.opcode).equalsIgnoreCase(next.opcode)) {
						mlen = m.len;
						break;
					}
				}

				// IF LITERAL IS PRESENT, ADD TO LITERAL TABLE
				if (next.op1.contains("="))
					lt.add(new Literal(++lid, next.op1.substring(1), -1, 4, "R"));
				if (next.op2 != null && next.op2.contains("="))
					lt.add(new Literal(++lid, next.op2.substring(1), -1, 4, "R"));

				// IF LABEL IS PRESENT, ADD TO SYMBOL TABLE
				if (next.label != null)
					st.add(new Symbol(++sid, next.label, lc, mlen, "R"));

				// ADDING VALUES FOR GENERATING INTERMEDIATE CODE
				optp1.add(new OutputIns(lc, next.opcode, next.op1, next.op2));

				// UPDATE LC
				lc += mlen;
				break;
			}
		}

		// WRITING OUTPUT TO FILES
		System.out.println("\nSymbol Table :\n\nID\tName\tValue\tLength\tR/A");
		for (Symbol s : st) {
			System.out.println(s);
			wrST.write(s.toString());
			wrST.newLine();
		}
		System.out.println("\nLiteral Table :\n\nID\tName\tValue\tLength\tR/A");
		for (Literal l : lt) {
			System.out.println(l);
			wrLT.write(l.toString());
			wrLT.newLine();
		}

		// PRINTING INTERMEDIATE CODE
		System.out.println("\nIntermediate Code :\n\nLC\tOpcode\tOp1,Op2\n");
		for (OutputIns op : optp1) {
			if (op.op1 == null && op.op2 == null) {
				System.out.println(op);
				wrIC.write(op.toString());
				wrIC.newLine();
				continue;
			}
			if (stContains(op.op1) != -1) {
				op.op1 = "ST#" + stContains(op.op1).toString();
			} else if (ltContains(op.op1.substring(1)) != -1) {
				op.op1 = "LT#" + ltContains(op.op1.substring(1)).toString();
			}
			if (op.op2 != null) {
				if (stContains(op.op2) != -1) {
					op.op2 = "ST#" + stContains(op.op2).toString();
				} else if (ltContains(op.op2.substring(1)) != -1) {
					op.op2 = "LT#" + ltContains(op.op2.substring(1)).toString();
				}
			}
			System.out.println(op);
			wrIC.write(op.toString());
			wrIC.newLine();
		}

		System.out.println("\nFinal LC value is : " + lc);
		System.out.println("\n\n------ PASS 1 ENDS ------");
		System.out.println("\n\n------ PASS 2 BEGINS ------");
		System.out.println("\nRegister Table ath end of Pass 2 :");
		for(Register r : rt) {
			System.out.println(r);
		}
		System.out.println("\nCode generated by Pass 2 :\n\nLC\tOpcode\tOp1,Op2\n");
		for (OutputIns op : optp1) {
			if (op.opcode.equalsIgnoreCase("USING")) {
				if(op.op1.equals("*"))
					rt.add(new Register(Integer.parseInt(op.op2),op.lc));
				else
					rt.add(new Register(Integer.parseInt(op.op2),Integer.parseInt(op.op1)));
			}

			else if(op.opcode.equals("START")) {
				continue;
			}

			else {
				if(op.opcode.equalsIgnoreCase("LTORG")) {
					optp2.add(new OutputIns(op.lc, op.opcode, null, null));
					for(Literal l :lt) {
						optp2.add(new OutputIns(l.value, l.name, null, null));
					}
					continue;
				}
				if(op.opcode.equalsIgnoreCase("END")) {
					optp2.add(new OutputIns(op.lc, null, null, null));
					continue;
				}
				if (op.op1 == null && op.op2 == null) {
					optp2.add(new OutputIns(op.lc, getHexCode(op.opcode), op.op1, op.op2));
					continue;
				}
				if(op.opcode.equalsIgnoreCase("BR")) {
					op.op2=op.op1;
					op.op1=Integer.toString(15);
				}
				if(op.opcode.equalsIgnoreCase("BNE")) {
					op.op2=op.op1;
					op.op1=Integer.toString(7);
				}
				if (getValue(op.op1) != -1)
					op.op1 = getValue(op.op1).toString();
				if (op.op2 != null) {
					if (getValue(op.op2) != -1)
						if (getMotType(op.opcode).equalsIgnoreCase("RX"))
							op.op2 = getOperand(op.op2);
						else
							op.op2 = getValue(op.op2).toString();
				}

				if(op.opcode.equalsIgnoreCase("DC")) {
					op.opcode=op.op1.substring(2,op.op1.length()-1);
					op.op1=null;
					optp2.add(new OutputIns(op.lc, op.opcode, op.op1, op.op2));
					continue;
				}
				if(op.opcode.equalsIgnoreCase("DS")) {
					op.opcode=op.op1.substring(0,op.op1.length()-1);
					op.op1=null;
					optp2.add(new OutputIns(op.lc, op.opcode, op.op1, op.op2));
					continue;
				}
				optp2.add(new OutputIns(op.lc, getHexCode(op.opcode), op.op1, op.op2));
			}
		}

		for (OutputIns op : optp2) {
			System.out.println(op);
			wrFC.write(op.toString());
			wrFC.newLine();
		}
		System.out.println("\nRegister Table ath end of Pass 2 :");
		for(Register r : rt) {
			System.out.println(r);
		}
		System.out.println("\n\n------ PASS 2 ENDS ------");
		sc.close();
		br.close();
		br1.close();
		wrMOT.close();
		wrPOT.close();
		wrST.close();
		wrLT.close();
		wrIC.close();
		wrFC.close();
	}

	public static int getType(String opcode) {
		int type = 0;
		if (pot.contains(opcode.toUpperCase()))
			type = 1;
		else {
			for (MachineOpcode m : mot) {
				if ((m.opcode).equalsIgnoreCase(opcode)) {
					type = 2;
					break;
				}
			}
		}
		return type;
	}

	public static Integer stContains(String operand) {
		int position = -1;
		for (Symbol s : st) {
			if ((s.name.equals(operand))) {
				position = s.id;
				break;
			}
		}
		return position;
	}

	public static Integer ltContains(String operand) {
		int position = -1;
		for (Literal l : lt) {
			if (l.name.equals(operand)) {
				position = l.id;
				break;
			}
		}
		return position;
	}

	public static Integer getValue(String operand) {
		int value = -1;
		if (operand.contains("#") && operand.charAt(0) == 'S') {
			for (Symbol s : st) {
				if ((s.id == Integer.parseInt(operand.substring(operand.length() - 1)))) {
					value = s.value;
					break;
				}
			}
		}
		if (operand.contains("#") && operand.charAt(0) == 'L') {
			for (Literal l : lt) {
				if ((l.id == Integer.parseInt(operand.substring(operand.length() - 1)))) {
					value = l.value;
					break;
				}
			}
		}
		return value;
	}

	public static String getHexCode(String opcode) {
		String hexcode = null;
		for (MachineOpcode m : mot) {
			if ((m.opcode.equals(opcode))) {
				hexcode = m.hexcode;
				break;
			}
		}
		return hexcode;
	}

	public static String getMotType(String opcode) {
		String type = null;
		for (MachineOpcode m : mot) {
			if ((m.opcode.equals(opcode))) {
				type = m.type;
				break;
			}
		}
		return type;
	}

	public static String getOperand(String operand) {
		int value=getValue(operand);
		int offset=Integer.MAX_VALUE;
		int regNumber=-1;
		for(Register r : rt) {
			int currOffset=value-r.value;
			if(offset>currOffset && currOffset>=0) {
				offset=currOffset;
				regNumber=r.regNumber;
			}
		}
		return offset+" (0,"+regNumber+")";
	}
}
