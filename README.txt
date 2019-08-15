README of Spreading Activation program
Dongwook Shin, National Library of Medicine
    
(1) Download SemMed database
    a. Visit SemMed database Web page at https://skr3.nlm.nih.gov/SemMedDB/index.html and click download link
    b. Download the database SemMedVER30_R and import it on your MySQL database

(2) Configuring properties
    a. There is a Java property file "config.properties" in the home directory
    b. Edit the file as needed. Here is a sample configuration file
    	connectionString=jdbc:mysql://indsrv2.nlm.nih.gov
	database=semmedVER30
	username=myuser
	password=*********
	motherNodes=30
	daughterNodes=6
	distance=1 
	repetition=2
	ranking=FrequencyHigh
	semtype=aapp,gngm
	semgroup=CHEM
	predicate=INTERACTS_WITH,STIMULATES,AFFECTS
	includeNegation=y

     c. Here are the descriptions of each configuration 
        - connectionString: database connection URL string where MySQL database is installed
        - database: the semmed database name
        - user name: user name of the database
        - password: password of the database
        - motherNodes: the maximun number of intermediary nodes to be generated in the first phase (mother graph) (default is 40). 
          Mother graph nodes always incude two initial seed nodes that are given as command line arguments. So when the number of mother nodes is given 40, the actual number of nodes in mother graph is 42.
        - daughterNodes: the number of nodes to be generated in the second phase (daughter graph) (default is 5)
          Daughter graph nodes always incude two seed nodes that are given as command line arguments. So when the number of mother nodes is given 5, the actual number of nodes in daughter graph is 7.
        - daughterGraphs: the number of daughter graphs automatically generated from a givin mother graph. Daughter nodes are selected randomly from the nodes in the mother graph.
        - distance: the maximum distance of the intermediary nodes from the initial seed nodes (Two initial nodes need to be given as command line arguments) (default is 1)
        - repetition: the number of iterations of the spreading activation algorithm (default is 3)
        - ranking: the criteria with which intermediary nodes of interest in the mother graph are selected. There are ranking options:
        	- Random: nodes of interest in mother graph are selected randomly
        	- FrequencyLow: nodes in mother graph are chosen in the ascending order of the degree of the node
        	- FrequencyHigh: nodes in mother graph are chosen in the descending order of the degree of the node
        - semtype: the comma-separated list of semantic types of the nodes that are included in the mother graph 
        - semgroup: the comma-separated list of semantic groups of the nodes that are included in the mother graph. Information of each semantic group is defined in the file resources/typedefs.xml.
		Note that semgroup option has precedence over semtype. Therefore, if the semgroup option is provided, semtype information is ignored. 
	- predicate: the comma-separated list of predicates that are included in the mother graph and daughter graphs
	- includeNegation: 'y' in this attribute indicates that negation predicates are also included in the predication search. For instance, if perdicate includes "INTERACTS_WITH" and "STIMULATES" and includeNegation is "y",
		the "NEG_INTERACTS_WITH" and "NEG_STIMULATES" are aso included in the predicate as well as "INTERACTS_WITH" and "STIMULATES"


    
(3) Build the jar file
    a. go to the home directory of Spreading Activation
    b. Compile Java files in src directory using the following command
    % ant compile
    C. Ccreate a jar file "sa.jar" by running the following command 
    % ant jar
 
(4) Run the Spreading Activation algorithm using the folling command:

     % java  -cp .:./lib/mysql-connector-java-5.1.18-bin.jar:./lib/jdom.jar:./lib/sa.jar gov.nih.nlm.skr.SA.SpreadingActivation NAME Melatonin Serotonin outdrug.xml
     
     - As the algorithm uses significant memory space, it is recommended to give enough heap space to run the algorithm
     - The three jar files the algorithm needs, MySQL jar file, JDOM jar file and sa.jar should be given in the class path. MySQL jar file and JDOM jar file are already provided in lib directory
     - The algorithm accepts four arguments from the command line.
     - The first argument of the algorithm indicates if the second and third arguments are provided as as UMLS preferred names or UMLS concept identifiers (CUIs). If it is given as "NAME", it means that the following two arguments are taken as preferred names. If it is "CUI", it means the following two are taken as CUIs. 
     - The second and third arguments are two seed nodes with which the spreading activation algorithm starts.
     - The fourth argument is the name of the output file where the daughter graph is generated in XML format. If the fourth argument is missing, the daughter graph is generated as standard output.
     - The Spreading Activation algorithm generates the mother graph first, which displays each concept in a separate line.
     - From these nodes in the mother graph, the program randomly chooses daughter nodes and generates daughter graphs in the following XML format. 
     		- Each daughter graph is included by the tag "<graph>", which is enclosed by the topmost tag "<daughterGraphs>".
     		- Each concept in a daughter graph is represented as a tag "<node>" with attributes corresponding to its CUI, preferred name and semantic type.
     		- Each edge is represented as a tag "<edge>" with attributes corresponding to subject, predicate and object.
     		- Each edge is associated with sentences that generate the predication.
