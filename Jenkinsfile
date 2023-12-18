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

timestamps {

	node ('ubuntu') {

		stage ('Streams - Checkout') {
			checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '', url: 'https://github.com/apache/streams']]])
		}

		stage ('Streams - Build') {

            def JAVA_JDK_17=tool name: 'jdk_17_latest', type: 'hudson.model.JDK'
            def MAVEN_3_LATEST=tool name: 'maven_3_latest', type: 'hudson.tasks.Maven$MavenInstallation'

			withEnv(["Path+JDK=$JAVA_JDK_17/bin","Path+MAVEN=$MAVEN_3_LATEST/bin","JAVA_HOME=$JAVA_JDK_17"]) {
                sh "mvn -P 'java-17' clean install"
			}
		}

		stage ('Streams - Post build actions') {
			step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: 'dev@streams.apache.org', sendToIndividuals: true])
		}
	}
}
