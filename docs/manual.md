## Introduction
DisCVR: Rapid Viral Diagnostic Tool
DisCVR is a viral detection tool which allows the identification of known human viruses in clinical samples from high-throughput sequencing (HTS) data. It uses the k-mers approach in which the sample reads are decomposed into k-mers and then matched against a virus k-mers database. The built-in database is a list consists of all k-mers (k=22) that are not low-complexity and only found in the viral genomes but not in the human genome. Each k-mer is assigned the taxonomic label of all viral genomes that contain that k-mer in the NCBI taxonomy tree. These assignments are made at the species and strains taxonomic level.
DisCVR has a user-friendly Graphical User Interface (GUI) which runs the analysis on the sample and shows the results interactively. It enables the visualisation of the coverage of the virus genomes found in the sample in order to validate the significance of the results. In addition, DisCVR is a generic tool which can be used with other non-human viruses by facilitating the build and use of customised k- mers database.
DisCVR is designed to run on machines with low processing capacity and small memory.

## System Requirements
* Disk Space: DisCVR.jar requires ~700MB space for installation with the built-in databases. It is recommended to have space for 2x sample size when using DisCVR for classification as the process involves writing temporary files to disk. When building a customised database, the amount of space depends on the size of the viral sequences and the k size. For example, extracting k-mers of size 32 from the human genomes generates a file that is 80GB in size. If the viral data sequences are 3GB in size, then the minimum disk space needed to build a customised database is 200GB. 
* Memory: DisCVR runs efficiently on a machine with 4GB RAM, which is the current standard for PCs. It is much faster on machines with higher RAM such as 8GB RAM. However, the amount of RAM depends on the number of the sequences used and the size of the k-mer. The larger the dataset and the k-mer, the larger the amount of RAM needed. Therefore, in the case of "out of memory" errors, the Java heap space should be increased. 

## Installation
* Operating System: DisCVR runs on both Windows and Linux platforms. To use DisCVR, the users need first to download the appropriate folder for their operating system. 
* Java: Java (1.8 or above) must be installed and the full path to the jre\bin folder should be included in the system variables. Java can be downloaded from: http://www.oracle.com/technetwork/java/javase/downloads/jre8- downloads-2133155.html 
* DisCVR.jar: After downloading the DisCVR zipped folder, it is recommended to use a tool, such as 7-zip, to unzip the Windows OS version and extract all files to a local directory. For Linux and Mac version, open a command prompt and move to the location of the zipped folder. Type the following commands to unzip the folder:
   `tar -xzvf DisCVR_Linux.tar.gz`
This creates a folder, called DisCVR. The contents of DisCVR consists of one jar: DisCVR.jar and a lib folder which are used to run the classification. The script file: downloadDataAndRefSeq.sh and the folders: bin, customisedDB, and TestData which are needed to build a customised database. 

**IMPORTANT:** The full path to DisCVR directory must NOT contain space nor the dot "." to avoid conflict with the files naming during the classification process.
* Dependencies: DisCVR uses external libraries such as KAnalyze, for k-mers counting, and
JFreechart packages, for graphs plotting. It makes use of Tanoti, a Blast-based tool for reference assembly. These are 10 files in total and they are in the lib folder. It is important not to alter the lib folder or its contents and to ensure that it is in the same path as the jar file.

* If you want to build a customised database, the following NCBI tools and files must be downloaded and installed:
  * The NCBI eutilities tools are used to download data. The tools can be found at: ([ftp://ftp.ncbi.nlm.nih.gov/entrez/entrezdirect/](ftp://ftp.ncbi.nlm.nih.gov/entrez/entrezdirect/)). The full path to the edirect folder should be added to the system variables
  * The NCBI taxdump files are used for taxonomy information retrieval when building a customised database. The file can be downloaded from ([ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/](ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/)). The file taxdump.tar.gz should be downloaded and unzipped. The two files: names.dmp and nodes.dmp MUST be copied to the customised database folder: customisedDB which is in the same path as DisCVR.jar.

* To test if the tools are installed properly, open a command prompt and type the following:
  * To know what Java version is installed: `java –version``
    * This should state java version 1.8.0_<some number>
  * To see if Java is added to the path: java
    * If the jre\bin is not added to the path, you will see the following message: "java is not
recognized as an internal or external command, operable program or batch file"". 
  * To see if eutilities tools is added to the path: esearch
    * This should state “Must supply -db database on command line”