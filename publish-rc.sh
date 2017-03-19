#!/bin/bash

REL=$1
REP=$2

if [[ -z "$REL" || -z "$REP" ]]; then
    echo "You must specify a release version and a staging repo name"
    exit 1
fi


mkdir -p dist
cd dist
svn co https://dist.apache.org/repos/dist/dev/incubator/streams dev
cd dev
mkdir $REL
cd $REL
wget https://repository.apache.org/content/repositories/$REP/org/apache/streams/streams-project/$REL/streams-project-$REL-source-release.zip
wget https://repository.apache.org/content/repositories/$REP/org/apache/streams/streams-project/$REL/streams-project-$REL-source-release.zip.md5
wget https://repository.apache.org/content/repositories/$REP/org/apache/streams/streams-project/$REL/streams-project-$REL-source-release.zip.sha1
wget https://repository.apache.org/content/repositories/$REP/org/apache/streams/streams-project/$REL/streams-project-$REL-source-release.zip.asc

wget https://repository.apache.org/content/repositories/$REP/org/apache/streams/streams-examples/$REL/streams-examples-$REL-source-release.zip
wget https://repository.apache.org/content/repositories/$REP/org/apache/streams/streams-examples/$REL/streams-examples-$REL-source-release.zip.md5
wget https://repository.apache.org/content/repositories/$REP/org/apache/streams/streams-examples/$REL/streams-examples-$REL-source-release.zip.sha1
wget https://repository.apache.org/content/repositories/$REP/org/apache/streams/streams-examples/$REL/streams-examples-$REL-source-release.zip.asc

svn add .*
svn commit -m "Publishing $REL RC for VOTE"
cd ../..