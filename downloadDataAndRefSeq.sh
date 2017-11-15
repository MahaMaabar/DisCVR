#!/bin/bash

execute_command (){
#string variables
line="$1"
DIRNAME="$2"
index="$3"

#create a directory to hold refSeq data
#make directory, if not existing
refSeqDir="$DIRNAME/RefSeq/"
if [ ! -d $refSeqDir ]; then
    mkdir -p $refSeqDir;
fi;

#create a directory to hold sequences data
#make directory, if not existing
dataSeqDir="$DIRNAME/DataSeq/"
if [ ! -d $dataSeqDir ]; then
    mkdir -p $dataSeqDir;
fi;


#String to hold awk command for formatting output file
formatOutput="| awk -v ORS= '/^>/ { "\$0" = (NR==1 ? \"\" : RS) "\$0" RS } END { printf RS}1'"
#echo "FORMATTING_COMMAND:$formatOutput";

#String to get the Title, Update Date, Seq Length, and TaxaID
tags="| esummary |grep 'Caption\|Title\|TaxId\|Slen\|UpdateDate\|SubType\|SubName\|Strain\|Extra'|sed -e 's/<[^>]*>//g'"


#using awk to get taxID
taxID=$(echo $line | awk -F "\t" '{print $1;}')
echo "TaxID($index): $taxID";
   
refSeqInfo=$(echo $line | awk -F "\t" '{print $2;}')
echo "refSeqInfo($index): $refSeqInfo";


#==========================================================
#First: download the reference sequence(s) for the virus 
#from the accession numbers and put it in a file named with #the taxID
#==========================================================
#get the accList into an array
IFS=',' read -a accList <<<$refSeqInfo

accListSize=${#accList[@]} 
echo "Number of accession Numbers for taxID[" "$taxID" "]:" "$accListSize"


if [[ accListSize -gt 0 ]]
then
    #String to hold RefSeq Info file name
    refSeqInfoFile=$(printf "%s%s%s%s" "$refSeqDir" "/Virus_" "$taxID" "_RefSeq_Info")
    #echo "REF_SEQ_INFO: $refSeqInfoFile"
    
    #create an empty file if it doesn't currently exist, or if it does exist, it will truncate it to zero length 
    cp /dev/null  $refSeqInfoFile
    
    
    #print the number of ref seq to the file
    echo "$accListSize" > "$refSeqInfoFile" 
    
    refSeqFileName=$(printf "%s%s%s%s" "$refSeqDir" "/Virus_" "$taxID" "_RefSeq" ".fa")
    #echo "RefSeq_FILE: $refSeqFileName"
    
    #create an empty file if it doesn't currently exist, or if it does exist, it will truncate it to zero length
    cp /dev/null "$refSeqFileName"
    

    for (( n = 0; n < $accListSize; n++));
    do
        echo "AccNum[$n]: ${accList[n]} "
        refSeQuery=$(printf "%s%s%s" "\""${accList[n]}"\"")
        echo "REF_SEQ_QUERY: $refSeQuery"
        refSeqOutput=""$formatOutput" >> "$refSeqFileName" "
        #command to download Ref Seq into output file
        command0=$(printf "%s %s %s %s %s" "esearch -db nucleotide " "-query" "$refSeQuery" "| efetch -format fasta" "$refSeqOutput")
     
       #echo "RefSeq_COMMAND:$command0"
       eval "$command0" 

       #command to write headers into output file
command1=$(printf "%s%s%s%s%s" "esearch -db nucleotide " "-query " "$refSeQuery" "$tags"  " >> " "$refSeqInfoFile" )
     #echo "RefSeq_INFO_COMMAND:$command1"
     eval "$command1" 
    done

else
    echo "No Reference Sequences found for taxaID: " "$taxID" 
fi

#==========================================================
#Second: download the sequences data for the virus 
#from the taxID and put it in a file named with the taxID
#Also, download information for the sequences in a separate 
#info file
#==========================================================
#String to hold the query to download the sequences
seqQuery=$(printf "%s%s%s" "\"txid" "$taxID" "[Organism]\"")
#echo "SEQ_QUERY: $seqQuery"

#String to hold Sequences Info file name

seqInfoFile=$(printf "%s%s%s%s" "$dataSeqDir" "/Virus_" "$taxID" "_Info")
#seqInfoFile=$(printf "%s%s%s%s" "$DIRNAME" "/Virus_" "$taxID" "_Info")
#echo "SEQ_INFO: $seqInfoFile"

#String to hold data sequences file name
seqFileName=$(printf "%s%s%s%s" "$dataSeqDir" "/Virus_" "$taxID" ".fa")
#seqFileName=$(printf "%s%s%s%s" "$DIRNAME" "/Virus_" "$taxID" ".fa")
#echo "SEQ_FILE: $seqFileName"

seqOutput=""$formatOutput" >> "$seqFileName" "

#==========================================================
#create commands
#==========================================================

#command to find number of seqs for the taxaID
seqCommand=$(printf "%s %s %s %s %s" "esearch -db nucleotide " "-query" "$seqQuery" "|grep Count" " |sed -e 's/<[^>]*>//g' ")

#echo "SEQ_COMMAND:$seqCommand"

#command to write headers into output file
command2=$(printf "%s%s%s%s%s" "esearch -db nucleotide " "-query " "$seqQuery" "$tags"  " >> " "$seqInfoFile" )

#echo "COMMAND2:$command2";

#command to download sequence into output file
command3=$(printf "%s %s %s %s %s" "esearch -db nucleotide " "-query" "$seqQuery" "| efetch -format fasta" "$seqOutput" )

#echo "COMMAND3:$command3";

#==========================================================
#execute the commands
#==========================================================

seqNum=$(eval "$seqCommand" )

#if taxaID has one or more seqs then download them into a #file, otherwise do nothing
if [[ $seqNum -gt 0 ]]
then
    echo "$seqNum Sequences found for taxaID: "     "$taxID" 
    #create an empty file if it doesn't currently exist, or if it does exist, it will truncate it to zero length 
    cp /dev/null  "$seqInfoFile"
    #touch "$seqInfoFile"
    echo "$seqNum" > "$seqInfoFile" 

    #printf "%s %s %s %s" "Sequences Count: " "$seqNum" " > "    "$seqInfoFile" 
    #create an empty file if it doesn't currently exist, or if it does exist, it will truncate it to zero length 
    cp /dev/null  "$seqFileName"
    #touch "$seqFileName"
    #echo "COMMAND2:$command2"
    eval "$command2" 
    #echo "COMMAND3:$command3";
    eval "$command3" 

else
    echo "No Sequences found for taxaID: " "$taxID" 
fi


}



#file to read
FILE="$1"
#output directory
DIRNAME="$2"

#make directory, if not existing
if [ ! -d $2 ]; then
    mkdir -p $2;
fi;

#delimiter
D="\t"
IFS=$'\n'
echo "################################"
k=0
#ensure file is unix compatible 
dos2unix $FILE
while read -r line;
do
   
   #echo "Line # $k: $line";

   #using awk to get taxaID
   taxID=$(echo $line | awk -F "\t" '{print $1;}')
   echo "TaxID($k): $taxID";
   
   accList=$(echo $line | awk -F "\t" '{print $2;}')
   echo "accList($k): $accList";
      
   #get a list of all taxaIDs in the file
   fileLines[$k]=$line
         
   ((k++))
done < $FILE
echo "Total number of lines in file: $k"

for (( i = 0; i< ${#fileLines[@]}; i++)); do
     echo "Line to pass[$i]: ${fileLines[i]} "
        
     #download the data using taxaID
     execute_command ${fileLines[i]} $DIRNAME $i
     
done
