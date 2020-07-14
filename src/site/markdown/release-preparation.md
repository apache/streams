##Release Preparation

Certain aspects of the release managers job are less structured and amenable to following a script.

### License, Notice, and Intellectual Property Verification

    0. Begin in working directory streams

    1. Run audit of all third-party dependencies

            mvn -Pcheck license:aggregate-add-third-party

        This task is meant to fail if blacklist licenses are found, or if any dependencies have an unknown license.

    2. Review third-party license names and urls

            cat src/license/THIRD-PARTY.txt | sort

        Identify any new name variants of common licenses, and modify src/license/licenseMerges.txt

        Any unindentified license, needs to be researched and dependency removed or added to src/license/licenseMissingFile.properties

        Files under src/license may be updated by maven plugins, and/or by project contributors.

        These files should be checked in and examined by the PMC prior to each release.

        NOTE: Dependencies may publish an uncommon license text, but still be OK to include.

        NOTE: Some projects are published under multiple licenses, and the operative license may not be the first listed.

    3. Update binary/bytecode LICENSE file

        Every bundled third-party dependency must declare at least one entry in streams-dist/LICENSE.

        Licenses which contain a copyright notice must include that copyright notice in-line.

        For licenses which fall under the 'Category B' designation, each dependency must be enumerated, associated with its license, and a url to its homepage.

        NOTE: src/license/THIRD-PARTY.txt and src/license/licenses.xml are helpful during this step.

    3. Update binary/bytecode NOTICE file

        Instructions TBD

### References

    [https://www.apache.org/dev/licensing-howto.html]("https://www.apache.org/dev/licensing-howto.html")

    [https://www.apache.org/legal/resolved.html]("https://www.apache.org/legal/resolved.html")

    [http://www.apache.org/foundation/license-faq.html#]("http://www.apache.org/foundation/license-faq.html"