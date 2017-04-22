
public class SymbolObject{
	SymbolObject(String s, String id, String v, String k, SymbolData d){
		scope = s;
		symid = id;
		value = v;
		kind = k;
		data = d;
	}
	SymbolObject(){}
	
	public String scope;
	public String symid;
	public String value;
	public String kind;
	public SymbolData data;
	
	@Override
	public String toString() {
		return "\nscope: " + scope + "\nsymid: " + symid + "\nvalue: " + value + "\nkind: " + kind + "\ndata: "
				+ data.toString();
	}
}