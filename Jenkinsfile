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
        MAVEN_DEPLOY_LOCAL_DIR = "/tmp/maven_deploy_dir"
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
                script {
                    withCredentials([usernamePassword(credentialsId: env.MAVEN_REPO_CREDS_ID, usernameVariable: 'REPOSITORY_USER', passwordVariable: 'REPOSITORY_TOKEN')]) {
                        configFileProvider([configFile(fileId: env.MAVEN_SETTINGS_CONFIG_FILE_ID, variable: 'MAVEN_SETTINGS_FILE')]) {
                            getMavenCommand()
                                .withSettingsXmlFile(MAVEN_SETTINGS_FILE)
                                .withProperty('wagon.source', env.MAVEN_DEPLOY_LOCAL_DIR)
                                .withProperty('wagon.target', env.MAVEN_DEPLOY_REPOSITORY)
                                .withProperty('wagon.targetId', 'apache-snapshots')
                                .withProperty('apache.snapshot.repository.username', REPOSITORY_USER)
                                .withProperty('apache.snapshot.repository.password', REPOSITORY_TOKEN)
                                .run("org.codehaus.mojo:wagon-maven-plugin:2.0.2:merge-maven-repos")
                        }
                    }
                }
            }
        }

		stage ('Notify') {
		    steps {
			    step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: 'dev@streams.apache.org', sendToIndividuals: true])
            }
		}
	}
}
