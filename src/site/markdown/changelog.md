Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

        Release Notes - Streams - Version 0.4
    
<h2>        Sub-task
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-121'>STREAMS-121</a>] -         InstagramTimelineProvider
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-217'>STREAMS-217</a>] -         Delete unneeded Configurator classes
</li>
</ul>
                            
<h2>        Bug
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-73'>STREAMS-73</a>] -         Interfaces
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-132'>STREAMS-132</a>] -         RegexUtils does not ensure that content is non-null
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-151'>STREAMS-151</a>] -         Refactor Facebook Provider to have continuous AND finite mode
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-159'>STREAMS-159</a>] -         Add facebook page feed provider
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-177'>STREAMS-177</a>] -         new NPE in LinkResolver causing surefire to fail
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-258'>STREAMS-258</a>] -         Prevent signing of .asc, .m5, and .sha1 artifacts
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-391'>STREAMS-391</a>] -         streams-provider-dropwizard exceptions in test logs
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-405'>STREAMS-405</a>] -         Link to src/site/markdown/index.md in README.md is broken
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-407'>STREAMS-407</a>] -         add DateTimeSerializers for formats safely, don't crash if invalid
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-414'>STREAMS-414</a>] -         Incorrect Documentation 
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-436'>STREAMS-436</a>] -         Put a timeout on all Provider Integration Tests 
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-437'>STREAMS-437</a>] -         DatumFromMetadataProcessorIT failing during 0.4 release
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-446'>STREAMS-446</a>] -         RAT check fails in prep for 0.4-incubating release
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-447'>STREAMS-447</a>] -         Scala-plugin failures in prep for 0.4-incubating release
</li>
</ul>
                        
<h2>        Improvement
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-127'>STREAMS-127</a>] -         JsonSchema Replication in Datasift provider
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-160'>STREAMS-160</a>] -         Embed original source provider pojos inside datasift pojos if possible
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-186'>STREAMS-186</a>] -         Platform-level 'detectConfiguration'
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-399'>STREAMS-399</a>] -         Add any missing fields to tweet.json
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-400'>STREAMS-400</a>] -         streams-persist-elasticsearch : bump to version 2.x
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-403'>STREAMS-403</a>] -         Ensure all providers function stand-alone
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-404'>STREAMS-404</a>] -         Integration Test for FsElasticsearchIndex
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-411'>STREAMS-411</a>] -         ability (and instructions on how) to run providers directly from console
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-413'>STREAMS-413</a>] -         Update dependency and plugin versions - Q4 2016
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-425'>STREAMS-425</a>] -         better tracking completion in multi-threaded providers
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-426'>STREAMS-426</a>] -         streams-persist-mongo : test with docker
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-427'>STREAMS-427</a>] -         Support any jackson-compatible class as valid input to base converters and provider converters
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-428'>STREAMS-428</a>] -         Update example markdown on website 
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-429'>STREAMS-429</a>] -         fix failing tests in streams-plugins
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-430'>STREAMS-430</a>] -         update jenkins to run advanced integration testing
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-431'>STREAMS-431</a>] -         Remove streams.util.RegexUtils
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-432'>STREAMS-432</a>] -         Update to Java 8
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-433'>STREAMS-433</a>] -         Upgrade maven and jenkins to build with jdk8
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-434'>STREAMS-434</a>] -         Delete CustomDateTimeFormat which is not used anywhere
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-435'>STREAMS-435</a>] -         remove incubator-streams-master-pom.xml
</li>
</ul>
            
<h2>        New Feature
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-213'>STREAMS-213</a>] -         Publish jsonschemas to a web-accessible URL when jenkins builds snapshot and releases
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-389'>STREAMS-389</a>] -         Support generation of scala source from jsonschemas
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-398'>STREAMS-398</a>] -         Support generation of hive table definitions from jsonschema
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-418'>STREAMS-418</a>] -         Flink twitter example(s)
</li>
</ul>
                                            
<h2>        Story
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-51'>STREAMS-51</a>] -         Complete, test, and document tika processor
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-297'>STREAMS-297</a>] -         Create RssLinkProvider
</li>
</ul>
            
