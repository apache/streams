## Prepare for a release

Certain aspects of the release managers job are less structured and amenable to following a script.

NOTE: Refer to the current steps and guides on the ASF website:

- [https://www.apache.org/dev/licensing-howto.html]("https://www.apache.org/dev/licensing-howto.html")
- [https://www.apache.org/legal/resolved.html]("https://www.apache.org/legal/resolved.html")
- [http://www.apache.org/foundation/license-faq.html#]("http://www.apache.org/foundation/license-faq.html")

### License, Notice, and Intellectual Property Verification

NOTE: Files under src/license may be updated by maven plugins, and/or by project contributors.

NOTE: These files should be checked in and examined by the PMC prior to each release.

NOTE: Dependencies may publish an uncommon license text, but still be OK to include.

NOTE: Some projects are published under multiple licenses, and the operative license may not be the first listed.

0. Begin in working directory streams

1. Run audit of all third-party dependencies

        mvn -Pcheck license:aggregate-add-third-party

   This task is meant to fail if blacklist licenses are found, or if any dependencies have an unknown license.

2. Review any warnings like the following:

        [WARNING] dependency [org.apache.ant--ant--1.7.0] does not exist in project, remove it from the missing file.

   If the dependency is not in the project, it should be removed from the missing file.

   Sometimes the dependency is in the project, but the version is different.  In this case, the version should be updated.

3. Review third-party license names and urls

        cat src/license/THIRD-PARTY.txt | sort

   Identify any new name variants of common licenses, and modify src/license/licenseMerges.txt

4. Research any dependency with an unidentified license.

   These dependency must be hand-labelled in src/license/licenseMissingFile.properties

5. If the total number of licenses has legitimately changed, run

        mvn license:download-licenses

6. Repeat step 1 and then proceed to step 7.

7. Compare src/license/THIRD-PARTY.txt to its contents during the prior release.

   With attention to any new entries, and any entries which have changed, modify NOTICE.txt.

   Every bundled third-party dependency must declare at least one entry in LICENSE.

   Licenses which contain a copyright notice must include that copyright notice in-line.

   For licenses which fall under the 'Category B' designation, each dependency must be enumerated, associated with its license, and a url to its homepage.

8. Audit the source code for any new files added since the last release missing mandatory license language.

         mvn -N -Pcheck apache-rat:check

    If any files are missing license headers, they must be added.

    If there is any question as to the intent of the contributor, the contributor should be consulted.

    In most cases, the contributor will have intended to contribute under the Apache License 2.0, and/or an IP clearance form will be on file with the ASF.

    Do the needful to ensure the rat check passes before proceeding.

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0