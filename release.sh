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

#incubator-streams
git clone https://git-wip-us.apache.org/repos/asf/incubator-streams.git ./incubator-streams-$REL
cd incubator-streams-$REL

printInfo

mvn clean verify $REPO > ../logs/incubator-streams_unittests.txt
checkStatus ../logs/incubator-streams_unittests.txt

mvn $REPO clean apache-rat:check -e -DskipTests=true  > ../logs/incubator-streams-project_apache-rat_check.txt
checkStatus ../logs/incubator-streams-project_apache-rat_check.txt

cp ../streams-c84fa47bd759.p12 .
cp ../application.conf .
sed -i '' "s#<WORK>#$(pwd)#g" application.conf
cat application.conf

mvn -PdockerITs $REPO docker:start > ../logs/incubator-streams_docker-start.txt
checkStatus ../logs/incubator-streams_docker-start.txt

sleep 30
docker ps
head *.properties

mvn clean verify $REPO -DskipTests=true -DskipITs=false -DargLine="-Dconfig.file=`pwd`/application.conf" > ../logs/incubator-streams_integrationtests.txt
checkStatus ../logs/incubator-streams_integrationtests.txt

mvn -PdockerITs $REPO docker:stop > ../logs/incubator-streams_docker-stop.txt
checkStatus ../logs/incubator-streams_docker-stop.txt

mvn -Papache-release $REPO release:prepare -DpushChanges=false -DautoVersionSubmodules=true -DreleaseVersion=$REL -DdevelopmentVersion=$DEV-SNAPSHOT -Dtag=streams-project-$REL > ../logs/incubator-streams-project_release-prepare.txt
checkStatus ../logs/incubator-streams-project_release-prepare.txt

mvn -Papache-release $REPO clean install release:perform -Darguments='-Dmaven.test.skip.exec=true' -Dgoals=deploy -DlocalRepoDirectory=. -DlocalCheckout=true > ../logs/incubator-streams-project_release-perform.txt
checkStatus ../logs/incubator-streams-project_release-perform.txt

cd ..

#streams-examples
git clone https://git-wip-us.apache.org/repos/asf/incubator-streams-examples.git ./incubator-streams-examples-$REL
cd incubator-streams-examples-$REL

printInfo

mvn $REPO clean apache-rat:check -e -DskipTests > ../logs/incubator-streams-examples_apache-rat_check.txt
checkStatus ../logs/incubator-streams-examples_apache-rat_check.txt

mvn $REPO clean verify > ../logs/incubator-streams-examples_unittests.txt
checkStatus ../logs/incubator-streams-examples_unittests.txt

cp ../streams-c84fa47bd759.p12 .
cp ../application.conf .
sed -i '' "s#<WORK>#$(pwd)#g" application.conf

mvn $REPO -PdockerITs -N docker:start > ../logs/incubator-streams-examples_docker-start.txt
checkStatus ../logs/incubator-streams-examples_docker-start.txt

sleep 30
docker ps
head *.properties

mvn $REPO clean verify -DskipTests=true -DskipITs=false -DargLine="-Dconfig.file=`pwd`/application.conf" > ../logs/incubator-streams-examples_integrationtests.txt
checkStatus ../logs/incubator-streams-examples_integrationtests.txt

mvn $REPO -Papache-release release:prepare -DpushChanges=false -DautoVersionSubmodules=true -DreleaseVersion=$REL -DdevelopmentVersion=$DEV-SNAPSHOT -Dtag=streams-examples-$REL > ../logs/incubator-streams-examples_release-prepare.txt
checkStatus ../logs/incubator-streams-examples_release-prepare.txt

mvn $REPO -Papache-release clean install release:perform -Darguments='-Dmaven.test.skip.exec=true' -Dgoals=deploy -DlocalRepoDirectory=. -DlocalCheckout=true > ../logs/incubator-streams-examples_release-perform.txt
checkStatus ../logs/incubator-streams-examples_release-perform.txt

git push origin master
git push origin streams-examples-$REL

cd ../incubator-streams-$REL
git push origin master
git push origin streams-project-$REL

cd..

cat << EOM
##################################################################

                    RELEASE COMPLETE

##################################################################
EOM