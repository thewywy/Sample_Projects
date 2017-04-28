import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

// Recursive Descent Compiler:
// Compiles a .txt source code file into assembly output to assembly_output.asm 
// to be assembled and executed by the assembler/virtual machine: 
// virtual_machine_to_convert_to_binary_and_execute.exe.
// One Argument: fileName (e.g. $java -jar Compiler.jar source_code_to_be_compiled.txt)

public class Driver {
	public static Boolean pass1 = true;
	public static Boolean pass2 = false;
	public static Boolean debug = false;
	
	//lexical and syntax analysis
	public static String fileName;
	public static Integer symNum = 100;
	public static String currentScope = "g";
	public static SymbolData tempSD = new SymbolData();
	public static TokenObject previousToken = new TokenObject();
	public static TokenObject currentToken = new TokenObject();
	public static ArrayList<String> Lits = new ArrayList<String>();
	public static String needsParams = null;
	public static int lineNumber = 0;
	public static ArrayList<RegexObject> myRegexes = new ArrayList<>();
	public static Pattern r = null;
	public static String line = null;
	public static Hashtable<String, SymbolObject> SymbolTable = new Hashtable<String, SymbolObject>();
	public static FileReader fileReader;
	public static BufferedReader bufferedReader;
	
	//semantic analysis
	public static Stack<String> OP = new Stack<String>();
	public static Stack<SAR> SAS = new Stack<SAR>();
	public static String previousSymID = null;
	public static String currentSymID = null;
	public static String currentFunctionType;
	
	//icode generation
	public static ArrayList<QUAD> quads = new ArrayList<QUAD>();
	public static int SKIPIFcount = 0;
	public static int SKIPELSEcount = 0;
	public static int BEGINcount = 0;
	public static int ENDWHILEcount = 0;
	public static Stack<Integer> ifs = new Stack<Integer>();
	public static Stack<Integer> elses = new Stack<Integer>();
	public static Stack<Integer> whiles = new Stack<Integer>();
	public static Stack<Integer> endWhiles = new Stack<Integer>();	
	public static String line2;
	
	public static void main(String[] args) throws IOException {
		fileName = args[0];
		addRegexes();
		pass();
		pass1 = false;
		pass2  = true;
		previousToken.lineNumber = 0;
		pass();
		if(debug){
			for(QUAD quad: quads){
				System.out.println(quad);
			}
		}
		genTcode();
		System.out.println("Compilation Successful!");
	}

	public static void addRegexes() {
		myRegexes.add(new RegexObject("White", "^(\\s)+"));
		myRegexes.add(new RegexObject("Character", "^((')(\\\\)?[\\u0000-\\u007F]('))|^('\\s')"));
		myRegexes.add(new RegexObject("KeyWord", "(^((atoi)|(bool)|(class)|(char)|(input)|(output)|(else)|(false)|" +
									"(if)|(int)|(itoa)|(main)|(new)|(null)|(object)|(public)|(private)|(return)|" +
									"(string)|(true)|(void)|(while)|(spawn)|(lock)|(release)|(block)|(sym)|(comp2017)|" + 
									"(this)|(protected)|(unprotected)|(and)|(or))(?![a-zA-Z0-9]))"));
		myRegexes.add(new RegexObject("Identifier", "^([a-zA-Z])[a-zA-Z0-9]*"));
		myRegexes.add(new RegexObject("Punctuation", "^([\\s.;,\\t\\n])"));
		myRegexes.add(new RegexObject("Number", "^[0-9]+"));
		myRegexes.add(new RegexObject("Comment", "^(//).+"));
		myRegexes.add(new RegexObject("Symbol", "^([#|\\(|\\)|\\{|\\}|\\[|\\]])|(<<)|(>>)|(&&)|(\\|\\|)"));
		myRegexes.add(new RegexObject("ExpressionzSymbol", "^((==)|(and)|(or)|(=)|(!=)|(<=)|(>=)|(<)|(>)|(\\+)|(\\-)|(\\*)|(/))"));
		myRegexes.add(new RegexObject("Unknown", "^.+(\\s)*"));
	}
	
	public static void pass() {
		try {
			symNum =100;
			try {
				fileReader = new FileReader(fileName);
				bufferedReader = new BufferedReader(fileReader);
			} catch (final IOException e) {
				throw new ExceptionInInitializerError(e.getMessage());
			}
			lineNumber = 0;
			
			line = bufferedReader.readLine();
			line2 = line;
			lineNumber++;
			nextToken();
			compilation_unit();
			
			while((line = bufferedReader.readLine())!=null){
				if(!line.matches("\\s+")){
					currentToken.lexeme = "extra code";
					syntaxError("no more code");
				}
			}
				
			currentToken = new TokenObject(-1, "EOF", "EOT");
			
			if(debug){
				System.out.println(String.format("%-4s %-12s %s", currentToken.lineNumber, currentToken.type, currentToken.lexeme));
			}
			bufferedReader.close();
			fileReader.close();
			if(pass1 && debug){
				for(String key : SymbolTable.keySet()){
					System.out.println(SymbolTable.get(key).toString());
				}
			}
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file " + fileName);
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
		}
		if(debug){System.out.println();}
	}
	
