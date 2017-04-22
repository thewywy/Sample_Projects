import java.util.ArrayList;

public class SymbolData{
	public String type;
	public String accessMod;
	public String returnType;
	public ArrayList<String> param;	
	
	SymbolData(){
		type = null;
		accessMod = null;
		returnType = null;
		param = new ArrayList<String>();
	}
	
	SymbolData(String type, String accessMod){
		this.type = type;
		this.accessMod = accessMod;
	}


	@Override
	public String toString() {
		String tempReturn = "";
		if(type != null){
			tempReturn += "\ttype: " + type + "\n"; 
		}
		if(accessMod != null){
			tempReturn += "\taccessMod: " + accessMod + "\n"; 
		}
		if(returnType != null){
			tempReturn += "\treturnType: " + returnType + "\n"; 
		}
		if(!param.isEmpty()){
			tempReturn += "\tparam: [";
			for(String p : param){
				tempReturn += p + " ";
			}
			tempReturn += "]";
		}
		return tempReturn;
	}
	

}
