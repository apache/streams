##Dependency Info

This project uses [Maven](http://maven.apache.org/ "Maven") for dependency management.

Below are some examples of how to import Streams artifacts into your project.

Please note that your project should import multiple artifacts corresponding to the Components in your stream(s) and the Runtime used to execute your stream(s).

You should *not* import streams-master (depicted below), because it does not do anything interesting.

Browse the "Project Modules" index of streams-project to find artifacts you might want to import.

[streams-project-modules](http://streams.apache.org/site/latest/streams-project/modules.html "http://streams.apache.org/site/latest/streams-project/modules.html")

<div class="section">

<h3><a name="Apache_Maven"></a>Apache Maven</h3><a name="Apache_Maven"></a>

<div class="source">

<pre class="prettyprint">&lt;dependency&gt;
  &lt;groupId&gt;org.apache.streams&lt;/groupId&gt;
  &lt;artifactId&gt;streams-master&lt;/artifactId&gt;
  &lt;version&gt;0.3-incubating-SNAPSHOT&lt;/version&gt;
  &lt;type&gt;pom&lt;/type&gt;
&lt;/dependency&gt;</pre>

</div>

</div>

<div class="section">

<h3><a name="Apache_Buildr"></a>Apache Buildr</h3><a name="Apache_Buildr"></a>

<div class="source">

<pre class="prettyprint">'org.apache.streams:streams-master:pom:0.3-incubating-SNAPSHOT'</pre>

</div>

</div>

<div class="section">

<h3><a name="Apache_Ivy"></a>Apache Ivy</h3><a name="Apache_Ivy"></a>

<div class="source">

<pre class="prettyprint">&lt;dependency org=&quot;org.apache.streams&quot; name=&quot;streams-master&quot; rev=&quot;0.3-incubating-SNAPSHOT&quot;&gt;
  &lt;artifact name=&quot;streams-master&quot; type=&quot;pom&quot; /&gt;
&lt;/dependency&gt;</pre>

</div>

</div>

<div class="section">

<h3><a name="Groovy_Grape"></a>Groovy Grape</h3><a name="Groovy_Grape"></a>

<div class="source"><pre class="prettyprint">@Grapes(
@Grab(group='org.apache.streams', module='streams-master', version='0.3-incubating-SNAPSHOT')
)</pre>

</div>

<div>

<div class="section">

<h3><a name="Gradle"></a>Gradle</h3><a name="Gradle"></a>

<div class="source"><pre class="prettyprint">compile 'org.apache.streams:streams-master:0.3-incubating-SNAPSHOT'</pre>

</div>

</div>

<div class="section">
<h3><a name="Leiningen"></a>Leiningen</h3><a name="Leiningen"></a>

<div class="source">

<pre class="prettyprint">[org.apache.streams/streams-master &quot;0.3-incubating-SNAPSHOT&quot;]</pre>

</div>

</div>

<div class="section">

<h3><a name="SBT"></a>SBT</h3><a name="SBT"></a>

<div class="source">

<pre class="prettyprint">libraryDependencies += &quot;org.apache.streams&quot; % &quot;streams-master&quot; % &quot;0.3-incubating-SNAPSHOT&quot;</pre>

</div>

</div>

</div>

</div>

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
