Title: Apache Streams Code Conventions

The Apache Streams code should follow our code conventions. All code which
is contributed to the project should adhere to these guidelines.

* Use 2 spaces for indentation. No tabs!
* Place open braces on the same line as the declaration, for example:
        <pre><code>
public class Foo extends Bar {
  public static void main(String args[]) {
    try {
      for (int i = 0; i < args.length; i++) {
        System.out.println(Integer.parseInt(args[i]));
      }
    }
    catch(NumberFormatException e) {
      e.printStackTrace();
    }
  }
}
        </code></pre>
* Wrap lines longer than 140 characters. For wrapped lines use an indent of 4 characters.
* Within a class or interface, definitions should be ordered as follows:
    * Class (static) variables
    * Instance variables
    * Constructors
    * Methods
    * Inner classes (or interfaces)

* Do not use '*' package imports (for example import org.apache.streams.*)
* For other cases, we try to follow
  [Java code conventions](http://www.oracle.com/technetwork/java/codeconventions-150003.pdf) as much as possible.

# Formatter and Style files
* A code formatter file for IntelliJ can be found below.
```xml
    <code_scheme name="Streams">
      <option name="CLASS_COUNT_TO_USE_IMPORT_ON_DEMAND" value="99" />
      <option name="NAMES_COUNT_TO_USE_IMPORT_ON_DEMAND" value="99" />
      <option name="PACKAGES_TO_USE_IMPORT_ON_DEMAND">
        <value />
      </option>
      <option name="IMPORT_LAYOUT_TABLE">
        <value>
          <package name="org.apache.streams" withSubpackages="true" static="false" />
          <emptyLine />
          <package name="" withSubpackages="true" static="false" />
          <emptyLine />
          <package name="java" withSubpackages="true" static="false" />
          <package name="javax" withSubpackages="true" static="false" />
          <emptyLine />
          <package name="" withSubpackages="true" static="true" />
        </value>
      </option>
      <MarkdownNavigatorCodeStyleSettings>
        <option name="RIGHT_MARGIN" value="72" />
      </MarkdownNavigatorCodeStyleSettings>
      <codeStyleSettings language="JAVA">
        <option name="RIGHT_MARGIN" value="140" />
        <option name="SPACE_BEFORE_ARRAY_INITIALIZER_LBRACE" value="true" />
        <option name="SPACE_BEFORE_ANNOTATION_ARRAY_INITIALIZER_LBRACE" value="true" />
        <option name="IF_BRACE_FORCE" value="3" />
        <option name="DOWHILE_BRACE_FORCE" value="3" />
        <option name="WHILE_BRACE_FORCE" value="3" />
        <option name="FOR_BRACE_FORCE" value="3" />
        <indentOptions>
          <option name="INDENT_SIZE" value="2" />
          <option name="CONTINUATION_INDENT_SIZE" value="4" />
        </indentOptions>
      </codeStyleSettings>
    </code_scheme>
 ```