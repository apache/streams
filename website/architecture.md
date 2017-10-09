---
layout: default
title:
description:
group:

---

## Architecture

Apache Streams consists of a loosely coupled set of modules with specific capabilities, such as:

 - collecting data.
 - transforming or filter data
 - storing and retrieving documents and metadata from databases
 - binding streams components to other systems
 - facilitating starting and stopping of streams.

![Architecture](architecture.dot.svg)

#### Modules

Each module has it's own POM and dependency tree.  Each stream deployment needs to import only the modules it needs for what it wants to do.

#### Schemas

Streams also contains libraries and patterns for specifying document schemas, converting documents to and from ActivityStreams format, and generating source and resource files for binding to data objects in those formats.

#### Pipelines

A Pipeline is a set of collection, processing, and storage components structured in a directed graph (cycles may be permitted) which is packaged, deployed, started, and stopped together.

#### Runtimes

A Runtime is a module containing bindings that help setup and run a pipeline.  Runtimes may submit pipeline binaries to an existing cluster, or may launch the process(es) to execute the stream directly.  

### Example

A standard usage of Apache Streams is to collect, normalize, and archive activity across multiple networks.

![Example](example.dot.svg)

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
