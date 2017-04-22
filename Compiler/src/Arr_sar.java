public class Arr_sar extends Id_sar{
	public String arrayName;
	public String index;
	public String getRecord(){
		return arrayName;
	}
	Arr_sar (String a, String i){
		super(a);
		arrayName = a;
		index = i;
	}
	
	public void iPushExists(){
		String key = Driver.keyWithinCurrentScope(this.arrayName, Driver.currentScope);
		if(!key.equals("")){
			if(!Driver.SymbolTable.get(key).data.type.startsWith("@:")){
				Driver.errorSem(1, "Array", this.getRecord(), "");
			}
			Driver.SAS.push(new Id_sar(Driver.genTemp('T', Driver.SymbolTable.get(key).data.type.substring(2))));
			if(Driver.debug){
				System.out.println("iExist: " + key);
			}
			return;
		}
		Driver.errorSem(1, "Array", this.getRecord(), "");
	}
	
	public void rPushExist(SAR next_sar) {
		String objectScope = "g." + next_sar.getType();
		for(String key : Driver.SymbolTable.keySet()){
			if(Driver.SymbolTable.get(key).scope.equals(objectScope) && Driver.SymbolTable.get(key).value.equals(this.getRecord())){
				if((Driver.SymbolTable.get(key).data.accessMod.equals("public") && Driver.currentScope.equals("g.main")) || Driver.currentScope.startsWith(objectScope )){
					Driver.SAS.push(new Ref_sar(Driver.genTemp('R', Driver.SymbolTable.get(key).data.type.substring(2))));
					if(Driver.debug){
						System.out.println("rExist: " + key); 
					}
					return;
				}
			}
		}
		Driver.errorSem(2, "Array", this.getRecord(), next_sar.getType());
	}
	
	public String getType(){
		return "@:" + arrayName;
	}
}