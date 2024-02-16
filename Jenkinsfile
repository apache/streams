// ***************************************************************************************************************************
// * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file *
// * distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file        *
// * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance            *
// * with the License.  You may obtain a copy of the License at                                                              *
// *                                                                                                                         *
// *  http://www.apache.org/licenses/LICENSE-2.0                                                                             *
// *                                                                                                                         *
// * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an  *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the        *
// * specific language governing permissions and limitations under the License.                                              *
// ***************************************************************************************************************************

def AGENT_LABEL = env.AGENT_LABEL ?: 'ubuntu'
def JDK_NAME = env.JDK_NAME ?: 'jdk_17_latest'
def MVN_NAME = env.MVN_NAME ?: 'maven_3_latest'

pipeline {

	agent {
        node {
            label AGENT_LABEL
        }
    }

    tools {
        maven MVN_NAME
        jdk JDK_NAME
    }

    environment {
        LANG = 'C.UTF-8'
        MAVEN_CLI_OPTS = "--batch-mode --errors --fail-at-end --show-version --no-transfer-progress"
    }

    stages {

		stage ('Build') {
            steps {
			    sh "mvn ${MAVEN_CLI_OPTS} -P 'java-17' -Dmaven.test.skip.exec=true clean install"
			}
			post {
                success {
                    archiveArtifacts '**/target/*.jar'
                }
            }
		}

        stage ('Test') {
            steps {
			    sh "mvn ${MAVEN_CLI_OPTS} -P 'java-17' verify"
			}
			post {
                always {
                    junit testResults: '**/target/surefire-reports/TEST-*.xml'
                }
            }
		}

        stage('Deploy') {
            when {
                branch 'snapshots'
            }
            steps {
                // Use release profile defined in project pom.xml
                sh "mvn ${MAVEN_CLI_OPTS} -Dmaven.test.skip.exec=true deploy"
            }
        }

		stage ('Notify') {
			step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: 'dev@streams.apache.org', sendToIndividuals: true])
		}
	}
}
