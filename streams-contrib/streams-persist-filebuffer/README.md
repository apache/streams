streams-persist-file
=====================

Read to / write from File-backed Queue

Example reader/writer configuration:

    file {
        path = "/tmp/file-queue.txt"
    }
    
Reader will consume lines from Writer