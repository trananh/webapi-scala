webapi-scala
============


This maven package provides Scala implementations for the following web search APIs:

* Google Custom Search [API](https://developers.google.com/custom-search/)
* Faroo search [API](http://www.faroo.com/hp/api/api.html)
* Twitter search/stream.


## Compilation

Maven command-line compilation:

```bash
$ cd /path/to/project/webapi-scala
$ mvn compile
```


## Running

You will need the appropriate API Authentication Keys for the search API that you wish
to access.  You can request the keys from the respective search engines.

Once you have the keys, you will need to add them to a configuration file.  Use the
included example file (config.properties.example) as a sample.  Rename the example
file to config.properties and fill in the appropriate keys.

```bash
$ cd /path/to/project/webapi-scala
$ cp config.properties.example config.properties
$ vim config.properties

# Fill in the appropriate API keys and save

```

Once the project has compiled and the configuration file is set, you can try running
the following demos:

```
src/main/scala/search.web.RunGoogleSearcher
src/main/scala/search.web.RunFarooSearcher
src/main/scala/search.twitter.RunTwitterSearcher
src/main/scala/search.twitter.RunTwitterStreamer
```


## Development

Follow the instructions below to import the project into IntelliJ IDEA for development.
Eclipse is another good IDE option.


#### Install IntelliJ IDEA

If you have not done so already, download IntelliJ IDEA Community Edition from
[here](http://www.jetbrains.com/idea/free_java_ide.html).

Install the Scala plugin. See this
[page](http://confluence.jetbrains.com/display/SCA/Getting+Started+with+IntelliJ+IDEA+Scala+Plugin)
for installation details


#### Import webapi-scala

When you are ready to import the project. Open IntelliJ. Go to File/Import Project.
In the file window, select webapi-scala/pom.xml. In the next window, make sure that only
the following options are checked:

	Search for projects recursively
	Project format: .idea (directory based)
	Import Maven projects automatically
	Create IDEA modules for aggregator projects (with 'pom' packaging)
	Keep source and test folders in reimport
	Exclude build directory (%PROJECT_ROOT%/target)
	Use Maven output directories
	Generated source folders: Detect automatically
	Phase to be used for folders update: process-resources
	Automatically download: Sources (YES) Documentation (YES)

Click Next. In the second window, check the Maven project com.github.trananh:web-api. Click Next.
In the next window select SDK 1.6 (should be already selected). Click through the subsequent windows
to finish.

