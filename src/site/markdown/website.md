## Website Management

http://streams.apache.org is a set of interconnected websites generated from markdown
by the maven site plugin.  The front page, this page, and all pages aside from those
organized under 'Examples' reside in http://github.com/apache/streams.  Those pages reside in 
http://github.com/apache/streams-examples

### Website Content

Pages, diagrams, and other hard-linkable resources are stored throughout the source tree.

#### Markdown

* src/site/markdown

Most HTML pages are generated from markdown.  The maven-site-plugin does this during the site build process.

#### Schemas

* src/main/jsonschema
* src/main/xmlschema

JSON and XML Schemas through-out the project are copied to the web page of their respective modules so they can be
linked to in other projects.

This allows users to extend the canonical streams schemas incrementally and/or re-use specific fields as they see fit.

#### Resources

* src/main/resources
* src/test/resources

Other resources including .conf and .properties files are copied to the web page of their respective modules so
they can be linked to across projects and in external projects.

This allows users to import HOCON from modules outside their sphere of control and adapt to changes upstream.

### Website Updates

The project website(s) are hosted by the Apache foundation and updated via SVN.

Currently pushing website changes is a manual process with several steps, performed by whomever is making the change.

This typically requires checking out the current website from SVN.

    svn co https://svn.apache.org/repos/asf/streams/site/trunk/content
    cd content

NOTE:

Repositories should always be built and published in the following order:

* streams-project
* streams-examples

#### Preparing to publishing a new website version

The instructions below presume:

* you have a shell open in the SVN content directory
* you know the artifactId and version of the repository you want to publish.

If this is a brand new snapshot or release version, you first need to create a directory corresponding to the new version.

    mkdir site/${project.version}
    svn add site/${project.version}
    svn commit -m "svn add site/${project.version}"

The first time a specific site is being published for this version, you must create the directory where it will be published.

    mkdir site/${project.version}/${project.artifactId}
    svn add site/${project.version}/${project.artifactId}
    svn commit -m "svn add site/${project.version}/${project.artifactId}"

If you are published over an existing snapshot, you may need to first remove the existing version and recreate an empty directory.

    rm -rf site/${project.version}/${project.artifactId}
    svn delete site/${project.version}/${project.artifactId}
    svn commit -m "svn delete site/${project.version}/${project.artifactId}"
    mkdir site/${project.version}/${project.artifactId}
    svn add site/${project.version}/${project.artifactId}
    svn commit -m "svn add site/${project.version}/${project.artifactId}"

In general however the scm plugin is smart enough to just add new resources and commit changes.

If you are publishing a release, it's appropriate to delete the site snapshots related to the prior releases.

For example when 0.5.2 is published, 0.5.1-SNAPSHOT should be deleted.

This policy of removing old snapshots keeps external projects from linking to snapshot artifacts indefinitely.

Release artifacts should be retained indefinitely.

#### Generating and committing a new website version

The instructions below presume:

* you have a shell open in the root of a project repository
* you know the artifactId and version of the repository you want to publish.

First, ensure that you have local credentials capable of publishing the site.

    <server>
      <id>site.streams.{master|project|examples}</id>
      <username>{your apache ID}</username>
      <privateKey>{absolute path to your private key</privateKey>
      <passphrase>{your private key passphrase}</passphrase>
      <filePermissions>664</filePermissions>
      <directoryPermissions>775</directoryPermissions>
      <configuration></configuration>
    </server>

Next, generate SVG resources for all DOT diagrams in the source tree

    for dot in $(find . -name *.dot); do dot -Tsvg $dot -o $dot.svg; done

Then, generate the site that will be published

    mvn clean generate-sources package -Dmaven.test.skip.exec=true site:site site:stage

Double-check the logs and determine where exactly the staged site is located on your local drive.

At this point you can open target/staging/index.html (or wherever) and do a basic sanity check on the site you intend to publish.

Finally, publish the site.

    mvn scm-publish:publish-scm

You may need to provide -Dscmpublish.content=<> depending exactly where the staging site directory winds up.

Note the revision number checked in at the bottom of the maven logs.

#### Updating the staging site

Next step is to update the staging site and check it out.

Log into https://cms.apache.org with your apache credentials.

Use https://cms.apache.org/streams to access the streams website.

Typically you can use Get streams Working Copy, although you might need to Force if you run into conflicts in SVN.

You'll probably need to 'Update this directory' if you want to inspect the changes you committed above.

'View Staging Builds' should show a build around the time of the previous commit.  This means the change has been staged.

You should now be able to access and review the published site(s) via the staging URL:

* http://streams.staging.apache.org/

At this point use explicit versions to access and review the new documentation, i.e.

* http://streams.staging.apache.org/site/0.5.1/streams-project/index.html
* http://streams.staging.apache.org/site/0.5.1/streams-examples/index.html

#### Managing version pointers

When new versions of these sites are built for the first time, an additional set to alter Apache rules may be appropriate.

The convention we use exposes the latest specific site version(s) using redirects maintained in the .htaccess file of project website SVN.

These rules are located in the '.htaccess' file in the root of the SVN content directory and looks something like this:

    Options +FollowSymLinks
    RewriteEngine on
    RedirectMatch   "^/$"  "/site/0.5.2-SNAPSHOT/streams-project"
    Redirect /site/latest /site/0.5.2-SNAPSHOT
    Redirect /site/latest/streams-project /site/0.5.2-SNAPSHOT/streams-project
    Redirect /site/latest/streams-examples /site/0.5.2-SNAPSHOT/streams-examples

Adjust the Redirect rules as appropriate for the project and version you are deploying.  You can do this directly from the CMS.

Commit your changes, wait a few seconds, click Follow Staging Build, and you should see a new build with a 'Build Successful' message.

You should now be able to use 'latest' as an alias for the docs you are deploying, i.e.

* http://streams.staging.apache.org/site/latest/streams-project/index.html
* http://streams.staging.apache.org/site/latest/streams-examples/index.html

#### Promoting a new website version

All that's left at this point is to promote from staging to production.

If a release is happening, this should happen just prior to the release announcement.

If you are just updating content associated with a snapshot, use good judgement to determine whether the list should have a chance to review
and/or vote on the changes in staging prior to promotion.

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
