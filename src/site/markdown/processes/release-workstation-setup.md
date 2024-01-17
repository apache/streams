## Release Workstation Setup

These setup steps need to be performed once on each workstation being used to perform a release.

### Install necessary tools

Refer to [General Installation Instructions](install/general.md) for instructions on installing the necessary tools.
 
### Configure Maven

Update your ~/.m2/settings.xml with the properties from [Publishing Maven Artifacts](http://www.apache.org/dev/publishing-maven-artifacts.html#dev-env)

If you are using an SDKMAN-managed maven, it may not use the settings.xml in your home directory.  

You can ensure that it does by adding the following to your ~/.bash_profile:

    export M2_SETTINGS=~/.m2/settings.xml

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
