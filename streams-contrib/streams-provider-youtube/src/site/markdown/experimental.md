## Experimental Features

To object a youtube export archive, follow these steps:

    *TODO*

Unzip the archive file into a local directory, $DATAROOT.

Define the namespace you want to use for the generated triples, $NAMESPACE.

    export NAMESPACE=http://streams.apache.org/streams-contrib/streams-provider-youtube

Define the id you want to attach the generated triples to, $ID.

    export ID=id

Specify where you want the generated triples to be written, $OUTPUTROOT.

    export OUTPUTROOT=target/generated-resources/youtube/
    
To generate RDF triples from a youtube export archive, follow these steps:

    cd streams-contrib/streams-provider-youtube
    java -cp ../../streams-dist/dist/streams-dist-jar-with-dependencies.jar \
            -DbaseDir=. \
            -DsettingsFile=../../streams-cli/src/main/resources/default.fmpp \
            -DsourceRoot=src/main/templates \
            -DdataRoot=$DATAROOT \
            -DoutputRoot=$OUTPUTROOT \
            -Dnamespace=$NAMESPACE \
            -Did=$ID \
            org.apache.streams.cli.RdfFreemarkerCli
                
###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
