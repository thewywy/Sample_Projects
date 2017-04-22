
public class TokenObject{
	public int lineNumber;
	public String lexeme;
	public String type;

	public TokenObject(int ln, String l, String t){
		lineNumber = ln;
		lexeme = l;
		type = t;
	}
	
	public TokenObject(){
		lineNumber = 0;
		lexeme = "";
		type = "";
	}
}