<h2>        Task
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-203'>STREAMS-203</a>] -         Update GooglePlus TypeConverter to handle Post Activities
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-316'>STREAMS-316</a>] -         add “apache’ to the artifact name
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-408'>STREAMS-408</a>] -         Check package names and run instructions of modules in streams-examples/local
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-409'>STREAMS-409</a>] -         the copyright year in the NOTICE files need to be updated for 2016. 
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-410'>STREAMS-410</a>] -         Delete any modules which have been removed from reactor from master branch
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-416'>STREAMS-416</a>] -         Delete defunct or not-implemented provider modules
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-417'>STREAMS-417</a>] -         Collect example AS 2.0 object and activity documents to use in test cases
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-419'>STREAMS-419</a>] -         reboot: cleanup git branches and tags 
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-421'>STREAMS-421</a>] -         Delete defunct or not-implemented runtime modules
</li>
</ul>
        
<h2>        Test
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-415'>STREAMS-415</a>] -         Proof of concept integration test that pulls actual data from generator
</li>
</ul>
        
        Release Notes - Streams - Version 0.3
    
<h2>        Sub-task
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-122'>STREAMS-122</a>] -         Build appropriate serializer classes
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-198'>STREAMS-198</a>] -         Deprecate DatumCountable
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-231'>STREAMS-231</a>] -         Basic Neo4j graph persistence
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-310'>STREAMS-310</a>] -         incubator-streams-examples rat plugin
</li>
</ul>
                            
<h2>        Bug
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-158'>STREAMS-158</a>] -         Sysomos Processor exceptions result in failed processor thread
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-220'>STREAMS-220</a>] -         FacebookPostSerializer is broken
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-223'>STREAMS-223</a>] -         streams-monitoring exception when streamConfig not set
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-227'>STREAMS-227</a>] -         Array out of bounds Exception running FacebookTypeConverter
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-229'>STREAMS-229</a>] -         GooglePlus TypeConverter needs to be able to accommodate String datums
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-230'>STREAMS-230</a>] -         Broadcast Monitor doesn't start in all cases
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-236'>STREAMS-236</a>] -         AbstractRegexExtensionExtractor should not allow duplicate entities
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-260'>STREAMS-260</a>] -         FacebookPageFeedDataCollector should handle backoff strategy correctly
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-264'>STREAMS-264</a>] -         LinkExpansion Tests are depending on (non-existent) external resources
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-311'>STREAMS-311</a>] -         TwitterUserInformationProvider stalls with > 20 items provided
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-337'>STREAMS-337</a>] -         WebHdfsPersistReader/Writer should allow user-specified file encoding
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-338'>STREAMS-338</a>] -         Runtime exceptions caught by LocalStreamBuilder aren't logged
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-346'>STREAMS-346</a>] -         persist-hdfs: unnecessary port config causes failure when reading from local filesystem
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-347'>STREAMS-347</a>] -         persist-hdfs: incorrectly escapes output when write called on a json String
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-354'>STREAMS-354</a>] -         pojo.json.Collection.totalItems should be an integer
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-355'>STREAMS-355</a>] -         WebHdfsPersistReader should be serializable
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-356'>STREAMS-356</a>] -         Metadata missing in StreamsDatum constructors and toString
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-357'>STREAMS-357</a>] -         Streams can shutdown before starting if providers haven't started yet when runtime first checks run status
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-358'>STREAMS-358</a>] -         Refreshing indexes in EsPW.cleanUp can result in streams not terminating
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-374'>STREAMS-374</a>] -         Fix ActivityConverterUtil detectClasses(document) exception handling
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-377'>STREAMS-377</a>] -         can't create different LineReadWriteUtils 
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-380'>STREAMS-380</a>] -         Media_Link should be serializable
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-382'>STREAMS-382</a>] -         Media_Link width and height should be integers, not floats
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-386'>STREAMS-386</a>] -         Clean up streams-pojo jsonschemas to ensure all beans are serialiazable
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-395'>STREAMS-395</a>] -         incubator-streams-examples COMPILATION ERROR
</li>
</ul>
                        
