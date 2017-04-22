
public class Ref_sar extends Id_sar {
	public String symID;
	public String getRecord(){
		return symID;
	}
	Ref_sar (String s){
		super(s);
		symID = s;
	}
	public String getType(){
		if(symID.startsWith("M")){
			return Driver.SymbolTable.get(symID).data.returnType;
		}
		else{
			return Driver.SymbolTable.get(symID).data.type;
		}
	}
}
