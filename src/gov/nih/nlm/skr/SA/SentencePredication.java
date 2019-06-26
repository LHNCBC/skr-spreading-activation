package gov.nih.nlm.skr.SA;

public class SentencePredication {

	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/* This part come from Sentence 
		 * 
		 */
		public String PMID;
		public String TYPE;
		public int NUMBER;
		public String SENTENCE;
		
		/*
		 * This paer come from predication
		 */
		public int PREDICATION_ID;
		public String PREDICATE;
		public String SUBJECT_CUI;
		public String SUBJECT_NAME;
		public String SUBJECT_SEMTYPE;
		public boolean SUBJECT_NOVELTY;
		public String OBJECT_CUI;
		public String OBJECT_NAME;
		public String OBJECT_SEMTYPE;
		public boolean OBJECT_NOVELTY;
		
		/**
	     * Simple constructor of SentencePredication instances.
	     */
	    public SentencePredication()
	    {
	    }
	    
	    public Predication getPredication() {
	    	Predication p = new Predication();
	    	p.PMID = this.PMID;
	    	p.addSubjectCUI(this.SUBJECT_CUI);
	    	p.addSubjectSemtype(this.SUBJECT_SEMTYPE);
	    	p.addSubject(this.SUBJECT_NAME);
	    	p.novelSubject = this.SUBJECT_NOVELTY;
	    	p.predicate = this.PREDICATE;
	    	p.addObjectCUI(this.OBJECT_CUI);
	    	p.addObjectSemtype(this.OBJECT_SEMTYPE);
	    	p.addObject(this.OBJECT_NAME);
	    	p.novelObject = this.OBJECT_NOVELTY;
	    	return p;
	    		    			
	    }
	    
	    public Sentence getSentence() {
	    	Sentence s = new Sentence();
	    	s.PMID = this.PMID;
	    	s.TYPE = this.TYPE;
	    	s.NUMBER = this.NUMBER;
	    	s.SENTENCE = this.SENTENCE;
	    	return s;
	    }

	   
	    /* Add customized code below */
	     public String toString() {
	    	
	    	return this.PMID + "." + this.TYPE + "." + this.NUMBER + ": " + 
	    			//this.getPredication().toString() + " " + 
	    			this.SUBJECT_NAME + "-" + this.PREDICATE + "-" + this.OBJECT_NAME + ":" + this.SENTENCE   /* +
	    			 this.getPredicationNumber().toString() */;
	    } 
}