<h2>        Improvement
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-126'>STREAMS-126</a>] -         StreamsLocalBuilder should look for provider timeout in typesafe config
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-191'>STREAMS-191</a>] -         Streams implement use of throughput queues
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-226'>STREAMS-226</a>] -         Consolidate all stream-wide configuration
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-279'>STREAMS-279</a>] -         Create Youtube User/Channel provider
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-294'>STREAMS-294</a>] -         Allow for custom setting of QueueSize, BatchSize, and ScrollTimeout
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-308'>STREAMS-308</a>] -         Update readme and website to recommend latest version of JDK
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-313'>STREAMS-313</a>] -         Performant Off-line Neo4j GraphPersistWriter
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-320'>STREAMS-320</a>] -         Update release documentation to cover build and deploy of mvn site to svn
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-321'>STREAMS-321</a>] -         Add HOCON Converter support for input object not at root node
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-322'>STREAMS-322</a>] -         Support gzipped files in WebHdfsReader
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-324'>STREAMS-324</a>] -         clean up example poms
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-325'>STREAMS-325</a>] -         Configure monitoring via typesafe 
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-326'>STREAMS-326</a>] -         Bump hadoop version in streams-persist-hdfs
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-328'>STREAMS-328</a>] -         StreamsJacksonMapper should omit null/empty fields when serializing
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-329'>STREAMS-329</a>] -         Processor to derive required ES metadata from standard fields
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-330'>STREAMS-330</a>] -         make TwitterErrorHandler respect the Twitter4j RateLimitStatus reset time
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-332'>STREAMS-332</a>] -         Restore ability to test data conversions on flat files during build
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-333'>STREAMS-333</a>] -         ElasticsearchPersistUpdater should set parent+routing when available
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-335'>STREAMS-335</a>] -         Publish schema and configuration resources to maven site pages
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-336'>STREAMS-336</a>] -         consolidate common pom sections to streams-master
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-339'>STREAMS-339</a>] -         incorporate defined extension fields for activities and actors into pojos
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-342'>STREAMS-342</a>] -         Expose convertResultToString and processLine as public methods
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-343'>STREAMS-343</a>] -         Make SerializationUtil spark-compatible
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-348'>STREAMS-348</a>] -         Add get/set methods for StreamsConfiguration to Builder interface
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-350'>STREAMS-350</a>] -         provider-twitter: derive baseURL from configuration
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-353'>STREAMS-353</a>] -         Support removal of 'extensions' from document path structure
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-361'>STREAMS-361</a>] -         Persist Reader/Writer for Amazon Kinesis
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-362'>STREAMS-362</a>] -         Make shutdown timers in local runtime fully configurable
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-363'>STREAMS-363</a>] -         upgrade persist-s3 to match persist-hdfs features
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-364'>STREAMS-364</a>] -         Allow resolution of typesafe/stream config from url in stream-config
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-368'>STREAMS-368</a>] -         Add String getId() to StreamsOperation
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-370'>STREAMS-370</a>] -         Support arbitrary labels in streams-persist-graph
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-371'>STREAMS-371</a>] -         Allow use of ids endpoints in TwitterFollowingProvider
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-373'>STREAMS-373</a>] -         Allow specification of 'maximum_items' in twitter providers
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-375'>STREAMS-375</a>] -         Override component configuration when valid beans supplied by runtime as prepare args
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-376'>STREAMS-376</a>] -         When twitter providers fail authentication, log the ID that could not be accessed.
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-378'>STREAMS-378</a>] -         incompatibility between streams binaries and spark 1.5
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-381'>STREAMS-381</a>] -         streams-provider-twitter: User can contain a Status
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-383'>STREAMS-383</a>] -         register DefaultScalaModule in StreamsJacksonMapper
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-387'>STREAMS-387</a>] -         Default behavior of streams-pojo-extensions: use additionalProperties directly
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-390'>STREAMS-390</a>] -         Eliminate dependency and plugin warnings from build 
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-392'>STREAMS-392</a>] -         Centralized logging configuration for maven build
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-393'>STREAMS-393</a>] -         Switch any usage of System.out and System.err to slf4j
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-397'>STREAMS-397</a>] -         streams-provider-youtube test failure in jenkins
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-401'>STREAMS-401</a>] -         Refresh Streams Website
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-406'>STREAMS-406</a>] -         Bring streams-master markdown files into compliance with rat plugin
</li>
</ul>
            
