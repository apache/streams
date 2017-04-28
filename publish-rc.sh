#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

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