	public static void nextToken() {
		while (line.equals("")) {
			try {
				line = bufferedReader.readLine();
				line2 = line;
				if (line == null) {
					return;
				}
				lineNumber++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (RegexObject aRegex : myRegexes) {
			r = Pattern.compile(aRegex.pattern);
			Matcher m = r.matcher(line);
			if (m.find()) {
				if (aRegex.name.equals("White")) {
					line = line.substring(m.group().length());
					nextToken();
					break;
				}
				if (aRegex.name.equals("Comment")) {
					try {
						if(debug){
							System.out.println(String.format("%-4s %-12s %s", lineNumber, "Comment", line));
						}
						lineNumber++;
						line = bufferedReader.readLine();
						nextToken();
						break;
					} catch (IOException e) {
						e.printStackTrace();
					}
					nextToken();
					break;
				}
				previousToken = currentToken;
				String lex = m.group().replaceAll("\\s+","");
				if(lex.equals("''")){
					lex = "' '";
				}
				currentToken = new TokenObject(lineNumber, lex, aRegex.name);
				if(!(previousToken.lineNumber <= 0) && debug){
					System.out.println(String.format("%-4s %-12s %s", previousToken.lineNumber, previousToken.type, previousToken.lexeme));
				}
				line = line.substring(m.group().length());
				break;
			}
		}
	}

	public static void compilation_unit() {
		if(pass1 && debug){System.out.println("compilation_unit");}
		while (currentToken.lexeme.equals("class")) {
			class_declaration();
		}
		if(pass2){
			quads.add(new QUAD("MAIN", "", "", line2));
		}
		terminalL("void", "void or class");
		terminalL("comp2017", "comp2017");
		terminalL("main", "main");
		tempSD.type = null;
		tempSD.returnType = "void";
		tempSD.accessMod = "public";
		genSym('F', "main");
		addScope(previousToken.lexeme);
		terminalL("(", "(");
		terminalL(")", ")");
		method_body();
		leaveScope();
	}

	public static void  class_declaration() {
		if(pass1 && debug){System.out.println("class_declaration");}
		terminalL("class", ""); //never error
		class_nameT();
		if(pass2){
			dup(previousToken);
		}
		genSym('C', "Class");
		addScope(previousToken.lexeme);
		terminalL("{", "{");
		while (isAModifier
				() || currentToken.type.equals("Identifier")) {
			class_member_declaration();
		}
		terminalL("}", "Class Name } or Modifier");
		leaveScope();
	}

	public static void class_member_declaration() {
		if(pass1 && debug){System.out.println("class_member_declaration");}
		if(isAModifier()){
			tempSD.accessMod = currentToken.lexeme;
			modifierT();
			tempSD.returnType = currentToken.lexeme;
			tempSD.type = currentToken.lexeme;			
			currentFunctionType = currentToken.lexeme;
			typeT();
			if(pass2){
				pndtExist();
			}
			terminalT("Identifier");
			if(pass2){
				dup(previousToken);
			}
			field_declaration();
		}
		else if(currentToken.type.equals("Identifier")){
			constructor_declaration();
		}
	}

	public static void field_declaration() {
		if(pass1 && debug){System.out.println("field_declaration");}
		if (currentToken.lexeme.equals("[") || currentToken.lexeme.equals("=") || currentToken.lexeme.equals(";")) 
		{		
			tempSD.returnType = null;
			if(currentToken.lexeme.equals("[")){
				tempSD.type = "@:" + tempSD.type;
			}
			genSym('V', "ivar");
			String tempName = previousToken.lexeme;
			if (currentToken.lexeme.equals("[")) {
				terminalL("[", ""); //never error  
				terminalL("]", "]");  
			}
			if(pass2){
				pndvPush(new Id_sar(tempName));
			}
			if (currentToken.lexeme.equals("=")) {
				terminalL("=", ""); //never error
				if(pass2){
					pndoPush(previousToken.lexeme);
				}
				assignment_expression();
			}
			if(previousToken.lexeme.equals("]")){
				terminalL(";", "= or ;");
			}
			else{
				terminalL(";", "Assignment Expression");
			}
			if(pass2){
				EOE();
			}
		}
		else 
		{
			tempSD.type = null;
			needsParams = "M" + symNum.toString();
			genSym('M', "method");
			addScope(previousToken.lexeme);
			terminalL("(", "[ = ; or (");
			if(isAType()){
				parameter_list();
			}
			if(previousToken.lexeme.equals("(")){
				terminalL(")", ") or type");
			}
			else{
				terminalL(")", ", or )");
			}
			method_body();
			leaveScope();
		}
	}
	
	public static void constructor_declaration() {
		if(pass1 && debug){System.out.println("constructor_declaration");}
		class_nameT();
		if(pass2){
			dup(previousToken);
			CD();
		}
		tempSD.accessMod = "public";
		tempSD.returnType = previousToken.lexeme;
		needsParams = "X" + symNum.toString();
		genSym('X', "Constructor");
		addScope(previousToken.lexeme);
		terminalL("(", "(");
		if (isAType()) {
			parameter_list();
		}
		if(previousToken.lexeme.equals("(")){
			terminalL(")", ") or type");
		}
		else{
			terminalL(")", ", or )");
		}
		method_body();
		leaveScope();
	}

	public static void method_body() {
		if(pass1 && debug){System.out.println("method_body");}
		terminalL("{", "{");
		while (isAType() || isAStatement()) {
			if (currentToken.type.equals("Identifier")) {
				peek();
				if (currentToken.type.equals("Identifier")) {
					currentToken = previousToken;
					variable_declaration();
				} else {
					currentToken = previousToken;
					statement();
				}
			} else {
				if (isAType()) {
					variable_declaration();
				} else {
					if (isAStatement()) {
						statement();
					}
				}
			}
		}
		terminalL("}", "type or Statement");
	}

	private static void variable_declaration() {
		if(pass1 && debug){System.out.println("variable_declaration");}
		tempSD.type = currentToken.lexeme;
		tempSD.accessMod = "private";
		typeT();
		if(pass2){
			pndtExist();
		}
		terminalT("Identifier");
		String id = previousToken.lexeme;
		if(currentToken.lexeme.equals("[")){
			tempSD.type = "@:" + tempSD.type;
		}
		genSym('L', "lvar");
		if (currentToken.lexeme.equals("[")) {
			terminalL("[", ""); //never error
			terminalL("]", "]");
		}
		if(pass2){
			dup(previousToken);
			pndvPush(new Id_sar(id));
		}
		if (currentToken.lexeme.equals("=")) {
			terminalL("=", ""); //never error
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			assignment_expression();
		}
		if(previousToken.lexeme.equals("=")){
			terminalL(";", "Assignment Expression");
		}
		else{
			terminalL(";", "= or ;");
		}
		if(pass2){
			EOE();
		}
	}

	private static void parameter_list() {
		if(pass1 && debug){System.out.println("parameter_list");}
		parameter();
		while(currentToken.lexeme.equals(",")){
			terminalL(",", ""); //never error
			parameter();
		}

	}

	public static void parameter() {
		if(pass1 && debug){System.out.println("parameter");}
		tempSD.type = currentToken.lexeme;
		tempSD.accessMod = "private";
		typeT(); //never error
		if(pass2){
			pndtExist();
		}
		terminalT("Identifier");
		genSym('P', "param");
		if (currentToken.lexeme.equals("[")) {
			terminalL("[", ""); //never error
			terminalL("]", "]"); 
		}
		if(pass2){
			dup(previousToken);
		}
	}
	
	private static void statement() {
		if(pass1 && debug){System.out.println("statement");}
		switch (currentToken.lexeme) {
		case "{":
			terminalL("{", "");
			while(isAStatement()){
				statement();
			}
			terminalL("}", "}");
			break;
		case "if":
			terminalL("if", "");
			terminalL("(", "(");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			terminalL(")", ")");
			if(pass2){
				pndClosingParen();
				pndIf();
			}
			statement();
			if(pass2 && !currentToken.lexeme.equals("else")){
				quads.add(new QUAD("SKIPIF" + ifs.pop(), "ADI", "R8", "0",  line2));
			}
			if(currentToken.lexeme.equals("else"))
			{
				if(pass2){
					quads.add(new QUAD("JMP", "SKIPELSE" + SKIPELSEcount, line2));
					elses.push(SKIPELSEcount);
					quads.add(new QUAD("SKIPIF" + ifs.pop(), "ADI", "R8", "0",  line2));
					SKIPELSEcount++;
				}
				terminalL("else", "else");
				statement();
				if(pass2){
					quads.add(new QUAD("SKIPELSE" + elses.pop(), "ADI", "R8", "0",  line2));
				}
			}
			break;
		case "while":
			terminalL("while", ""); //never error
			if(pass2){
				quads.add(new QUAD("BEGIN" + BEGINcount, "ADI", "R8", "0",  line2));
				whiles.push(BEGINcount);
				BEGINcount++;
			}
			terminalL("(", "(");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			terminalL(")", ")");
			if(pass2){
				pndClosingParen();
				pndWhile();
			}
			statement();
			if(pass2){
				quads.add(new QUAD("JMP", "BEGIN" + whiles.pop(), line2));
				quads.add(new QUAD("ENDWHILE" + endWhiles.pop(), "ADI", "R8", "0",  line2));
			}
			break;
		case "return":
			terminalL("return", "");
			if(isAnExpression())
			{
				expression(); 
			}
			terminalL(";", ";");
			if(pass2){
				pndReturn();
			}
			break;
		case "output":
			terminalL("output", "");
			terminalL("<<", "<<");
			expression();
			terminalL(";", ";");
			if(pass2){
				pndOutput();
			}
			break;
		case "input":
			terminalL("input", "");
			terminalL(">>", ">>");
			expression();
			terminalL(";", ";");
			if(pass2){
				pndInput();
			}
			break;
		case "spawn":
			terminalL("spawn", "");
			expression();
			terminalL("set", "set");
			terminalT("Identifier");
			if(pass2){
				pndiPush(new Id_sar(previousToken.lexeme));
			}
			terminalL(";", ";");
			break;
		case "block":
			terminalL("block", "");
			terminalL(";", ";");
			break;
		case "lock":
			terminalL("lock", "");
			terminalT("Identifier");
			if(pass2){
				pndiPush(new Id_sar(previousToken.lexeme));
			}
			terminalL(";", ";");
			break;
		case "release":
			terminalL("release", "");
			terminalT("Identifier");
			if(pass2){
				pndiPush(new Id_sar(previousToken.lexeme));
			}
			terminalL(";", ";");
			break;
		default:
			if(isAnExpression()){
				expression();
				terminalL(";", "; or expressionz");
				if(pass2){
					EOE();
				}
			}
			break;
		}
	}

	private static void expression() {
		if(pass1 && debug){System.out.println("expression");}
		if(currentToken.lexeme.equals("+") || currentToken.lexeme.equals("-")){
			currentToken.type = "Number";
			}
		switch (currentToken.lexeme) {
		case "(":
			terminalL("(", "(");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			terminalL(")", ")");
			if(pass2){
				pndClosingParen();
			}
			if(isAnExpressionZ())
				{
					expressionz();
				}
			return;
		case "true":
			terminalL("true", "");
			tempSD.accessMod = "public";
			tempSD.type = "bool";
			genSym('B', "blit");
			if(pass2){
				pndlPush(new Id_sar(previousToken.lexeme));
			}
			if(isAnExpressionZ())
			{
				expressionz();
			}
			return;
		case "false":
			terminalL("false", "");
			tempSD.accessMod = "public";
			tempSD.type = "bool";
			genSym('B', "blit");
			if(pass2){
				pndlPush(new Id_sar(previousToken.lexeme));
			}
			if(isAnExpressionZ())
			{
				expressionz();
			}
			return;
		case "null":
			terminalL("null", "");
			tempSD.accessMod = "public";
			tempSD.type = "null";
			genSym('U', "null");
			if(pass2){
				pndlPush(new Id_sar(previousToken.lexeme));
			}
			if(isAnExpressionZ())
			{
				expressionz();
			}
			return;
		case "this":
			terminalL("this", "");
			if(pass2){
				pndiPush(new Id_sar(previousToken.lexeme));
				pndiExist();
			}
			if(currentToken.lexeme.equals("."))
			{
				member_refz();
			}
			if(isAnExpressionZ())
			{
				expressionz();
			}
			return;
		default:
			switch (currentToken.type) {
			case "Number":
				if(currentToken.lexeme.equals("+") || currentToken.lexeme.equals("-")){
					String sign = "";
					if(currentToken.lexeme.equals("-")){
						sign = currentToken.lexeme;
					}
					nextToken();
					if(!currentToken.type.equals("Number")){
						syntaxError("numeric literal");
					}
					nextToken();
					previousToken.lexeme = sign + previousToken.lexeme;
					if(pass2){
						pndlPush(new Id_sar(previousToken.lexeme));
					}
				}
				else{
					terminalT("Number");
				}
				tempSD.accessMod = "public";
				tempSD.type = "int";				
				genSym('N', "ilit");
				if(isAnExpressionZ())
				{
					expressionz(); 
				}
				return;
			case "Character":
				terminalT("Character");
				tempSD.accessMod = "public";
				tempSD.type = "char";
				genSym('H', "clit");
				if(isAnExpressionZ())
				{
					expressionz(); 
				}
				return;
			case "Identifier":
				terminalT("Identifier");
				if(pass2){
					pndiPush(new Id_sar(previousToken.lexeme));
				}
				if(currentToken.lexeme.equals("(") || currentToken.lexeme.equals("["))
				{
					fn_arr_member();
				}
				if(pass2){
					pndiExist();
				}
				if(currentToken.lexeme.equals("."))
				{
					member_refz();
				}
				if(isAnExpressionZ())
				{
					expressionz();
				}
				return;
			}
		}
		syntaxError("Expression");
	}

	private static void fn_arr_member() {
		if(pass1 && debug){System.out.println("fn_arr_member");}
		if(currentToken.lexeme.equals("(")){
			terminalL("(", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
				pndBAL();
			}
			if(isAnExpression()){
				argument_list();
			}
		terminalL(")", ")");
		if(pass2){
			pndClosingParen();
			pndEAL();
			pndFunc();
		}

		}
		
		else if(currentToken.lexeme.equals("[")){
			terminalL("[", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			terminalL("]", "]");
			if(pass2){
				pndClosingSquare();
				pndArr();
			}
		}
	}

	private static void argument_list() {
		if(pass1 && debug){System.out.println("argument_list");}
		expression();
		while(currentToken.lexeme.equals(",")){
			terminalL(",", "");
			if(pass2){
				pndArgument();
			}
			expression();
		}
	}

	private static void member_refz() {
		if(pass1 && debug){System.out.println("member_refz");}
		terminalL(".", ".");
		terminalT("Identifier");
		if(pass2){
			pndiPush(new Id_sar(previousToken.lexeme));
		}
		if(currentToken.lexeme.matches("\\(|\\["))
		{
			fn_arr_member();
		}
		if(pass2){
			pndrExist();
		}
		if(currentToken.lexeme.equals("."))
		{
			member_refz();
		}
	}

	private static void expressionz() {
		if(pass1 && debug){System.out.println("expressionz");}
		switch(currentToken.lexeme){
		case "=":
			terminalL("=", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			assignment_expression();
			break;
		case "and":
			terminalL("and", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			break;
		case "or":
			terminalL("or", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			break;
		case "==":
			terminalL("==", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			break;
		case "!=":
			terminalL("!=", "");
			if(pass2){pndoPush(previousToken.lexeme);
			}
			expression();
			break;
		case "<=":
			terminalL("<=", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			break;
		case ">=":
			terminalL(">=", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			break;
		case "<":
			terminalL("<", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			break;
		case ">":
			terminalL(">", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			break;
		case "+":
			terminalL("+", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			break;
		case "-":
			terminalL("-", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			break;
		case "*":
			terminalL("*", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			break;
		case "/":
			terminalL("/", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			break;
		}
	}

	private static void assignment_expression() {
		if(pass1 && debug){System.out.println("assignment_expression");}
		switch(currentToken.lexeme){
			case "new":
				terminalL("new", "");
				typeT();
				new_declaration();
				break;
			case "atoi":
				terminalL("atoi", "");
				terminalL("(", "(");
				if(pass2){
					pndoPush(previousToken.lexeme);
				}
				expression();
				terminalL(")", ")");
				if(pass2){
					pndClosingParen();
					pndAtoi();
				}
				break;
			case "itoa":
				terminalL("itoa", "");
				terminalL("(", "(");
				if(pass2){
					pndoPush(previousToken.lexeme);
				}
				expression();
				terminalL(")", ")");
				if(pass2){
					pndClosingParen();
					pndItoA();
				}
				break;
			default:
				if(currentToken.lexeme.matches("\\+|-")){
					currentToken.type = "Number";
				}
				if(isAnExpression()){
					expression();
					break;
				}
		}
	}

	private static void new_declaration() {
		if(pass1 && debug){System.out.println("new_declaration");}
		if(currentToken.lexeme.equals("(")){
			terminalL("(", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
				pndBAL();
			}
			if(isAnExpression()){
				argument_list();
			}
			terminalL(")", ")");
			if(pass2){
				pndClosingParen();
				pndEAL();
				pndNewObj();
			}
		}
		else if(currentToken.lexeme.equals("[")){
			terminalL("[", "");
			if(pass2){
				pndoPush(previousToken.lexeme);
			}
			expression();
			terminalL("]", "]");
			if(pass2){
				pndClosingSquare();
				pndNewArray();
			}
		}
	}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~end of recursive calls~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public static void numeric_literal() {
		if (currentToken.type.equals("Number")) {
			nextToken();
			return;
		}
		else if(currentToken.lexeme.matches("-|+")){
			nextToken();
			if(currentToken.type.equals("Number")){
				nextToken();
				return;
			}
		}
		else {
		syntaxError("class_nameT");
		}
	}
	
	public static void class_nameT() {
		if (currentToken.type.equals("Identifier")) {
			nextToken();
		}
		else {
		syntaxError("Class Name");
		}
	}

	public static void modifierT() {
		if (currentToken.lexeme.equals("public") || currentToken.lexeme.equals("private")) {
			nextToken();
		} else {
			syntaxError("modifier");
		}
	}

	public static void typeT() {
		if (isAType()) {
			if(pass2){
				pndtPush(new Id_sar(currentToken.lexeme));
			}
			nextToken();
		} else {
			syntaxError("type");
		}
	}
	
	public static void terminalL(String lexeme, String other){
		if (currentToken.lexeme.equals(lexeme)) {
			nextToken();
		}
		else{
			syntaxError(other);
		}
	}
	
	public static void terminalT(String type){
		if (currentToken.type.equals(type)) {
			nextToken();
			if(pass2 && (type.matches("Character|Number"))){
				pndlPush(new Id_sar(previousToken.lexeme));
			}
		}
		else{
			syntaxError(type);
		}
	}
	
	public static void syntaxError(String expected) {
		int errorLine;
		if(line.equals("")){
			errorLine = previousToken.lineNumber;
		}
		else{
			errorLine = currentToken.lineNumber;
		}
		System.out.println(
				errorLine + 
				": found " + currentToken.lexeme + 
				" expected " + expected);
		System.exit(0);
	}
	
	public static TokenObject peek(){
		try {
			bufferedReader.mark(1000);
			String tempLine = line;
			nextToken();
			if(pass1 && debug){System.out.println("peeked");}
			line = tempLine;
			bufferedReader.reset();
			return currentToken;
		} catch (IOException e) {
			// peek failed
			e.printStackTrace();
		}
		return null;
	}
	
	public static void genSym(Character c, String t){
		if(c.equals('N') || c.equals('H') || c.equals('B') || c.equals('U')){
			if(!Lits.contains(previousToken.lexeme)){
				Lits.add(previousToken.lexeme);
				if(pass1){
					SymbolTable.put(c + symNum.toString(), new SymbolObject("g", c + symNum.toString(), previousToken.lexeme, t, tempSD));
				}
			}
			else{
				return;
			}
		}
		else{
			if(pass1){
				SymbolTable.put(c + symNum.toString(), new SymbolObject(currentScope, c + symNum.toString(), previousToken.lexeme, t, tempSD));
			}
			if(c.equals('P') && pass1){
				SymbolTable.get(needsParams).data.param.add(c + symNum.toString());
			}
		}
		previousSymID = currentSymID;
		currentSymID = c +symNum.toString();
		tempSD = new SymbolData();
		symNum++;
	}
	
	public static String genTemp(Character c, String s){
		String symID = c + symNum.toString();
		SymbolTable.put(symID, new SymbolObject(currentScope, symID, symID, "temp", new SymbolData(s, "private")));
		if(debug){
			System.out.println("GENTEMP: " + symID);
		}
		symNum++;
		return symID;
	}
	
	public static void addScope(String s){	
		currentScope += "." + s;
	}
	
	public static void leaveScope(){
		int i = currentScope.lastIndexOf(".");
		currentScope = currentScope.substring(0, i);
	}
	
	public static Boolean isAnExpressionZ()
	{
		switch (currentToken.lexeme) {
		case "=":
			return true;
		case "and":
			return true;
		case "or":
			return true;
		case "==":
			return true;
		case "!=":
			return true;
		case "<=":
			return true;
		case ">=":
			return true;
		case "<":
			return true;
		case ">":
			return true;
		case "+":
			return true;
		case "-":
			return true;
		case "*":
			return true;
		case "/":
			return true;
		default:
			return false;
		}
	}
	
	public static Boolean isAnExpression()
	{
		switch (currentToken.lexeme) {
		case "(":
			return true;
		case "true":
			return true;
		case "false":
			return true;
		case "null":
			return true;
		case "this":
			return true;
		case "-":
			return true;
		case "+":
			return true;
		default:
			switch (currentToken.type) {
			case "Number":
				return true;
			case "Identifier":
				return true;
			case "Character":
				return true;
			}
			return false;
		}
	}
	
	public static Boolean isAType()
	{
		switch(currentToken.lexeme) {
		case "int":
			return true;
		case "char":
			return true;
		case "bool":
			return true;
		case "void":
			return true;
		case "sym":
			return true;
		default:
			if(currentToken.type.equals("Identifier")){
				return true;
			}
			else{
				return false;
			}
		}
	}
	
	public static Boolean isAStatement(){
		switch (currentToken.lexeme){
		case "{":
				return true;
		case "if":
			return true;
		case "while":
			return true;
		case "return":
			return true;
		case "output":
			return true;
		case "input":
			return true;
		case "spawn":
			return true;
		case "block":
			return true;
		case "lock":
			return true;
		case "release":
			return true;
		default:
			if(isAnExpression()){
				return true;
			}
			else{
				return false;
			}
		}
	}
	
	public static Boolean isAModifier(){
		switch(currentToken.lexeme){
			case "public":
				return true;
			case "private":
				return true;
			default:
				return false;
		}
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//~~~~~~~~~~~~~~~~~Semantic Actions
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public static void pndiPush(Id_sar id_sar){
		if(debug){System.out.println("iPush: " + id_sar.getRecord());}
		SAS.push(id_sar);
	}
	
	public static void pndlPush(SAR lit_sar){
		for (String key : SymbolTable.keySet()) {
			if (SymbolTable.get(key).scope.equals("g")&& SymbolTable.get(key).value.equals(lit_sar.getRecord())) {
				if(debug){
					System.out.println("lPush: " + key + " " + lit_sar.getRecord());
				}
				SAS.push(new Id_sar(key));	
				return;
			} 
		}
	}
	
	public static void pndoPush(String operator){
		if(debug){System.out.println("oPush: " + operator);}
		if(OP.isEmpty()){
			OP.push(operator);
			return;
		}
		while(!OP.isEmpty() && precedence(operator, false) <= precedence(OP.peek(), true)){
		switch (OP.pop()) {
		case "*":
			pndMultiply();
			break;
		case "/":
			pndDivide();
			break;
		case "+":
			pndAdd();
			break;
		case "-":
			pndSubtract();
			break;
		case "<":
			pndLessThan();
			break;
		case ">":
			pndGreaterThan();
			break;
		case "<=":
			pndLessEqual();
			break;
		case ">=":
			pndGreaterEqual();
			break;
		case "==":
			pndEqual();
			break;
		case "!=":
			pndNotEqual();
			break;
		case "and":
			pndAnd();
			break;
		case "or":
			pndOr();
			break;
		case "=":
			pndAssignment();
			break;
		default:
				errorSem(8, "oPush compiler error", "", "");
			}		
		}
		OP.push(operator);
	}
	
	public static void pndtPush(SAR type_sar){
		if(debug){System.out.println("tPush: " + type_sar.getRecord());}
		SAS.push(type_sar);
	}
	
	public static void pndiExist() {
		Id_sar top_sar = (Id_sar) SAS.pop();
		
		if(top_sar.getRecord().equals("this")){
			String Scope = currentScope;
			Scope = rescoping(Scope);
			for(String key: SymbolTable.keySet()){
				if(("g." + SymbolTable.get(key).value).equals(Scope)){
					if(debug){System.out.println("iExist: " + key);}
					SAS.push(new Id_sar(key));
					return;
				}
			}
			errorSem(8, "Declaring this outside Constructor/Function", "", "");
		}
		
		top_sar.iPushExists();
	}
	
	public static void pndvPush(SAR top_sar){
		for (String key : SymbolTable.keySet()) {
			if (SymbolTable.get(key).scope.equals(currentScope)&& SymbolTable.get(key).value.equals(top_sar.getRecord())) {
				if(debug){System.out.println("vPush: " + SymbolTable.get(key).symid + " " + top_sar.getRecord());}
				SAS.push(new Id_sar(key));	
				return;
			} 
		}
	}
	
	public static void pndrExist(){
		Id_sar top_sar = (Id_sar)  SAS.pop();
		Id_sar next_sar = (Id_sar) SAS.pop();
		top_sar.rPushExist(next_sar);
	}
	
	public static void pndtExist() {
		SAR type_sar = SAS.pop();
		if(type_sar.getRecord().matches("int|char|bool|void|sym")){
			if(debug){System.out.println("tExist: " + type_sar.getRecord());}
			return;
		}
		for (String key : SymbolTable.keySet()) {
			if (SymbolTable.get(key).scope.equals("g")
					&& SymbolTable.get(key).value.equals(type_sar.getRecord())
					&& SymbolTable.get(key).kind.equals("Class")) {
				if(debug){System.out.println("tExist: " + type_sar.getRecord());}
				return;
			} 
		}
		errorSem(3, type_sar.getRecord() ,"", "");
	}
	
	public static void pndBAL(){
		if(debug){System.out.println("pndBAL");}
		SAS.push(new Bal_sar("1bal"));
	}
	
	public static void pndEAL(){
		if(debug){System.out.println("pndEAL");}
		Al_sar al = new Al_sar();
		while(!(SAS.peek().getRecord().equals("1bal"))){
			al.args.add(SAS.pop().getRecord());
		}
		SAS.pop();
		SAS.push(al);
	}
	
	private static void pndFunc() {
		if(debug){System.out.println("pndFunc");}
		Al_sar al = (Al_sar)SAS.pop();
		Id_sar id = (Id_sar)SAS.pop();
		for (String key : SymbolTable.keySet()) {
			if (SymbolTable.get(key).kind.equals("method")&& SymbolTable.get(key).value.equals(id.getRecord())) {
				SAS.push(new Func_sar(id.getRecord(), al.args));
				return;
			} 
		}
	}

	private static void pndArr() {
		if(debug){System.out.println("pndArr");}
		Id_sar exp = new Id_sar(SAS.pop().getRecord());
		Id_sar arr = new Id_sar(SAS.pop().getRecord());
		if(SymbolTable.get(exp.symID).data.type.equals("int")){
			SAS.push(new Arr_sar(arr.getRecord(), exp.getRecord()));
		}
	}
	
	private static void pndIf() {
		if(debug){System.out.println("pndIf");}
		SAR exp = SAS.pop();
		if(exp.getType().equals("bool")){
			quads.add(new QUAD("BF", exp.getRecord(), "SKIPIF" + SKIPIFcount, line2));
			ifs.push(SKIPIFcount);
			SKIPIFcount++;
			return;
		}
		else{
			errorSem(4, "if", exp.getType(), "");
		}
	}

	private static void pndWhile() {
		if(debug){System.out.println("pndWhile");}
		SAR exp = SAS.pop();
		if(exp.getType().equals("bool")){
			quads.add(new QUAD("BF", exp.getRecord(), "ENDWHILE" + ENDWHILEcount, line2));
			endWhiles.push(ENDWHILEcount);
			ENDWHILEcount++;
			return;
		}
		else{
			errorSem(4, "while", exp.getType(), "");
		}
	}

	private static void pndReturn() {
		if(debug){System.out.println("pndReturn");}
		while(!OP.isEmpty()){
			String operator = OP.pop();
			switch (operator) {
				case ")":
					pndClosingParen();
					break;
				case "]":
					pndClosingSquare();
					break;
				case "*":
					pndMultiply();
					break;
				case "/":
					pndDivide();
					break;
				case "+":
					pndAdd();
					break;
				case "-":
					pndSubtract();
					break;
				case "<":
					pndLessThan();
					break;
				case ">":
					pndGreaterThan();
					break;
				case "<=":
					pndLessEqual();
					break;
				case ">=":
					pndGreaterEqual();
					break;
				case "==":
					pndEqual();
					break;
				case "!=":
					pndNotEqual();
					break;
				case "and":
					pndAnd();
					break;
				case "or":
					pndOr();
					break;
				case "=":
					pndAssignment();
					break;
				default:
					errorSem(8, "output general error", "", "");
			}
		}
		if(SymbolTable.get(SAS.peek().getRecord()).data.type.equals(currentFunctionType) || SymbolTable.get(SAS.peek().getRecord()).value.equals("null")){
			SAS.pop();
			return;
		}
		else{
			errorSem(7, currentFunctionType, SymbolTable.get(SAS.pop().getRecord()).data.type, "");
		}
	}

	private static void pndOutput() {
		if(debug){System.out.println("pndOutput");}
		while(!OP.isEmpty()){
			String operator = OP.pop();
			switch (operator) {
				case ")":
					pndClosingParen();
					break;
				case "]":
					pndClosingSquare();
					break;
				case "*":
					pndMultiply();
					break;
				case "/":
					pndDivide();
					break;
				case "+":
					pndAdd();
					break;
				case "-":
					pndSubtract();
					break;
				case "<":
					pndLessThan();
					break;
				case ">":
					pndGreaterThan();
					break;
				case "<=":
					pndLessEqual();
					break;
				case ">=":
					pndGreaterEqual();
					break;
				case "==":
					pndEqual();
					break;
				case "!=":
					pndNotEqual();
					break;
				case "and":
					pndAnd();
					break;
				case "or":
					pndOr();
					break;
				case "=":
					pndAssignment();
					break;
				default:
					errorSem(8, "output general error", "", "");
			}
		}
		if(SymbolTable.get(SAS.peek().getRecord()).data.type.matches("int|char")){
			switch (SymbolTable.get(SAS.peek().getRecord()).data.type) {
			case "int":
				quads.add(new QUAD("WRITE", "1", SAS.pop().getRecord(), line2));
				break;
			case "char":
				quads.add(new QUAD("WRITE", "2", SAS.pop().getRecord(), line2));
				break;
			}
			return;
		}
	}

	private static void pndInput() {
		if(debug){System.out.println("pndInput");}
		while(!OP.isEmpty()){
			String operator = OP.pop();
			switch (operator) {
				case ")":
					pndClosingParen();
					break;
				case "]":
					pndClosingSquare();
					break;
				case "*":
					pndMultiply();
					break;
				case "/":
					pndDivide();
					break;
				case "+":
					pndAdd();
					break;
				case "-":
					pndSubtract();
					break;
				case "<":
					pndLessThan();
					break;
				case ">":
					pndGreaterThan();
					break;
				case "<=":
					pndLessEqual();
					break;
				case ">=":
					pndGreaterEqual();
					break;
				case "==":
					pndEqual();
					break;
				case "!=":
					pndNotEqual();
					break;
				case "and":
					pndAnd();
					break;
				case "or":
					pndOr();
					break;
				case "=":
					pndAssignment();
					break;
				default:
					errorSem(8, "output general error", "", "");
			}
		}
		if(SymbolTable.get(SAS.peek().getRecord()).data.type.matches("int|char")){
			switch (SymbolTable.get(SAS.peek().getRecord()).data.type) {
			case "int":
				quads.add(new QUAD("READ", "1", SAS.pop().getRecord(), line2));
				break;
			case "char":
				quads.add(new QUAD("READ", "2", SAS.pop().getRecord(), line2));
				break;
			}
			return;
		}
	}
	
	private static void pndAtoi() {
		if(debug){System.out.println("pndAtoi");}
		SAR exp = SAS.pop();
		if(SymbolTable.get(exp.getRecord()).data.type.equals("char")){
			SAS.push(new Id_sar(genTemp('T', "int")));
			quads.add(new QUAD("ATOI", exp.getRecord(), SAS.peek().getRecord(), line2));
			return;
		}
		else{
			errorSem(8, "atoi compiler error", "", "");
		}
	}
	
	private static void pndItoA() {
		if(debug){System.out.println("pndItoA");}
		SAR exp = SAS.pop();
		if(SymbolTable.get(exp.getRecord()).data.type.equals("int")){
			SAS.push(new Id_sar(genTemp('T', "char")));
			quads.add(new QUAD("ITOA", exp.getRecord(), SAS.peek().getRecord(), line2));
			return;
		}
		else{
			errorSem(8, "itoa compiler error", "", "");
		}
	}
	
	private static void pndNewObj() {
		if(debug){System.out.println("pndNewObj");}
		Al_sar al = (Al_sar) SAS.pop();
		SAR type = SAS.pop();
		String key = "";
		String scope = "g." + type.getRecord();

		for (String theKey : SymbolTable.keySet()) {
			if (SymbolTable.get(theKey).scope.equals(scope)
					&& SymbolTable.get(theKey).value.equals(type.getRecord()) && SymbolTable.get(theKey).symid.startsWith("X")) {
				key = theKey;
				break;
			}
		}
		Collections.reverse(al.args);
		if(!key.equals("")){
			if(al.args.size() != SymbolTable.get(key).data.param.size()){
				errorSem(8, "Constructor: " + type.getRecord() + paramsToString(al.args) + " not defined", "", "");
			}
			int i = 0;
			for(String arg: SymbolTable.get(key).data.param){
				if(SymbolTable.get(arg).data.type.equals(SymbolTable.get(al.args.get(i)).data.type)){
					i++;					
				}
				else{
					System.out.println(SymbolTable.get(al.args.get(i)).value + " " + SymbolTable.get(arg).data.type);
					if(SymbolTable.get(al.args.get(i)).value.equals("null") && !SymbolTable.get(arg).data.type.equals("int|bool|char")){
						i++;
					}
					else{
						errorSem(8, "Constructor: " + type.getRecord() + paramsToString(al.args) + " not defined", "", "");
					}
				}
				
			}
			if(debug){System.out.println("newObj: created");}
			SAS.push(new Id_sar(genTemp('T', Driver.SymbolTable.get(key).data.returnType)));
		}
		else{
			errorSem(8, "No constructors for " + type.getRecord() + " are defined", "", "");
		}
	}

	private static void pndNewArray() {
		if(debug){System.out.println("pndNewArray");}
		SAR exp = SAS.pop();
		if(exp.getType().equals("int")){
			SAR type_sar = SAS.pop();
			if(type_sar.getRecord().matches("bool|int|char|void|sym")){
				SAS.push(new Arr_sar(type_sar.getRecord(), exp.getRecord()));
				return;
			}
			for(String key: SymbolTable.keySet()){
				if(SymbolTable.get(key).value.equals(type_sar.getRecord()) && SymbolTable.get(key).scope.equals("g")){
					SAS.push(new Arr_sar(type_sar.getRecord(), exp.getRecord()));
					return;
				}
			}
		}
		errorSem(8, "NewArray General Compiler Error", "", "");
	}
	
	public static void CD(){
		if(debug){System.out.println("CD");}
		if(("g." + previousToken.lexeme).equals(currentScope)){
			return;
		}
		else{
			errorSem(8, "Constructor name is not the same as the class", "", "");
		}
	}
	
	public static void dup(TokenObject tokObj){
		String scope  = currentScope;
		int countS = 0;
		int countI = 0;
		while(!scope.equals("")){
			countS++;
			for(String key : SymbolTable.keySet()) {
				if(SymbolTable.get(key).scope.equals(scope) && SymbolTable.get(key).value.equals(tokObj.lexeme)){
					countI++;
				}
			}
			if(countI > countS){
				errorSem(5, "" , tokObj.lexeme, ""); //s1 = class function variable
				System.exit(1);
			}
			scope = rescoping(scope);
		}
		if(debug){System.out.println("no duplication for: " + tokObj.lexeme);}
	}

	private static void pndClosingParen() {
		if(debug){System.out.println("pndClosingParen");}
		while (!OP.peek().equals("(")) {
			String op = OP.pop();
			switch (op) {
			case ")":
				pndClosingParen();
				break;
			case "]":
				pndClosingSquare();
				break;
			case "*":
				pndMultiply();
				break;
			case "/":
				pndDivide();
				break;
			case "+":
				pndAdd();
				break;
			case "-":
				pndSubtract();
				break;
			case "<":
				pndLessThan();
				break;
			case ">":
				pndGreaterThan();
				break;
			case "<=":
				pndLessEqual();
				break;
			case ">=":
				pndGreaterEqual();
				break;
			case "==":
				pndEqual();
				break;
			case "!=":
				pndNotEqual();
				break;
			case "and":
				pndAnd();
				break;
			case "or":
				pndOr();
				break;
			case "=":
				pndAssignment();
				break;
			default:
				errorSem(8, "Closing Parens error: OP popped a non-operator", "" ,"");
			}
		}
		OP.pop();
	}
	
	private static void pndClosingSquare() {
		if(debug){System.out.println("pndClosingSquare");}
		if(OP.isEmpty()){
			return;
		}
		while(!OP.peek().equals("[")){
			switch (OP.pop()) {
				case "(":
					//pndMultiply();
					break;
				case "-":
					pndSubtract();
					break;
				case "+":
					pndAdd();
					break;
				case "/":
					pndDivide();
					break;
				case "*":
					pndMultiply();
					break;
				case ")":
					//pndMultiply();
					break;
				case "=":
					pndAssignment();
					break;
				default:
					errorSem(8, "Closing Square error: OP popped not an operator", "" ,"");
				}		
			}
		OP.pop();
	}
	
	private static void pndArgument() {
		if(debug){System.out.println("pndArgument");}
		while(!(OP.peek().equals("("))){
			String operator = OP.pop();
			switch (operator) {
				case "-":
					pndSubtract();
					break;
				case "+":
					pndAdd();
					break;
				case "/":
					pndDivide();
					break;
				case "*":
					pndMultiply();
					break;
				case "=":
					pndAssignment();
					break;
				default:
					errorSem(8, "Argument error: OP popped not an operator", "" ,"");
			}
		}
	}
	
	public static void EOE(){
		if(debug){System.out.println("EOE");}
		while(!OP.isEmpty()){
			String operator = OP.pop();
			switch (operator) {
				case "-":
					pndSubtract();
					break;
				case "+":
					pndAdd();
					break;
				case "/":
					pndDivide();
					break;
				case "*":
					pndMultiply();
					break;
				case "=":
					pndAssignment();
					break;
				default:
					errorSem(8, "EOE error: OP popped not an operator", "" ,"");
			}
		}
	}
	
	private static void pndAdd() {
		if(debug){System.out.println("pndAdd");}
		SAR t1 = SAS.pop();
		SAR t2 = SAS.pop();
		if(t1.getType().equals(t2.getType())){
			if(debug){System.out.println("#+ they are the same type/assignable");}
			SAS.push(new Id_sar(genTemp('T', "int")));
			quads.add(new QUAD("ADD", t1.getRecord(), t2.getRecord(), SAS.peek().getRecord(), true, line2));
			return;
		}
		else{
			errorSem(6, t2.getType() + " " + SymbolTable.get(t2.getRecord()).value, "+", t1.getType() + " " + SymbolTable.get(t1.getRecord()).value);
		}
	}
	
	private static void pndSubtract() {
		if(debug){System.out.println("pndSubtract");}
		SAR t1 = SAS.pop(); //pop 1
		SAR t2 = SAS.pop(); //pop 2
		if(t1.getType().equals(t2.getType())){
			if(debug){System.out.println("#- they are the same type/assignable");}
			SAS.push(new Id_sar(genTemp('T', "int")));
			quads.add(new QUAD("SUB", t1.getRecord(), t2.getRecord(), SAS.peek().getRecord(), true,  line2));
			return;
		}
		else{
			errorSem(6, t2.getType() + " " + SymbolTable.get(t2.getRecord()).value, "-", t1.getType() + " " + SymbolTable.get(t1.getRecord()).value);
		}
	}

	private static void pndMultiply() {
		if(debug){System.out.println("pndMultiply");}
		SAR t1 = SAS.pop();
		SAR t2 = SAS.pop();
		if(t1.getType().equals(t2.getType())){
			if(debug){System.out.println("#* they are the same type/assignable");}
			SAS.push(new Id_sar(genTemp('T', "int")));
			quads.add(new QUAD("MUL", t1.getRecord(), t2.getRecord(), SAS.peek().getRecord(), true, line2));
			return;
		}
		else{
			errorSem(6, t2.getType() + " " + SymbolTable.get(t2.getRecord()).value, "*", t1.getType() + " " + SymbolTable.get(t1.getRecord()).value);
		}
	}

	private static void pndDivide() {
		if(debug){System.out.println("pndDivide");}
		SAR t1 = SAS.pop();
		SAR t2 = SAS.pop();
		if(t1.getType().equals(t2.getType())){
			if(debug){System.out.println("#/ they are the same type/assignable");}
			SAS.push(new Id_sar(genTemp('T', "int")));
			quads.add(new QUAD("DIV", t1.getRecord(), t2.getRecord(), SAS.peek().getRecord(), true, line2));
			return;
		}
		else{
			errorSem(6, t2.getType() + " " + SymbolTable.get(t2.getRecord()).value, "/", t1.getType() + " " + SymbolTable.get(t1.getRecord()).value);
		}
	}

	private static void pndAssignment() {
		SAR exp1_sar = SAS.pop();
		SAR exp2_sar = SAS.pop();
		if(exp1_sar.getType().equals(exp2_sar.getType()) || (!exp1_sar.getType().matches("int|bool|char") && SymbolTable.get(exp1_sar.getRecord()).value.equals("null"))){
			if(debug){System.out.println("#= they are the same type/assignable");}
			String moveType = SymbolTable.get(exp2_sar.getRecord()).data.type;
			SAS.push(new Id_sar(genTemp('T', moveType)));
			if(moveType.matches("int|bool")){
				moveType = "MOVr";
			}
			else{
				moveType = "MOVb";
			}
			quads.add(new QUAD(moveType, exp1_sar.getRecord(), exp2_sar.getRecord(), line2));
		}
		else {
			errorSem(6, exp2_sar.getType() + " " + SymbolTable.get(exp2_sar.getRecord()).value, "=", exp1_sar.getType() + " " + SymbolTable.get(exp1_sar.getRecord()).value);
	
		}
	}

	
	private static void pndLessThan() {
		if(debug){System.out.println("pndLessThan");}
		SAR exp2 = SAS.pop();
		SAR exp1 = SAS.pop();
		if(exp2.getType().equals(exp1.getType())){
			SAS.push(new Id_sar(genTemp('T', "bool")));
			quads.add(new QUAD("LT", exp1.getRecord(), exp2.getRecord(), SAS.peek().getRecord(), true, line2));
			return;
		}
		else{
			errorSem(6, exp1.getType() + " " + SymbolTable.get(exp1.getRecord()).value, "<", exp2.getType() + " " + SymbolTable.get(exp2.getRecord()).value);
		}
	}

	
	private static void pndGreaterThan() {
		if(debug){System.out.println("pndGreaterThan");}
		SAR exp2 = SAS.pop();
		SAR exp1 = SAS.pop();
		if(exp2.getType().equals(exp1.getType())){
			SAS.push(new Id_sar(genTemp('T', "bool")));
			quads.add(new QUAD("GT", exp1.getRecord(), exp2.getRecord(), SAS.peek().getRecord(), true, line2));
			return;
		}
		else{
			errorSem(6, exp1.getType() + " " + SymbolTable.get(exp1.getRecord()).value, ">", exp2.getType() + " " + SymbolTable.get(exp2.getRecord()).value);
		}
	}

	private static void pndEqual() {
		if(debug){System.out.println("pndEqual");}
		SAR exp2 = SAS.pop();
		SAR exp1 = SAS.pop();
		if(exp2.getType().equals(exp1.getType())){
			SAS.push(new Id_sar(genTemp('T', "bool")));
			quads.add(new QUAD("EQ", exp1.getRecord(), exp2.getRecord(), SAS.peek().getRecord(), true, line2));
			return;
		}
		else{
			errorSem(6, exp1.getType() + " " + SymbolTable.get(exp1.getRecord()).value, "==", exp2.getType() + " " + SymbolTable.get(exp2.getRecord()).value);
		}
	}
	
	private static void pndLessEqual() {
		if(debug){System.out.println("pndLessEqual");}
		SAR exp2 = SAS.pop();
		SAR exp1 = SAS.pop();
		if(exp2.getType().equals(exp1.getType())){
			SAS.push(new Id_sar(genTemp('T', "bool")));
			quads.add(new QUAD("LE", exp1.getRecord(), exp2.getRecord(), SAS.peek().getRecord(), true, line2));
			return;
		}
		else{
			errorSem(6, exp1.getType() + " " + SymbolTable.get(exp1.getRecord()).value, "<=", exp2.getType() + " " + SymbolTable.get(exp2.getRecord()).value);
		}
	}

	
	private static void pndGreaterEqual() {
		if(debug){System.out.println("pndGreaterEqual");}
		SAR exp2 = SAS.pop();
		SAR exp1 = SAS.pop();
		if(exp2.getType().equals(exp1.getType())){
			SAS.push(new Id_sar(genTemp('T', "bool")));
			quads.add(new QUAD("GE", exp1.getRecord(), exp2.getRecord(), SAS.peek().getRecord(), true, line2));
			return;
		}
		else{
			errorSem(6, exp1.getType() + " " + SymbolTable.get(exp1.getRecord()).value, ">=", exp2.getType() + " " + SymbolTable.get(exp2.getRecord()).value);
		}
	}
	
	private static void pndAnd() {
		if(debug){System.out.println("pndAnd");}
		SAR exp2 = SAS.pop();
		SAR exp1 = SAS.pop();
		if(exp2.getType().equals("bool") && exp1.getType().equals("bool")){
			SAS.push(new Id_sar(genTemp('T', "bool")));
			quads.add(new QUAD("AND", exp1.getRecord(), exp2.getRecord(), SAS.peek().getRecord(), true, line2));
			return;
		}
		else{
			errorSem(4, "and", exp1.getType(), "");
		}
	}

	private static void pndOr() {
		if(debug){System.out.println("pndOr");}
		SAR exp2 = SAS.pop();
		SAR exp1 = SAS.pop();
		if(exp2.getType().equals("bool") && exp1.getType().equals("bool")){
			SAS.push(new Id_sar(genTemp('T', "bool")));
			quads.add(new QUAD("OR", exp1.getRecord(), exp2.getRecord(), SAS.peek().getRecord(), true, line2));
			return;
		}
		else{
			errorSem(4, "or", exp1.getType(), "");
		}
	}
	
	private static void pndNotEqual() {
		if(debug){System.out.println("pndNotEqual");}
		SAR exp2 = SAS.pop();
		SAR exp1 = SAS.pop();
		if(exp2.getType().equals(exp1.getType())){
			SAS.push(new Id_sar(genTemp('T', "bool")));
			quads.add(new QUAD("NE", exp1.getRecord(), exp2.getRecord(), SAS.peek().getRecord(), true, line2));
			return;
		}
		else{
			errorSem(4, "!=", exp1.getType(), "");
		}
	}
	
	
	public static String rescoping(String s) {
		if (!s.equals("g")) {
			int i = s.lastIndexOf(".");
			return currentScope.substring(0, i);
		} else {
			return "";
		}
	}
	
	public static String keyWithinCurrentScope(String theLexeme, String scope) {
		while (!scope.equals("")) {
			for (String key : SymbolTable.keySet()) {
				if (SymbolTable.get(key).scope.equals(scope)
						&& SymbolTable.get(key).value.equals(theLexeme)) {
					return key;
				} 
			}
			scope = rescoping(scope);
		}
		return "";
	}
	
	public static void errorSem(int code1, String s1, String s2, String s3){
		int errorLine;
		if(line.matches(" |;")){
			errorLine = currentToken.lineNumber;
		}
		else{
			errorLine = previousToken.lineNumber;
		}
		System.out.print(errorLine + ": ");
		switch (code1) {
		case 1: //iexist
			System.out.println(s1 + " " + s2 + " not defined"); // s1 = variable array function s2 = lexeme + "(" + "parameter_list_types//" + ")" for function
			break;
		case 2:	//rexist
			System.out.println(s1 + " " + s2 + " not defined/public in class " + s3); //s1 = variable array function s2 = lexeme + "(" + "parameter_list_types//" + ")" for function s3 = class_name
			break;
		case 3: //texist
			System.out.println("Type " + s1 + " not Defined"); // s1 = lexeme
			break;
		case 4: //boolean returners
			System.out.println(s1 + " requires bool got " + s2); //s1 = if while and or not equal	s2 = error type
			break;
		case 5: //duplication
			System.out.println("Duplicate " + s1 + " " + s2); //s1 = class function variable s2 = lexeme
			break;
		case 6: //evaluators, assignment, and arithmetic
			System.out.println("Invalid Operation " + s1 + " " + s2 + " " + s3); // s1 = exp1.type + " " + exp1.lexeme s2 = operatorSymbol s3 = exp2.type + " " exp2.lexeme
			break;
		case 7: // return
			System.out.println("Return requires " + s1 + " returned " + s2); //s1 = function_return_type s2 = return_type
			break;
		case 8:
			System.out.print(s1);
		}
		System.exit(0);
	}
	
	public static int precedence(String operator, Boolean b){
		switch(operator) {
		case ".":
			if(b)return -1;
			else return 15;
		case "(":
			if(b)return -1;
			else return 15;
		case "[":
			if(b)return -1;
			else return 15;
		case ")":
			return 0;
		case "]":
			return 0;
		case "*":
			return 13;
		case "/":
			return 13;
		case "+":
			return 11;
		case "-":
			return 11;
		case "<":
			return 9;
		case ">":
			return 9;
		case "<=":
			return 9;
		case ">=":
			return 9;
		case "==":
			return 7;
		case "!=":
			return 7;
		case "and":
			return 5;
		case "or":
			return 3;
		case "=":
			return 1;
		default:
			return -1;
		}
	}	
	
	private static void genTcode() throws IOException {
		File file = new File("assembly_output.asm");
		FileWriter fileWriter = new FileWriter(file);
		for(String key: SymbolTable.keySet()){
			if(key.charAt(0) == 'H' || key.charAt(0) == 'N' || key.charAt(0) == 'B'){
				fileWriter.write("[" + key + "] .");
				if(SymbolTable.get(key).data.type.equals("bool")){
					fileWriter.write("INT ");
					if(SymbolTable.get(key).value.equals("true")){
						fileWriter.write("1\n");
					}
					else{
						fileWriter.write("0\n");
					}
				}
				if(SymbolTable.get(key).data.type.equals("int")){
					fileWriter.write("INT ");
					fileWriter.write(SymbolTable.get(key).value + "\n");
				}
				if(SymbolTable.get(key).data.type.equals("char")){
					fileWriter.write("BYT ");
					fileWriter.write(SymbolTable.get(key).value + "\n");
				}
			}
			if((key.charAt(0) == 'T') || (key.charAt(0) == 'L') && (SymbolTable.get(key).data.type.matches("int|char|bool"))){
				if(SymbolTable.get(key).data.type.matches("int|bool")){
					fileWriter.write("[" + key + "] .");
					fileWriter.write("INT 0\n");
				}
				if(SymbolTable.get(key).data.type.equals("char")){
					fileWriter.write("[" + key + "] .");
					fileWriter.write("BYT '0'\n");
				}
			}
		}

		fileWriter.write("[false] .INT 0\n[true] .INT 1\n[negOne] .INT -1\n[char0] .BYT '0'\n");
		fileWriter.write("LDR R0 [false]\nLDR R1 [true]\nLDR R2 [negOne]\n");
		fileWriter.write("JMP [MAIN]\n");
		
		for(QUAD quad: quads){
			switch (quad.q1){
			case "WRITE":
				switch (quad.q2) {
				case "1": //int
					fileWriter.write("LDR R3 [" + quad.q3 + "]\nTRP 1\n");
					break;
				case "2": //char
					fileWriter.write("LDB R3 [" + quad.q3 + "]\nTRP 3\n");
					break;
				}
				break;
			case "READ":
				switch (quad.q2) {
				case "1": //int
					fileWriter.write("TRP 2\nSTR R3 [" + quad.q3 + "]\n");
					break;
				case "2": //char
					fileWriter.write("TRP 4\n" + "STR R3 [" + quad.q3 + "]\n");
					break;
				}
				break;
			case "MOVr":
				fileWriter.write("LDR R5 [" + quad.q2 + "]\nSTR R5 [" + quad.q3 + "]\n");
				break;
			case "MOVb":
				fileWriter.write("LDB R5 [" + quad.q2 + "]\nSTB R5 [" + quad.q3 + "]\n");
				break;
			case "BF":
				fileWriter.write("LDR R5 [" + quad.q2 + "]\nBRZ R5 [" + quad.q3 + "]\n");
				break;
			case "JMP":
				fileWriter.write(quad.q1 + " [" + quad.q2 + "]\n");
				break;
			case "GT":				
				fileWriter.write("LDR R6 [" + quad.q3 + "]\nLDR R7 [" + quad.q2 + "]\nCMP R6 R7\nCMP R6 R2\nCMP R6 R1\nSTR R6 [" + quad.q4 + "]\n");
				break;
			case "LT":
				fileWriter.write("LDR R6 [" + quad.q2 + "]\nLDR R7 [" + quad.q3 + "]\nCMP R6 R7\nCMP R6 R2\nCMP R6 R1\nSTR R6 [" + quad.q4 + "]\n");
				break;
			case "GE":
				fileWriter.write("LDR R6 [" + quad.q2 + "]\nLDR R7 [" + quad.q3 + "]\nCMP R6 R7\nCMP R6 R2\nSTR R6 [" + quad.q4 + "]\n");				
				break;	
			case "LE":
				fileWriter.write("LDR R6 [" + quad.q3 + "]\nLDR R7 [" + quad.q2 + "]\nCMP R6 R7\nCMP R6 R2\nSTR R6 [" + quad.q4 + "]\n");				
				break;	
			case "NE":
				fileWriter.write("LDR R6 [" + quad.q2 + "]\nLDR R7 [" + quad.q3 + "]\nCMP R6 R7\nSTR R6 [" + quad.q4 + "]\n");
				break;
			case "EQ":
				fileWriter.write("LDR R5 [" + quad.q2 + "]\nLDR R6 [" + quad.q2 + "]\nLDR R7 [" + quad.q3 + "]\nCMP R6 R7\nCMP R7 R5\nMUL R6 R7\nCMP R6 R2\nSTR R6 [" + quad.q4 + "]\n");
				break;
			case "AND":
				fileWriter.write("LDR R6 [" + quad.q2 + "]\nLDR R7 [" + quad.q3 + "]\nAND R6 R7\nSTR R6 [" + quad.q4 + "]\n");
				break;
			case "OR":
				fileWriter.write("LDR R6 [" + quad.q2 + "]\nLDR R7 [" + quad.q3 + "]\nOR R6 R7\nSTR R6 [" + quad.q4 + "]\n");
				break;
			case "SUB":
				fileWriter.write("LDR R5 [" + quad.q2 + "]\nLDR R6 [" + quad.q3 + "]\nSUB R6 R5\nSTR R6 [" + quad.q4 + "]\n");
				break;
			case "ADD":
				fileWriter.write("LDR R5 [" + quad.q2 + "]\nLDR R6 [" + quad.q3 + "]\nADD R6 R5\nSTR R6 [" + quad.q4 + "]\n");
				break;
			case "MUL":
				fileWriter.write("LDR R5 [" + quad.q2 + "]\nLDR R6 [" + quad.q3 + "]\nMUL R6 R5\nSTR R6 [" + quad.q4 + "]\n");
				break;
			case "DIV":
				fileWriter.write("LDR R5 [" + quad.q2 + "]\nLDR R6 [" + quad.q3 + "]\nDIV R6 R5\nSTR R6 [" + quad.q4 + "]\n");
				break;	
			case "ATOI":
				fileWriter.write("LDR R6 [" + quad.q2 + "]\nLDR R7 [char0]\nSUB R6 R7\nSTR R6 [" + quad.q3 + "]\n");
				break;
			case "ITOA":
				fileWriter.write("LDR R6 [" + quad.q2 + "]\nADI R6 48\nSTR R6 [" + quad.q3 + "]\n");
				break;
			case "MAIN":
				fileWriter.write("[MAIN] ADI R8 0\n");
				break;
			default:
				fileWriter.write("[" + quad.label + "] " + quad.q1 + " " + quad.q2 + " " + quad.q3 + "\n");
				break;
			}
		}
		fileWriter.write("TRP 0");
		fileWriter.close();
	}
	
	public static String paramsToString(ArrayList<String> args){
		String paramTypes = "(";
		Boolean b = false;
		for(String arg: args){
			if(b){
				paramTypes +=  ", ";
			}
			b = true;
			paramTypes += Driver.SymbolTable.get(arg).data.type;
		}
		paramTypes += ")";
		return paramTypes;
	}
}