<h2>        New Feature
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-120'>STREAMS-120</a>] -         Implement, Document, and Test Instagram Provider
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-221'>STREAMS-221</a>] -         Simple HTTP Persist Writer
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-222'>STREAMS-222</a>] -         Simple HTTP Webhook Provider
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-290'>STREAMS-290</a>] -         Example: local/twitter-userstream-elasticsearch
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-292'>STREAMS-292</a>] -         Example: local/twitter-history-elasticsearch
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-295'>STREAMS-295</a>] -         Example - elasticsearch-hdfs
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-302'>STREAMS-302</a>] -         Example: local/twitter-follow-graph
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-385'>STREAMS-385</a>] -         ActivityObjectConverter
</li>
</ul>
                                            
<h2>        Story
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-43'>STREAMS-43</a>] -         Complete, test, and document g+ provider
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-44'>STREAMS-44</a>] -         Complete, test, and document sysomos provider
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-46'>STREAMS-46</a>] -         Complete, test, and document facebook API provider
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-261'>STREAMS-261</a>] -         Create Facebook Bio Collector/Provider
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-272'>STREAMS-272</a>] -         Create a Youtube Post Provider
</li>
</ul>
            
<h2>        Task
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-123'>STREAMS-123</a>] -         Add twitter specific link handling for datasift
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-202'>STREAMS-202</a>] -         Create Google Plus Deserializer and TypeConverter
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-274'>STREAMS-274</a>] -         Create a YoutubeTypeConverter and serializer
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-280'>STREAMS-280</a>] -         Add ability to get a final document count from the Sysomos Provider
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-298'>STREAMS-298</a>] -         Ensure jcl-over-slf4j bridge is included where necessary
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-317'>STREAMS-317</a>] -         release a .tar.gz artefact as well as a .zip
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-318'>STREAMS-318</a>] -         remove test files whose license may not be compatible with an Apache release (google-gplus)
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-319'>STREAMS-319</a>] -         update &lt;developer&gt; info in the pom with active contributors
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-331'>STREAMS-331</a>] -         Set up build of master and pull requested branches on travis-ci.org
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-334'>STREAMS-334</a>] -         version bump datasift module to remove boundary repo dependency
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-351'>STREAMS-351</a>] -         add A2 license to site.xml files
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-359'>STREAMS-359</a>] -         copy streams-master pom into streams-project as CI workaround
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-372'>STREAMS-372</a>] -         Support deploy to snapshot repo by CI
</li>
</ul>
        
<h2>        Test
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-208'>STREAMS-208</a>] -         Integration Testing capability and reference Integration Test
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-384'>STREAMS-384</a>] -         TestLinkUnwinderProcessor.test404Link is failing
</li>
</ul>

        Release Notes - Streams - Version 0.2
    
<h2>        Sub-task
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-275'>STREAMS-275</a>] -         ActivityConverterProcessor should apply reflection mode when configuration is not provided
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-277'>STREAMS-277</a>] -         Upgrade streams-provider-twitter to work with reflection-based conversion
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-305'>STREAMS-305</a>] -         Add missing AL
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-306'>STREAMS-306</a>] -         Intermittent test failures
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-307'>STREAMS-307</a>] -         Release test-jar packaging
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-309'>STREAMS-309</a>] -         incubator-streams-examples site plugin
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-312'>STREAMS-312</a>] -         Remove test resources without clear licensing and ignore tests that require them
</li>
</ul>
                            
<h2>        Bug
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-155'>STREAMS-155</a>] -         Build hadoop modules with Apache artifacts
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-200'>STREAMS-200</a>] -         StreamsProcessorTask ignores any processing
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-225'>STREAMS-225</a>] -         Streams need to remove any of their JMX beans on shutdown/cleanup
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-243'>STREAMS-243</a>] -         S3 Persist Writer does not flush or shutdown on stream shutdown
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-263'>STREAMS-263</a>] -         FacebookTypeConverter should be able to handle Facebook Pages
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-266'>STREAMS-266</a>] -         some classes/tests are using the wrong NotImplementedException
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-278'>STREAMS-278</a>] -         Rework pig runtime as part of switch from 'Serializer' to 'Converter'
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-281'>STREAMS-281</a>] -         enable BroadcastMessagePersister
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-288'>STREAMS-288</a>] -         StreamsJacksonModule should not scan for DateTimeFormats by default
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-296'>STREAMS-296</a>] -         Local Runtime doesn't allow persist writers enough time to flush and close during shutdown
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-299'>STREAMS-299</a>] -         Sysomos Provider uses dev API URL
</li>
</ul>
                        
