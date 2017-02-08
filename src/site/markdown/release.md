##Release Process

There are two distinct sets of artifacts that are released on independent schedules:  streams-master & streams-project.  The streams-master is the project metadata and only needs to be released when there is a change in the structure of the project itself.  The streams-project artifacts are comprised of all streams source code, binaries and a standalone demo.  For release setup information, refer to [Release Setup Information](/release-setup.html).

All of the steps below apply to all streams repository releases, unless otherwise specified.  As an alternative to releasing separately, the projects MAY be released together as one combined release.  The steps for this can be found below. ([Combined Release Steps](#combined))

NOTE:

Releases should always be built and published in the following order:

* streams-master
* streams-project
* streams-examples

In the instructions below, ${project.name} should be one of these top-level repository aggregator pom project names.

As an alternative to releasing separately, the projects MAY be released together as one combined release. The steps for this can be found below. (Combined Release Steps)

###Common Release Steps

1. Environment setup for releasing artifacts (same for SNAPSHOTs and releases)    

    1. Increase the default Java heap available to Maven (required for Java SE 6)   

            export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"

    2. Use the latest Sun 1.8.x JDK
    3. Use Maven 3.3.9 or later
    4. Make sure the [Release Setup](release-setup.html) steps have been performed.

2. Prepare the source for release:

     1. Cleanup JIRA so the Fix Version in issues resolved since the last release includes this release version correctly.
     2. Update the text files in a working copy of the project root -
         1. Update the CHANGELOG based on the Text release reports from JIRA.
         2. Review and update README.md if needed.
         3. Commit any changes back to git
     3. Stage any Roadmap or Release landing pages on the site.

3. Create a release candidate branch from master.
   X should start at 1 and increment if early release candidates fail to complete the release cycle.

        git checkout master
        git branch ${project.name}-${project.version}-rcX

4. Verify the source has the required license headers before trying to release:

        mvn -Papache-release clean apache-rat:check -e -DskipTests

5. Do a dry run of the release:prepare step:  

        mvn -Papache-release release:prepare -DautoVersionSubmodules=true -DdryRun=true

    - The dry run will not commit any changes back to SCM and gives you the opportunity to verify that the release process will complete as expected. You will be prompted for the following information :

        * Release version - take the default (should be ${project.version}-incubating)
        * SCM release tag - *DO NOT TAKE THE DEFAULT*  - ${project.artifactId}-${project.version}-rcX
        * New development version - take the default (should be ${project.version}-incubating-SNAPSHOT)
        * GPG Passphrase  

    - *If you cancel a release:prepare before it updates the pom.xml versions, then use the release:clean goal to just remove the extra files that were created.*

    - The Maven release plugin checks for SNAPSHOT dependencies in pom's. It will not complete the prepare goal until all SNAPSHOT dependencies are resolved.

6. Verify that the release process completed as expected
    1. The release plugin will create pom.xml.tag files which contain the changes that would have been committed to SVN. The only differences between pom.xml.tag and it's corresponding pom.xml file should be the version number.
    2. Check release.properties and make sure that the scm properties have the right version. Sometimes the scm location can be the previous version not the next version.
    3. Verify signatures ([Verifying release signatures](#verify_signatures))

7. Cleanup the release prepare files again:  

        mvn -Papache-release release:clean

8. Prepare the release
    1. Run the "release:prepare" step for real this time. You'll be prompted for the same version information.

            mvn -Papache-release -U clean release:prepare -DautoVersionSubmodules=true

    2. Backup (zip or tar) your local release candidate directory in case you need to rollback the release after the next step is performed.
9. Perform the release
    * This step will create a maven staging repository and site for use in testing and voting.

            mvn -Papache-release -Darguments='-Dmaven.test.skip.exec=true' release:perform -Dgoals=deploy -DlocalRepoDirectory=. -DlocalCheckout=true

    * If your local OS userid doesn't match your Apache userid, then you'll have to also override the value provided by the OS to Maven for the site-deploy step to work. This is known to work for Linux, but not for Mac and unknown for Windows.*

            -Duser.name=[your_apache_uid]

10. Verify the Nexus release artifacts
    1. Verify the staged artifacts in the nexus repo     
        * https://repository.apache.org/index.html
        * Staging repositories (under Build Promotion) --> Name column --> org.apache.streams
        * Navigate through the artifact tree and make sure that all javadoc, sources, tests, jars, ... have .asc (GPG signature) and .md5 files. See http://people.apache.org/~henkp/repo/faq.html and http://www.apache.org/dev/release-signing.html#openpgp-ascii-detach-sig
    2. Close the nexus staging repo
        * https://repository.apache.org/index.html
        * Staging repositories (under Build Promotion) --> Name column --> org.apache.streams
        * Click checkbox for the open staging repo (org.apache.streams-XXX) and press Close in the menu bar.
11. Put the release candidate up for a vote
     1. Create a VOTE email thread on dev@ to record votes as replies
     2. Create a DISCUSS email thread on dev@ for any vote questions
     3. Perform a review of the release and cast your vote. See the following for more details on Apache releases
           [http://www.apache.org/dev/release.html](http://www.apache.org/dev/release.html)  
     4. A -1 vote does not necessarily mean that the vote must be redone, however it is usually a good idea to rollback the release if a -1 vote is received. See - Recovering from a vetoed release
     5. After the vote has been open for at least 72 hours, has at least three +1 PMC votes and no -1 votes, then post the results to the vote thread by -
         * reply to the initial email and prepend to the original subject "[RESULT]"
         * Include a list of everyone who voted +1, 0 or -1.
     6. Promote the staged nexus artifacts  
         * https://repository.apache.org/index.html
         * Staging repositories (under Build Promotion) --> Name column --> org.apache.streams
         * Click checkbox of the closed staging repo (org.apache.streams-XXX) and select Release.
13. Put the release candidate up for an IPMC vote if there are fewer than 3 +1 (binding) votes from IPMC members
    1. Copy the source artifacts over to the release dev area     

            svn co https://dist.apache.org/repos/dist/dev/incubator/streams ./streams-dev  (KEEP this directory until after the release process has been completed)
            cd ./streams-dev
            mkdir ${project.version}
            cd ./${project.version}
            wget https://repository.apache.org/content/repositories/releases/org/apache/streams/${project.name}/${project.version}/${project.name}-${project.version}-source-release.zip    
            wget https://repository.apache.org/content/repositories/releases/org/apache/streams/${project.name}/${project.version}/${project.name}-${project.version}-source-release.zip.asc   
            wget https://repository.apache.org/content/repositories/releases/org/apache/streams/${project.name}/${project.version}/${project.name}-${project.version}-source-release.zip.md5   
            svn add ${project.name}-*
            svn commit -m "Committing Source Release for ${project.name}-${project.version}

    2. Submit a vote to general@incubator.apache.org requesting additional IPMC member votes.

14. Complete the release
    1. Copy the source artifacts over to the distribution area  

            svn co https://dist.apache.org/repos/dist/release/incubator/streams/releases ./streams-releases  (KEEP this directory until after the release process has been completed)
            cd ./streams-releases
            mkdir ${project.version}
            cd ./${project.version}
            wget https://repository.apache.org/content/repositories/releases/org/apache/streams/${project.name}/${project.version}/${project.name}-${project.version}-source-release.zip    
            wget https://repository.apache.org/content/repositories/releases/org/apache/streams/${project.name}/${project.version}/${project.name}-${project.version}-source-release.zip.asc   
            wget https://repository.apache.org/content/repositories/releases/org/apache/streams/${project.name}/${project.version}/${project.name}-${project.version}-source-release.zip.md5   
            svn add ${project.name}-*
            svn commit -m "Committing Source Release for ${project.name}-${project.version}

    3. Create an official release tag from the successful release candidate tag.

            git checkout ${project.name}-${project.version}-rcX
            git tag -a ${project.name}-${project.version} -m 'release tag ${project.name}-${project.version}'
            git push origin :refs/tags/streams-project-${project.version}

    4. Update the staged website
        *  Update the downloads page (downloads.md) to add new version using the mirrored URLs
        *  Modify the URL for the prior release to the archived URL for the release
    5.  Publish the website (see [website](website.html "Website Management"))
        *  WAIT 24hrs after committing releases for mirrors to replicate
        *  Publish updates to the download page
    6.  Delete the prior versions
        *  Navigate to the release directories checked out in the prior steps
        *  Delete the prior release artifacts using the svn delete command
        *  Commit the deletion
14. Update the JIRA versions page to close all issues, mark the version as "released", and set the date to the date that the release was approved. You may also need to make a new release entry for the next release.
15. Announcing the release
       * Make a news announcement on the streams homepage.
       * Make an announcement about the release on the dev@streams.incubator.apache.org, and announce@incubator.apache.org list as per the Apache Announcement Mailing Lists page)

####Recovering from a vetoed release

1. Reply to the initial vote email and prepend to the original subject -
     [CANCELED]
2. Clean the release prepare files and hard reset the release candidate branch.

        mvn -P apache-release release:clean

3. Delete the git tag created by the release:perform step -

        git tag -d streams-project-${project.version}-rcX
        git push origin :refs/tags/streams-project-${project.version}-rcX

4. Delete the build artifacts on people & www           

        rm -rfv /www/people.apache.org/builds/streams/${project.version}
        rm -rfv /www/www.apache.org/dist/streams/${project.version}

5. Drop the nexus staging repo
    1. https://repository.apache.org/index.html
    2. Enterprise --> Staging
    3. Staging tab --> Name column --> org.apache.streams
    4. Right click on the closed staging repo (org.apache.streams-XXX) and select Drop.
5. Remove the staged site
6. Make the required updates that caused the vote to be canceled during the next release cycle

<a name="verify_signatures" ></a>
####Verifying release signatures
On unix platforms the following command can be executed -

    for file in `find . -type f -iname '*.asc'`
    do
        gpg --verify ${file}
    done

You'll need to look at the output to ensure it contains only good signatures -

gpg: Good signature from ...
gpg: Signature made ...


<a name="combined" ></a>
####Combined Release
In order to perform a combined release of the streams-master and streams-project trunks, do the following:    

  *  Perform Steps 1-9 of the [release](#release-steps) for streams-master, streams-project, and streams-examples
      *  Do NOT perform step 10 until steps 1-9 have been completed for BOTH projects
      *  Build the streams-master FIRST
      *  When prompted to change dependencies on SNAPSHOTs, do so to the corresponding releases that you just built
  *  Execute the remaining steps using the following e-mail template

          to: streams-dev@incubator.apache.org
          subject: [VOTE] Apache Streams ${release.version} Release Candidate

          I've created a combined ${release.version} release candidate, with the
          following artifacts up for a vote:

          incubator-streams-master source tag (r${release.version}):
          https://git-wip-us.apache.org/repos/asf?p=incubator-streams-master.git;a=commit;h=...

          incubator-streams source tag (r${release.version}):
          https://git-wip-us.apache.org/repos/asf?p=incubator-streams.git;a=commit;h=...

          incubator-streams-examples source tag (r${release.version}):
          https://git-wip-us.apache.org/repos/asf?p=incubator-streams-examples.git;a=commit;h=...

          Maven staging repo:
          https://repository.apache.org/content/repositories/${release.master.repository}
          https://repository.apache.org/content/repositories/${release.project.repository}
          https://repository.apache.org/content/repositories/${release.examples.repository}

          Source releases:
          https://repository.apache.org/content/repositories/${release.master.repository}/org/apache/streams/streams-master/${release.version}/streams-master-${release.version}-source-release.zip
          https://repository.apache.org/content/repositories/${release.project.repository}/org/apache/rave/rave-project/${release.version}/streams-project-${release.version}-incubating-source-release.zip
          https://repository.apache.org/content/repositories/${release.project.repository}/org/apache/rave/rave-project/${release.version}/streams-project-${release.version}-incubating-source-release.zip

          Checksums of streams-master-${release.version}-source-release.zip:
          MD5: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
          SHA1: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

          Release artifacts are signed with the following key:
          https://people.apache.org/keys/committer/...

          Please take the time to verify the artifacts before casting your vote.

          Vote will be open for 72 hours.

          [ ] +1  approve
          [ ] +0  no opinion
          [ ] -1  disapprove (and reason why)

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
