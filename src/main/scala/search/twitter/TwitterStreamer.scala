// Copyright 2013 trananh
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package search.twitter

import java.io.{FileWriter, IOException, File}
import twitter4j._
import twitter4j.conf.{Configuration, ConfigurationBuilder}
import utils.Constants

/** A Twitter streamer that uses the twitter4j API to constantly retrieve relevant
  * tweets.
  *
  * See [[search.web.RunFarooStreamer]] for an example usage.
  *
  * @constructor Create a Twitter streamer using twitter4j.
  * @param config The twitter connection configuration to use.
  *
  * @author trananh
  */
class TwitterStreamer(
    var config: Configuration = new ConfigurationBuilder()
      .setDebugEnabled(Constants.DEBUG)
      .setOAuthConsumerKey(Constants.TwitterProperties.oauthConsumerKey)
      .setOAuthConsumerSecret(Constants.TwitterProperties.oauthConsumerSecret)
      .setOAuthAccessToken(Constants.TwitterProperties.oauthAccessToken)
      .setOAuthAccessTokenSecret(Constants.TwitterProperties.oauthAccessTokenSecret)
      .build()) {

  /** Creates a TwitterStreamFactory with the given configuration. */
  val streamFactory = new TwitterStreamFactory(config)


  /** Streams relevant tweets from twitter.
    * @param query Any filter restrictions on the stream (default to None).
    * @param outFile A file to redirect the stream into (default to None).
    */
  def stream(query: Option[Array[String]] = None, outFile: Option[File] = None) {

    if (Constants.DEBUG) println("\n\n*****\nStarting stream\n*****\n\n")
    if (outFile.isDefined) {
      if (!outFile.get.exists()) outFile.get.createNewFile()
      if (Constants.DEBUG) println("Streaming to file " + outFile.get.getPath + "\n")
    }
    Thread.sleep(3000)

    // Setup listener to print out incoming tweets.
    val listener = new StatusListener {

      def onStatus(status: Status) {
        val statusMessage = "@" + status.getUser.getScreenName + ": " + status.getText
        if (outFile.isDefined) {
          // Write to outfile, if it's defined.
          try {
            val writer = new FileWriter(outFile.get, true)
            writer.write(statusMessage)
            writer.write("\n")
            writer.close()
          } catch {
            case ioe: IOException => println("IOException: " + ioe.getMessage())
            case e: Exception => println(e.printStackTrace())
          }
        } else {
          // If no outfile, then print to console.
          println(statusMessage)
        }
      }

      def onException(ex: Exception) { ex.printStackTrace() }

      def onStallWarning(warning: StallWarning) {}
      def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
      def onScrubGeo(userId: Long, upToStatusId: Long) {}
      def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}
    }

    // Initialize stream
    val twitterStream = streamFactory.getInstance()
    twitterStream.addListener(listener)

    // Create if a filtered stream if necessary.
    if (query.isDefined) {
      // Set up filter query
      val filterQuery = new FilterQuery().track(query.get)

      // filter() internally creates a thread which manipulates TwitterStream
      // and calls the adequate listener methods continuously.
      twitterStream.filter(filterQuery)
    } else {
      // sample() internally creates a thread which manipulates TwitterStream
      // and calls the adequate listener methods continuously.
      twitterStream.sample()
    }
  }

}

/** Stream Twitter. */
object RunTwitterStreamer {

  def main(args: Array[String]) {

    // Query string
    val queryStr = "twitter stream"

    // Path to out file, or leave empty to print to standard out stream
    val filePath = ""

    // Parse arguments
    val query = if (queryStr.length > 0) Option(queryStr.split(" ")) else None
    val outFile = if (filePath.length > 0) Option(new File(filePath)) else None

    // Stream tweets
    val streamer = new TwitterStreamer()
    streamer.stream(query, outFile)
  }

}
