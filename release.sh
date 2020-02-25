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
DEV=$2
REPO="-Dmaven.repo.local=/tmp/streams_release"

if [[ -z "$REL" || -z "$DEV" ]]; then
    echo "You must specify a release and new dev version"
    echo "Useage: ./release.sh <new_version> <new_dev_version>"
    echo "Example: ./release.sh 0.5.1 0.6.0-SNAPSHOT"
    exit 1
fi

mkdir -p /tmp/streams_release
mkdir -p logs

printInfo() {
    echo "\n"
    mvn -v
    echo "\n"
    docker -v
    echo "\n"
    docker info
    echo "\n\n"
    git status
    git log | head
}

checkStatus() {
    local output=$1
    if [[ -z "$(tail $output | egrep 'BUILD SUCCESS')" ]]; then
        echo "Release failed"
        exit 1
    fi
}

#streams
git clone https://github.com/apache/streams.git ./streams-$REL
cd streams-$REL

printInfo

mvn -Pcheck apache-rat:check -e -DskipTests=true -Drat.excludeSubprojects=false $REPO  > ../logs/streams_ratcheck.txt
checkStatus ../logs/streams_ratcheck.txt

mvn clean test $REPO > ../logs/streams_unittests.txt
checkStatus ../logs/streams_unittests.txt

mvn -Papache-release $REPO release:prepare -DpushChanges=false -DautoVersionSubmodules=true -DreleaseVersion=$REL -DdevelopmentVersion=$DEV-SNAPSHOT -Dtag=streams-project-$REL > ../logs/streams_release-prepare.txt
checkStatus ../logs/streams_release-prepare.txt

mvn -Papache-release $REPO clean install release:perform -Darguments='-Dmaven.test.skip.exec=true' -Dgoals=deploy -DlocalRepoDirectory=. -DlocalCheckout=true > ../logs/streams_release-perform.txt
checkStatus ../logs/streams_release-perform.txt

git push origin master
git push origin streams-project-$REL

cd..

cat << EOM
##################################################################

                    RELEASE COMPLETE

##################################################################
EOM