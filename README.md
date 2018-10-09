DisCVR is a user-friendly tool for detecting known viruses in clinical samples. 
It works by creating a viral database of _k_-mers (22 nucleotide sequences) from a set 
of known sequence. These _k_-mers are taxonomically labelled according to the viruses 
from which they originated. Each read in the sample is then screened for the presence 
of _k_-mers in the viral database, and a list of all viruses with _k_-mers found in the 
sample is shown. DisCVR accurately detects known human viruses from HTS data using 
computers with limited resources, and includes a graphical user interface to make the 
interpretation and validation of results easy. At present, DisCVR is a human viral 
diagnostic tool, but it can be readily extended to include non-viral human pathogens 
and pathogens of other hosts using the scripts provided to build customised databases. 


The source code for DisCVR can be downloaded from [https://github.com/centre-for-virus-research/DisCVR](https://github.com/centre-for-virus-research/DisCVR) 

Compiled versions with the built-in databases are available from [http://bioinformatics.cvr.ac.uk/discvr.php](http://bioinformatics.cvr.ac.uk/discvr.php)
The download includes information for installing and running the tool. 

For more information on how to run DisCVR, here is the [manual](https://centre-for-virus-research.github.io/DisCVR/manual)

DisCVR was developed by [Maha Maabar](https://github.com/MahaMaabar)

