/**
 * 
 */
package gov.nih.nlm.skr.SA;

import java.util.List;

public class SemanticGroup {

	private List semanticTypes;
	private String name;
	private String abbreviation;
	
	/**
	 * 
	 */
	public SemanticGroup() {
		super();
		// TODO Auto-generated constructor stub 
	}
	
	/**
	 * @param colorCode
	 * @param name
	 * @param abbreviation
	 */
	public SemanticGroup(String name, String abbreviation) {
		super();
		this.name = name;
		this.abbreviation = abbreviation;
	}

	/**
	 * @return Returns the abbreviation.
	 */
	public String getAbbreviation() {
		return abbreviation;
	}

	/**
	 * @param abbreviation The abbreviation to set.
	 */
	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the semanticTypes.
	 */
	public List getSemanticTypes() {
		return semanticTypes;
	}

	/**
	 * @param semanticTypes The semanticTypes to set.
	 */
	public void setSemanticTypes(List semanticTypes) {
		this.semanticTypes = semanticTypes;
	}



}
