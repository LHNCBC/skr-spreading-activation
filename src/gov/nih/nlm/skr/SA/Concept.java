package gov.nih.nlm.skr.SA;

public class Concept {
    String cui;
    String preferredName;
    String type;
    float weight;
    
    public Concept(String cui, String preferredName, String type) {
    	this.cui = cui;
    	this.preferredName = preferredName;
    	this.type = type;
    } 
    
    public Concept(String cui, String preferredName, float weight) {
    	this.cui = cui;
    	this.preferredName = preferredName;
    	this.weight = weight;
    } 
    
	
	public boolean equals(Object o) {

		if (o == null || !(o instanceof Concept))
			return false;
		// return((this.cui.equals(((Concept) o).cui) && this.type.equals(((Concept) o).type)));
		return(this.cui.equals(((Concept) o).cui));

	}
	
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + cui.hashCode();
		// hash = hash * 31 + type.hashCode();
		return hash;
	}
}