<h2>        Improvement
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-147'>STREAMS-147</a>] -         Platform-level type conversion
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-201'>STREAMS-201</a>] -         Util function to remove all MXBeans for tests
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-214'>STREAMS-214</a>] -         Create, test, and document file-backed persistance module
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-271'>STREAMS-271</a>] -         suggest increasing JVM heap in readme
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-273'>STREAMS-273</a>] -         Support POST endpoints in streams-http
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-284'>STREAMS-284</a>] -         Read/write parent IDs in streams-persist-elasticsearch
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-285'>STREAMS-285</a>] -         Add all objectTypes in spec to streams-pojo
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-286'>STREAMS-286</a>] -         Add all verbs in spec to streams-pojo
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-293'>STREAMS-293</a>] -         allow for missing metadata fields in streams-persist-hdfs
</li>
</ul>
            
<h2>        New Feature
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-235'>STREAMS-235</a>] -         TwitterFollowingProvider
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-259'>STREAMS-259</a>] -         Filter a stream of Activities for specific verbs and objectTypes
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-283'>STREAMS-283</a>] -         Logstash-friendly monitoring
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-287'>STREAMS-287</a>] -         Example: local/elasticsearch-reindex
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-289'>STREAMS-289</a>] -         Example: local/mongo-elasticsearch-sync
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-301'>STREAMS-301</a>] -         streams-converters: HOCON Converter
</li>
</ul>
                                            
<h2>        Story
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-47'>STREAMS-47</a>] -         Complete, test, and document mongo persist
</li>
</ul>
            
<h2>        Task
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-95'>STREAMS-95</a>] -         TwitterProfileProcessor needs to send a user ID in each StreamsDatum
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-304'>STREAMS-304</a>] -         Perform 0.2-incubating release
</li>
</ul>

                
        Release Notes - Streams - Version 0.1
    
<h2>        Sub-task
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-212'>STREAMS-212</a>] -         Generic Type Converter Processor
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-215'>STREAMS-215</a>] -         Add method to StreamsConfigurator to return Serializable config given a Typesafe Config
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-218'>STREAMS-218</a>] -         Generic Activity Converter Processor
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-241'>STREAMS-241</a>] -         Reflection-based StreamsJacksonMapper
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-245'>STREAMS-245</a>] -         Clean-up root POM
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-246'>STREAMS-246</a>] -         Clean-up module POMs
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-247'>STREAMS-247</a>] -         Clean-up READMEs
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-248'>STREAMS-248</a>] -         Bring website content in-line with released capabilities.
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-249'>STREAMS-249</a>] -         Javadoc plugin
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-250'>STREAMS-250</a>] -         Prepare release notes
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-251'>STREAMS-251</a>] -         Dry-run release process, review artifacts, update documentation
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-254'>STREAMS-254</a>] -         Resolve rat plugin failures
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-255'>STREAMS-255</a>] -         Merge streams-master into streams-project
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-256'>STREAMS-256</a>] -         streams-components-all version doesn't bump during release
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-267'>STREAMS-267</a>] -         .gitignore eclipse workspace files
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-268'>STREAMS-268</a>] -         remove streams-web from master
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-269'>STREAMS-269</a>] -         maven-remote-resources-plugin using SNAPSHOT resource-bundles
</li>
</ul>
                            
<h2>        Bug
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-167'>STREAMS-167</a>] -         TwitterConfigurator doesn't properly create TwitterUserInfoConfiguration from hocon
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-219'>STREAMS-219</a>] -         src/main/resource files are being created by build
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-252'>STREAMS-252</a>] -         Monitor Executor Service in LocalStreamBuilder needs to be Flexible
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-262'>STREAMS-262</a>] -         jars in runner folder of source release
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-265'>STREAMS-265</a>] -         some jar artifacts from 0.1-rc1 did not contain DISCLAIMER
</li>
</ul>
                        
<h2>        Improvement
</h2>
<ul>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-68'>STREAMS-68</a>] -         ActivitySerializer should be ActivityConverter
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-143'>STREAMS-143</a>] -         Allow modules to get instances of StreamsJacksonMapper that can process additional DateTime formats
</li>
<li>[<a href='https://issues.apache.org/jira/browse/STREAMS-244'>STREAMS-244</a>] -         Prepare for 0.1 release
</li>
</ul>
                                                                                    