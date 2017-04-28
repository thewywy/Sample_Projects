import java.util.ArrayList;
import java.util.Collections;

public class Func_sar extends Id_sar{
	String functionName;
	ArrayList<String> args;
	Func_sar(String id, ArrayList<String> args){
		super(id);
		functionName = id;
		this.args = args;
	}
	
	public String getRecord(){
		return functionName;
	}
	
	public void iPushExists(){
		String key = Driver.keyWithinCurrentScope(this.getRecord(), Driver.currentScope);
		if(!key.equals("")){
			int i = 0;
			int counter = 0;
			int paramCount = Driver.SymbolTable.get(key).data.param.size();
			for(String arg: Driver.SymbolTable.get(key).data.param){
				if(Driver.SymbolTable.get(arg).data.type.equals(Driver.SymbolTable.get(this.args.get(i)).data.type)){
					counter++;
				}
			}
			if(counter == paramCount){
				if(Driver.debug){
					System.out.println("iExist: Function");
				}
				Driver.SAS.push(new Id_sar(Driver.genTemp('T', Driver.SymbolTable.get(key).data.returnType)));
				return;
			}
			else{
				String paramTypes = "(";
				for(String arg: Driver.SymbolTable.get(key).data.param){
					paramTypes += Driver.SymbolTable.get(arg).data.type + ", ";
				}
				paramTypes += ")";
				Driver.errorSem(1, "Function", this.getRecord() + paramTypes, "");
			}
		}
		else {
			Driver.errorSem(1, "Function", this.getRecord(), "");
		}
	}
	
	public void rPushExist(SAR next_sar) {
		String objectScope = "g." + next_sar.getType();
		
		for(String key : Driver.SymbolTable.keySet()){
			if(Driver.SymbolTable.get(key).scope.equals(objectScope) && Driver.SymbolTable.get(key).value.equals(this.getRecord())){ //finds function symid
				if((Driver.SymbolTable.get(key).data.accessMod.equals("public") || (Driver.currentScope.startsWith(Driver.SymbolTable.get(key).scope + ".")))){ // is accessible?
					Collections.reverse(this.args);
					//arty
					if(this.args.size() != Driver.SymbolTable.get(key).data.param.size()){
						Driver.errorSem(2, "Function", this.getRecord() + paramsToString(), Driver.SymbolTable.get(next_sar.getRecord()).value);
					}
					//order and type
					int i = 0;
					for(String arg: Driver.SymbolTable.get(key).data.param){
						if(!Driver.SymbolTable.get(arg).data.type.matches(Driver.SymbolTable.get(this.args.get(i)).data.type + "|" + Driver.SymbolTable.get(this.args.get(i)).value)){
								Driver.errorSem(2, "Function", this.getRecord() + paramsToString(), Driver.SymbolTable.get(next_sar.getRecord()).value);
						}
						i++;
					}
					Driver.SAS.push(new Ref_sar(Driver.genTemp('R', Driver.SymbolTable.get(key).data.returnType)));
					if(Driver.debug){
						System.out.println("rExist: " + key); 
					}
					return;
				}
			}
		}
		Driver.errorSem(2, "Function", this.getRecord() + paramsToString(), Driver.SymbolTable.get(next_sar.getRecord()).value);
	}
	
	public String paramsToString(){
		String paramTypes = "(";
		Boolean b = false;
		for(String arg: this.args){
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