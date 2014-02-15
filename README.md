webapi-scala
============


This maven package provides Scala implementations for the following web search APIs:

* Google Custom Search [API](https://developers.google.com/custom-search/)
* Bing search [API](http://go.microsoft.com/fwlink/?LinkID=272626&clcid=0x409)
* Faroo search [API](http://www.faroo.com/hp/api/api.html)
* Twitter search/stream

More APIs will be added in the future ...

## Example Output

**Google Query**: "chase police"

Showing snippets from the top 10 results out of ~160000000 (_retrieved on 02.15.2014_):

```
Jul 22, 2006 ... police chase. ... Police Chase: Suspect tricks police by taking off coat and walking away - 10.02.13by timlopez346,124 views; 14:46
RAW VIDEO: Police chase, shootout with the man suspected of a series of home invasions and sexual assaults near USF campus Thursday night. WARNING: ...
Aug 30, 2007 ... Westchester County, New York- Police chase a skilled driver in a white limousine through the streets who has a trick or two up his sleeve..
Jan 9, 2014 ... A man led police on an hour-long pursuit through southeast San Diego Thursday that apparently ended in gunfire.. Visit 10News.com for ...
Sep 18, 2013 ... http://smarturl.it/AssociatedPress A high-speed chase ended in a violent crash in southwestern Broward County, Florida. Police took cover after ...
For fans of high-speed police chases and police actions videos. Whether filmed by a crew (à la COPS), by a dashcam or other. Police shoot outs and hostage ...
Jul 5, 2013 ... Check Out http://bloxstarzlifestyle.com & http://streetstuntparts.com Insane high speed police chase actual dash cam footage of motorcycle stunt ...
The death of university student Clea Rose following a police chase in Canberra sparked major recriminations over police pursuit policies. Ole Christian Bach ...
Another police chase through Miami-Dade County for an armed robbery ... Police have caught a crime suspect which led them on a chase through the City of ...
Strive for your personal best and enjoy the beautiful and rejuvenating Leech Lake area at the Chase the Police Triathlon, hosted by the Walker Area Community ...

```


**Bing Query**: "chase police"

Showing snippets from the top 10 results out of ~34800000 (_retrieved on 02.15.2014_):

```
8:48 Police Car Challenge Part 1 - Top Gear - BBC by TopGear Featured 4,520,756; 1:16:53 World's Scariest Most Shocking Stopped by the Police (Must Watch ...
Police. The Chevy Chase Village Police Department is a nationally accredited Law Enforcement Agency whose mission is to serve and protect the residents of the Village ...
The Town of Chase City Police Departments goal is to provide both Professional and Dedicated Law enforcement Service to the wonderful citizens of Chase City, VA.
Police chase car hits and kills two cyclists Police were in pursuit of a black BMW convertible in Purley on Thames when it hit the two cyclists
Police Fury Exterminate the terrorists in the target zone. Friends Chase Go to guys wearing the same dress color and avoid others. Moon Police You are in charge of ...
A car chase is the vehicular pursuit of suspects by law enforcers. Car chases are often captured on film and broadcast due to the availability of video footage ...
COLUMBUS, Ohio (AP) — Dash camera video recorded by two Ohio troopers and released Tuesday shows a car racing past at high speed, followed by the af...
Grand Prairie police arrested a man early Friday following a chase where the man forced his cousin from the driver's seat with his an 8-year-old child in ...
Strive for your personal best and enjoy the beautiful and rejuvenating Leech Lake area at the Chase the Police Triathlon, hosted by the Walker Area Community Center ...
In Police Chase flash game, it's your job to follow the criminal's getaway car and do enough damage by ramming it with your squad car.
```


**Faroo Query**: "chase police"

Showing snippets from the top 10 results (_retrieved on 02.15.2014_):

```
Answer (1 of 26): Simple: Elude law enforcement in a jurisdiction with a strict pursuit policy. In my department, unless a suspect vehicle was ...
Get A Motorcycle Police Chase Race Track Game PLUS on the App Store. See screenshots and ratings, and read customer reviews.
It must have been some party 28-year old Erin B. Holdsworth was coming from on the night of October 11, 2011. Police in the Ohio town of Bainbridge ...
A PREGNANT mother of six who led police on a car chase to her own Canberra driveway in the early hours of Saturday morning has been granted bail. ...
Three people have died when their car crashed and rolled after being chased by police west of Alice Springs.
Stolen milk tanker's tyres creamed in police chase
Hoon driver clocked at 210km/h in police chase
A Calgary man is facing charges after Saskatchewan RCMP were taken on a wild and crazy chase that lasted more than 90 minutes and covered 120 ...
... car went crashing into her car and home Wednesday after the driver eluded police through city streets.  It's just one of five car chases and ...
... Film-making duo The Theory decided to make the best of one of those gadgets, the Micro Vision projector, by crafting a clever police chase scene. ...
```


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
src/main/scala/search.web.RunBingSearcher
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

