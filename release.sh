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

usage() {
echo "Usage: $0 "
echo "[-v RELEASE_VERSION (required) (e.g. 1.0.0)]"
echo "[-d DEVELOPMENT_VERSION (required) (e.g. 1.0.1)]"
echo "[-r RELEASE_CANDIDATE (default: 1)]"
1>&2; exit 1;
}

while getopts ":v:d:r:" OPTION; do
    case $OPTION in
        v) export RELEASE_VERSION=${OPTARG};;
        d) export DEVELOPMENT_VERSION=${OPTARG};;
        r) export RELEASE_CANDIDATE=${OPTARG};;
        \?) usage; exit 1;;
    esac
done

if [ -z "${RELEASE_VERSION}" ]; then
  echo "RELEASE_VERSION (-v) is not set!";
  usage
  exit 1;
fi
if [ -z "${DEVELOPMENT_VERSION}" ]; then
  echo "DEVELOPMENT_VERSION (-d) is not set!";
  exit 1;
fi
if [ -z "${RELEASE_CANDIDATE}" ]; then
  echo "RELEASE_CANDIDATE (-r) is not set";
  echo "Using default value '1'";
  exit 1;
fi

if [ -z `which docker` ]; then
  echo "docker is not configured!";
  exit 1;
fi
if [ -z `which gpg` ]; then
  echo "gpg is not configured!";
  exit 1;
fi
if [ -z `which mvn` ]; then
  echo "mvn is not configured!";
  exit 1;
fi

mkdir -p /tmp/streams_release_repository
rm -rf /tmp/streams_release_repository/*
mkdir -p /tmp/streams_release_source
rm -rf /tmp/streams_release_source/*
mkdir -p /tmp/streams_release_logs
rm -rf /tmp/streams_release_logs/*

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
        echo "Release failed!"
        echo "Check $output for more details."
        exit 1;
    fi
}

#streams
git clone https://github.com/apache/streams.git /tmp/streams_release_source
cd /tmp/streams_release_source

printInfo

set -x

mvn -Pcheck \
  apache-rat:check \
  -Drat.excludeSubprojects=false \
  -e -DskipTests=true \
  | tee /tmp/streams_release_logs/streams_ratcheck.txt
checkStatus /tmp/streams_release_logs/streams_ratcheck.txt

mvn clean test \
  | tee /tmp/streams_release_logs/streams_unittests.txt
checkStatus /tmp/streams_release_logs/streams_unittests.txt

mvn -Papache-release \
  release:prepare \
  -DpushChanges=false \
  -DautoVersionSubmodules=true \
  -DreleaseVersion=${RELEASE_VERSION} \
  -DdevelopmentVersion=${DEVELOPMENT_VERSION}-SNAPSHOT \
  -Dtag=apache-streams-${RELEASE_VERSION}-rc${RELEASE_CANDIDATE} \
  | tee /tmp/streams_release_logs/streams_release-prepare.txt
checkStatus /tmp/streams_release_logs/streams_release-prepare.txt

mvn -Papache-release \
  clean install release:perform \
  -Darguments='-Dmaven.test.skip.exec=true' \
  -Dgoals=deploy \
  -DlocalRepoDirectory=. \
  -DlocalCheckout=true \
  | tee /tmp/streams_release_logs/streams_release-perform.txt
checkStatus /tmp/streams_release_logs/streams_release-perform.txt

git push origin master
git push origin apache-streams-$REL

cd -

cat << EOM
##################################################################

                    RELEASE COMPLETE

##################################################################
EOM