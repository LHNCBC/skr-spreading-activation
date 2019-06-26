package gov.nih.nlm.skr.SA;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.sql.DataSource;


public class PredicationList extends ArrayList<Predication> {
	/* private static final String QUERY_PREFIX = "SELECT p.PMID, s.SENTENCE, p.SUBJECT_NAME , p.PREDICATE, p.OBJECT_NAME FROM "+
			" PREDICATION p, SENTENCE s WHERE p.SENTENCE_ID = s.SENTENCE_ID AND ("; */
	Connection conn = null;
	// Statement stmt = null;


	public PredicationList(String DBConnectionString, String DBname, String DBUsername, String DBPassword){
		try {
		Class.forName( "com.mysql.jdbc.Driver" ) ;
		  conn = DriverManager.getConnection( DBConnectionString + "/" +  DBname + "?autoReconnect=true", DBUsername, DBPassword ) ;
		  // stmt = conn.createStatement() ;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Predication>  getPredications(String nameType, String seed1, String seed2, List<String> semtypes, String[] predicates, boolean negationInclude) throws SQLException{
		String query = createQuery(nameType, seed1, seed2, semtypes, predicates, negationInclude);
		ArrayList<Predication> list = queryExecute(query);
		return list;
	}

	public ArrayList<Predication>  getPredications(String nameType, String seed1, String seed2, List predications, List<String> semtypes, String[] predicates, boolean negationInclude) throws SQLException{
		String query = createQuery(nameType, seed1, seed2, predications, semtypes, predicates, negationInclude);
		ArrayList<Predication> list = queryExecute(query);
		return list;
	}
	
	public  ArrayList<SentencePredication> getSentencePredications(Vector concepts, String[] predicates, boolean negationInclude) throws SQLException{
		String query = createQuery(concepts, predicates, negationInclude);
		ArrayList<SentencePredication>  list = queryExecute(query,concepts);
		return list;
	}

	protected String createQuery(String nameType, String seed1, String seed2, List<String> semtypes, String[] predicates, boolean negationInclude) throws SQLException{

		StringBuffer query = null;

		query = new StringBuffer("SELECT PREDICATION_ID, PMID, SENTENCE_ID, PREDICATE, SUBJECT_NAME, SUBJECT_SEMTYPE, SUBJECT_CUI, SUBJECT_NOVELTY, OBJECT_NAME, OBJECT_SEMTYPE, OBJECT_CUI, OBJECT_NOVELTY FROM PREDICATION WHERE  ");
		if(nameType.compareToIgnoreCase("CUI") == 0)
			query.append("((SUBJECT_CUI = \"" + seed1 + "\" or OBJECT_CUI = \"" + seed2 + "\") or (SUBJECT_CUI = \"" + seed2 + "\" or OBJECT_CUI = \"" + seed1 +"\"))");
		else
			query.append("((SUBJECT_NAME = \"" + seed1 + "\" or OBJECT_NAME = \"" + seed2 + "\") or (SUBJECT_NAME = \"" + seed2 + "\" or OBJECT_NAME = \"" + seed1 +"\"))");

		if(semtypes != null && semtypes.size() > 0) {			
			StringBuffer subjTypesHead = new StringBuffer(" and SUBJECT_SEMTYPE in (");
			StringBuffer typesBuf = new StringBuffer(subjTypesHead);
			for(String str: semtypes) {
				typesBuf.append("'" + str + "',");
			}			
			typesBuf.setCharAt(typesBuf.length()-1, ')');
			query.append(typesBuf);
			StringBuffer objTypesHead = new StringBuffer(" and OBJECT_SEMTYPE in (");
			typesBuf = new StringBuffer(objTypesHead);
			for(String str: semtypes) {
				typesBuf.append("'" + str + "',");
			}	
			
			typesBuf.setCharAt(typesBuf.length()-1, ')');
			query.append(typesBuf);
		}
		
		if(predicates != null && predicates.length > 0) {
			query.append(new String(" and PREDICATE in ("));
			for(int i=0; i < predicates.length; i++) {
				query.append("'" + predicates[i] + "',");
				if(negationInclude)
					query.append("'NEG_" + predicates[i] + "',");
			}
			query.setCharAt(query.length() - 1, ')');
		}

		System.out.println("Database Query : " + query.toString());
		return query.toString();
	}
	
	protected String createQuery(String nameType, String seed1, String seed2, List predications, List<String> semtypes, String[] predicates, boolean negationInclude) throws SQLException{

		StringBuffer query = null;
		String s1 = seed1.toLowerCase();
		String s2 = seed2.toLowerCase();
		HashSet<String> alreadyIn = new HashSet<String>();
		query = new StringBuffer("SELECT PREDICATION_ID, PMID, SENTENCE_ID, PREDICATE, SUBJECT_NAME, SUBJECT_SEMTYPE, SUBJECT_CUI, SUBJECT_NOVELTY, OBJECT_NAME, OBJECT_SEMTYPE, OBJECT_CUI, OBJECT_NOVELTY FROM PREDICATION WHERE ");
		if(predications.size() > 0) {
			Iterator it = predications.iterator();
			int pNumber = 0;
			while(it.hasNext()) {
				Predication apred = (Predication) it.next();
				String subj = apred.subject.get(0).toLowerCase();
				String obj = apred.object.get(0).toLowerCase();
				String subjCUI = apred.subjectCUI.get(0).toLowerCase();
				String objCUI = apred.objectCUI.get(0).toLowerCase();

				StringBuffer segment = new StringBuffer();
				/* if(pNumber > 0)
				query.append(" and ((");
				else
					query.append("(("); */
				boolean subjIncluded = false;
				if(nameType.compareToIgnoreCase("CUI") == 0) {
					if(!subjCUI.equals(s1) && !subjCUI.equals(s2)  && !alreadyIn.contains(subjCUI)) {
					segment.append(" (SUBJECT_CUI = \"" + subjCUI + "\" or OBJECT_CUI = \"" + subjCUI + "\"" );
					subjIncluded = true;
					alreadyIn.add(subjCUI);
					}
					if(subjIncluded && !obj.equals(subjCUI) && !obj.equals(s1) && !obj.equals(s2)  && !alreadyIn.contains(objCUI)) { // if obj is different from subj
					segment.append(" or SUBJECT_CUI = \"" + objCUI + "\" or OBJECT_CUI = \"" + objCUI +"\")");
					alreadyIn.add(objCUI);
					} else 	if(!subjIncluded && !obj.equals(subjCUI) && !obj.equals(s1) && !obj.equals(s2)  && !alreadyIn.contains(objCUI)) { // if obj is different from subj
					segment.append(" (SUBJECT_CUI = \"" + objCUI + "\" or OBJECT_CUI = \"" + objCUI + "\")");
					alreadyIn.add(objCUI);
					} else if(subjIncluded)
					segment.append(")");
				
					if(segment.length() > 0) {
						if(pNumber > 0 )
							query.append(" or (");
						else
							query.append("(");
						query.append(segment);
						query.append(" )");
					} 
				} else {
					if(!subj.equals(s1) && !subj.equals(s2)  && !alreadyIn.contains(subj)) {
						segment.append(" (SUBJECT_NAME = \"" + subj + "\" or OBJECT_NAME = \"" + subj + "\"" );
						subjIncluded = true;
						alreadyIn.add(subj);
						}
						if(subjIncluded && !obj.equals(subj) && !obj.equals(s1) && !obj.equals(s2)  && !alreadyIn.contains(obj)) { // if obj is different from subj
						segment.append(" or SUBJECT_NAME = \"" + obj + "\" or OBJECT_NAME = \"" + obj +"\")");
						alreadyIn.add(obj);
						} else 	if(!subjIncluded && !obj.equals(subj) && !obj.equals(s1) && !obj.equals(s2)  && !alreadyIn.contains(obj)) { // if obj is different from subj
						segment.append(" (SUBJECT_NAME = \"" + obj + "\" or OBJECT_NAME = \"" + obj + "\")");
						alreadyIn.add(obj);
						} else if(subjIncluded)
						segment.append(")");
					
						if(segment.length() > 0) {
							if(pNumber > 0 )
								query.append(" or (");
							else
								query.append("(");
							query.append(segment);
							query.append(" )");
						} 
				}
				pNumber++;
			}
			
			if(semtypes != null) {
				StringBuffer subjTypesHead = new StringBuffer(" and SUBJECT_SEMTYPE in (");
				StringBuffer typesBuf = new StringBuffer(subjTypesHead);
				for(String str: semtypes) {
					typesBuf.append("'" + str + "',");
				}			
				typesBuf.setCharAt(typesBuf.length()-1, ')');
				query.append(typesBuf);
				StringBuffer objTypesHead = new StringBuffer(" and OBJECT_SEMTYPE in (");
				typesBuf = new StringBuffer(objTypesHead);
				for(String str: semtypes) {
					typesBuf.append("'" + str + "',");
				}			
				typesBuf.setCharAt(typesBuf.length()-1, ')'); 
				query.append(typesBuf);
			}
			
			if(predicates != null && predicates.length > 0) {
				query.append(new String(" and PREDICATE in ("));
				for(int i=0; i < predicates.length; i++) {
					query.append("'" + predicates[i] + "',");
					if(negationInclude)
						query.append("'NEG_" + predicates[i] + "',");
				}
				query.setCharAt(query.length() - 1, ')');
			}
		}
		System.out.println("Database Query : " + query.toString());
		return query.toString();
	}
	
	protected String createQuery(Vector concepts, String[] predicates, boolean negationInclude) throws SQLException{

		StringBuffer query = null;

		query = new StringBuffer("SELECT s.PMID, s.TYPE, s.NUMBER, s.SENTENCE, p.PREDICATE, p.SUBJECT_CUI, p.SUBJECT_NAME, p.SUBJECT_SEMTYPE, p.SUBJECT_NOVELTY, p.OBJECT_CUI, p.OBJECT_NAME, p.OBJECT_SEMTYPE, p.OBJECT_NOVELTY " +
				" FROM SENTENCE s, PREDICATION p WHERE s.SENTENCE_ID = p.SENTENCE_ID and ");
		int indexi = 0;
		int indexj = 0;
		query.append(" p.SUBJECT_NAME in (");
		for(Object o: concepts) {
			String s_concept = (String) o;
			query.append("'" + s_concept + "',");
			
		}
		query.delete(query.length()-1, query.length());
		query.append(") and p.OBJECT_NAME in (");
		for(Object o: concepts) {
			String s_concept = (String) o;
			query.append("'" + s_concept + "',");
			
		}
		query.delete(query.length()-1, query.length());
		query.append(")");
		
		if(predicates != null && predicates.length > 0) {
			query.append(new String(" and PREDICATE in ("));
			for(int i=0; i < predicates.length; i++) {
				query.append("'" + predicates[i] + "',");
				if(negationInclude)
					query.append("'NEG_" + predicates[i] + "',");
			}
			query.setCharAt(query.length() - 1, ')');
		}
		System.out.println("Database Query : " + query.toString());

		return query.toString();
	}
	
	protected ArrayList<Predication>   queryExecute(String query) throws SQLException{
		ArrayList<Predication> list = new ArrayList<Predication>();
		if (query!=null){
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(query);
			if(rs != null) {
				while (rs.next()){
					Predication predication = new Predication();
					predication.PREDICATION_ID  = rs.getInt(1);
					predication.PMID = rs.getString(2);
					predication.SENTENCE_ID = rs.getInt(3);
					// predication.SID = rs.getInt(3);
					predication.predicate = rs.getString(4);
					predication.addSubject(rs.getString(5));
					predication.addSubjectSemtype(rs.getString(6));
					predication.addSubjectCUI(rs.getString(7));
					predication.novelSubject =  rs.getInt(8)==1;
					predication.addObject(rs.getString(9));
					predication.addObjectSemtype(rs.getString(10));
					predication.addObjectCUI(rs.getString(11));
					predication.novelObject = rs.getInt(12)==1;
					list.add(predication);
				}
			}
			rs.close();
			s.close();
		}
		return list;
	}
	
	protected ArrayList<SentencePredication> queryExecute(String query, Vector<String> concepts) throws SQLException {
		ArrayList<SentencePredication> list = new ArrayList<SentencePredication>();
		if (query!=null){
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(query);
			int i=0;
			if(rs != null) {
				while (rs.next()){
					SentencePredication predication = new SentencePredication();
					predication.PMID  = rs.getString(1);
					predication.TYPE = rs.getString(2);
					predication.NUMBER = rs.getInt(3);
					predication.SENTENCE = rs.getString(4);
					predication.PREDICATE =  rs.getString(5);
					predication.SUBJECT_CUI =  rs.getString(6);
					predication.SUBJECT_NAME =  rs.getString(7);
					predication.SUBJECT_SEMTYPE =  rs.getString(8);
					predication.SUBJECT_NOVELTY =  rs.getBoolean(9);
					predication.OBJECT_CUI =  rs.getString(10);
					predication.OBJECT_NAME =  rs.getString(11);
					predication.OBJECT_SEMTYPE =  rs.getString(12);
					predication.OBJECT_NOVELTY =  rs.getBoolean(13);
					// log.debug("pred subject = " + subj + ", relation = " + pred + " object = " + obj);
					if(concepts.contains(predication.SUBJECT_NAME) && concepts.contains(predication.OBJECT_NAME)) {
					// log.debug("\t filtered pred subject = " + subj + ", relation = " + pred + " object = " + obj);
						list.add(predication);
					}
				}
			}
		}

		return list;
	}

	

	/* protected String createQuerySent(int[] sentenceids) throws SQLException{
		StringBuffer query = null;

		int addedTotal=0;
		StringBuffer sb = new StringBuffer(QUERY_PREFIX);
		for(int i=0;i<sentenceids.length;i++){
			// StringBuffer sb = queries.get(source[i]);

			int addedIndividual = 0;
			if (sb==null){
				sb = new StringBuffer(QUERY_PREFIX);
			}else
				addedIndividual = 1;

			if (addedIndividual>0)
				sb.append(" OR ");
				sb.append(" s.SENTENCE_ID=");
				sb.append(sentenceids[i]);
				addedTotal++;
		}


		sb.append(") ORDER by PMID");
		return query.toString();
	}

	 protected String createQuery(int[] ids) throws SQLException{

		StringBuffer query = null;
		String connector = " or PMID in ";

		query = new StringBuffer("SELECT PREDICATION_ID, PMID, SENTENCE_ID, PREDICATE, SUBJECT_NAME, SUBJECT_SEMTYPE, SUBJECT_CUI, SUBJECT_NOVELTY, OBJECT_NAME, OBJECT_SEMTYPE, OBJECT_CUI, OBJECT_NOVELTY FROM PREDICATION WHERE PMID in ");

		int added=0;
		for(int i=0;i<ids.length;){
			if (true){//existingDocuments.contains(ids[i])){
				if (added>0)
					query.append(connector);
				query.append("(");
				query.append("\"" + ids[i++] + "\"");
				added++;
			}else{
				i++;
				continue;
			}
			while (added%1500!=0 && i<ids.length){
				//	if (existingDocuments.contains(ids[i])){
				query.append(",");
				query.append("\"" + ids[i++]+ "\"");
				added++;
				//	}else
				//	i++;
			}
			query.append(")");
		}

		if (added==0)
			return null;
		else
			return query.toString();
	} */
	
	


	/* protected ArrayList<APredication> createPredications(ResultSet rs,boolean includeSentence) throws SQLException{
		ArrayList<APredication> list = this;
		if (includeSentence){
			int i=0;
			while (rs.next()){
				APredication predication = new APredication();
				predication.PMID  = rs.getString(1);
				predication.sentence = rs.getString(2);
				predication.addSubject(rs.getString(3));
				predication.predicate = rs.getString(4);
				predication.addObject(rs.getString(5));	

				list.add(predication);
			}
		}else
			while (rs.next()){
				APredication predication = new APredication();
				predication.PREDICATION_ID  = rs.getInt(1);
				predication.PMID = rs.getString(2);
				predication.SENTENCE_ID = rs.getInt(3);
				// predication.SID = rs.getInt(3);
				predication.predicate = rs.getString(4);
				predication.addSubject(rs.getString(5));
				predication.addSubjectSemtype(rs.getString(6));
				predication.addSubjectCUI(rs.getString(7));
				predication.novelSubject =  rs.getInt(8)==1;
				predication.addObject(rs.getString(9));
				predication.addObjectSemtype(rs.getString(10));
				predication.addObjectCUI(rs.getString(11));
				predication.novelObject = rs.getInt(12)==1;
				list.add(predication);
			}
		return list;
	}

	 public List<Long> getPredicationIDs() throws SQLException{
		StringBuffer query = new StringBuffer("SELECT PREDICATION_ID FROM PREDICATION WHERE ");

		int added=0;

		if (added==0)
			return new ArrayList<Long>(0); 

		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(query.toString());

		List<Long> result = new ArrayList<Long>();

		while(rs.next())
			result.add((long)rs.getInt(1));

		rs.close();
		s.close();

		return result;
	} */

	public List<SentencePredication> getSentencePredication() throws SQLException{
		StringBuffer query = new StringBuffer("SELECT p.PREDICATION_ID, p.PMID, s.TYPE, s.NUMBER, s.SENTENCE, p.PREDICATE, " +
				"p.SUBJECT_CUI, p.SUBJECT_NAME, p.SUBJECT_SEMTYPE, p.OBJECT_CUI, p.OBJECT_NAME, p.OBJECT_SEMTYPE FROM PREDICATION as p, SENTENCE as s WHERE (p.SENTENCE_ID = s.SENTENCE_ID) and p.SUBJECT_NAME in ( ");

		int added=0;
		for(Predication predication : this){
			if (added>0)
				query.append(" OR ");
			query.append("p.PREDICATION_ID = ");
			query.append(predication.PREDICATION_ID);
			added++;
		} 
		query.append(")"); 
		

		if (added==0)
			return new ArrayList<SentencePredication>(0); 
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(query.toString());

		List<SentencePredication> result = new ArrayList<SentencePredication>();

		while(rs.next()) {
			SentencePredication sp = new SentencePredication();
			sp.PREDICATION_ID = rs.getInt(1);
			sp.PMID = rs.getString(2);
			sp.TYPE = rs.getString(3);
			sp.NUMBER = rs.getInt(4);
			sp.SENTENCE = rs.getString(5);
			sp.PREDICATE = rs.getString(6);
			sp.SUBJECT_CUI = rs.getString(7);
			sp.SUBJECT_NAME = rs.getString(8);
			sp.SUBJECT_SEMTYPE = rs.getString(9);
			sp.OBJECT_CUI = rs.getString(10);
			sp.OBJECT_NAME = rs.getString(11);
			sp.OBJECT_SEMTYPE = rs.getString(12);			
			result.add(sp);
			}

		rs.close();
		s.close();	
		return result;
	}

}
