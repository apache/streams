## SBT

Run from your command line:

    sbt

| Possible result | Explanation |
|-----------------|-------------|
| bash: sbt: command not found | You need to install sbt |

You should really install sbt-extras, like this:

    curl -s https://raw.githubusercontent.com/paulp/sbt-extras/master/sbt > /usr/bin/sbtx && chmod 0755 /usr/bin/sbtx

Now you can easily run the streams examples using SBT, like a boss.

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
