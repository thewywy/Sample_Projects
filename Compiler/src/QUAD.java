
public class QUAD {
	String label;
	String q1;
	String q2;
	String q3;
	String q4;
	String comment;
	
	QUAD(String label, String q1, String q2, String q3, String comment){
		this.label = label;
		this.q1 = q1;
		this.q2 = q2;
		this.q3 = q3;
		this.comment = comment;
	}
	
	QUAD(String q1, String q2, String q3, String q4, Boolean b, String comment){
		this.q1 = q1;
		this.q2 = q2;
		this.q3 = q3;
		this.q4 = q4;		
		this.comment = comment;
	}
	
	QUAD(String q1, String q2, String q3, String comment){
		this.q1 = q1;
		this.q2 = q2;
		this.q3 = q3;
		this.comment = comment;
	}
	
	QUAD(String q1, String q2, String comment){
		this.q1 = q1;
		this.q2 = q2;
		this.comment = comment;
	}
	
	@Override
	public String toString() {
		return label + " " + q1 + " " + q2 + " "
				+ q3 + " " + q4 + " " + comment;
	}
}
