
public class Id_sar extends SAR{
	public String symID;

	public String getRecord(){
		return symID;
	}
	Id_sar (String s){
		symID = s;
		
	}
	
	public String getType(){
		if(symID.startsWith("X") || symID.startsWith("M")){
			return Driver.SymbolTable.get(symID).data.returnType;
		}
		if(symID.startsWith("C")){
			return Driver.SymbolTable.get(symID).value;
		}
		return Driver.SymbolTable.get(symID).data.type;
	}
	
	public void iPushExists(){
		String key = Driver.keyWithinCurrentScope(this.getRecord(), Driver.currentScope);
		if(!key.equals("")){
			Driver.SAS.push(new Id_sar(Driver.SymbolTable.get(key).symid)); 
			if(Driver.debug){
				System.out.println("iExist:");
			}
			return;
		}
		else {
			Driver.errorSem(1, "Variable", this.getRecord(), "");
		}
	}

	public void rPushExist(SAR next_sar) {
		String objectScope = "g." + next_sar.getType();
		for(String key : Driver.SymbolTable.keySet()){
			if(Driver.SymbolTable.get(key).scope.equals(objectScope) && Driver.SymbolTable.get(key).value.equals(this.getRecord())){
				if((Driver.SymbolTable.get(key).data.accessMod.equals("public") && Driver.currentScope.equals("g.main")) || Driver.currentScope.startsWith(objectScope )){
					if(Driver.debug){
						System.out.println("rExist: " + key); 
					}
					Driver.SAS.push(new Ref_sar(key));
					return;
				}
			}
		}
		Driver.errorSem(2, "Variable", this.getRecord(), Driver.SymbolTable.get(next_sar.getRecord()).value);
	}
}