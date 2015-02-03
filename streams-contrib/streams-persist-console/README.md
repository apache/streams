streams-persist-console
=====================

Read and write documents from console

This can also be used to read and write files.

For example, to read a stream from a file of json documents:

    cat inputfile.txt | java -jar stream.jar

For example, to write a stream to a file of json documents:

    java -jar stream.jar > outputfile.txt
    
NOTE: Be sure to disable the console logger in the latter case.