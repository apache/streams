---
layout: default
title:
description:
group:

---

## Website Management

http://streams.incubator.apache.org is generated via Jekyll.

Jekyll is a CMS that let's you build the website locally, where you can check that changes and things work as expected.

If you wish to make a change to the website, clone the streams project:

`git clone https://github.com/apache/incubator-streams`

Go to the website directory

`cd incubator-streams/website`

And begin hosting

`bundle exec jekyll build --safe`



### Website Updates

##### Open a PR

After you have made changes to the website and checked them thoroughly, push the branch to a local repository and open a pull request (link guidelines for pull requests).

A committer will review with you shortly.

It is good manners to include documentation whenever you are adding or updating features as well, I might add.

##### Publishing Changes: Committers Only

Commiters can publish a new website (usually after an interval of time in which website updates have occurred) as follows:

Assuming you are at the top level directory...

1. Checkout from svn
`svn co https://svn.apache.org/repos/asf/incubator/streams asf-streams`

2. Build with Jekyll
`bundle exec jekyll build --safe`

3. Copy the new site from `website/_site` to `asf-streams/site/trunk/content/site/[VERSION]`
 
4. Commit the updates with `svn commit`


#### Promoting a new website version

All that's left at this point is to promote from staging to production.

If a release is happening, this should happen just prior to the release announcement.

If you are just updating content associated with a snapshot, use good judgement to determine whether the list should have a chance to review
and/or vote on the changes in staging prior to promotion.

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
