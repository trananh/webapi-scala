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

import scala.collection.JavaConversions._
import scala.collection.mutable
import twitter4j._
import twitter4j.conf.{Configuration, ConfigurationBuilder}
import utils.Constants
import search.Searcher

/** A Twitter searcher that uses the twitter4j API to search for relevant tweets.
  *
  * See [[search.web.RunTwitterSearcher]] for an example usage.
  *
  * @constructor Create a Twitter searcher using twitter4j.
  * @param config The twitter connection configuration to use.
  * @param params Additional search-specific parameters (stored as key-value pairs).
  *
  * @author trananh
  */
class TwitterSearcher(
    var config: Configuration = new ConfigurationBuilder()
      .setDebugEnabled(Constants.DEBUG)
      .setOAuthConsumerKey(Constants.TwitterProperties.oauthConsumerKey)
      .setOAuthConsumerSecret(Constants.TwitterProperties.oauthConsumerSecret)
      .setOAuthAccessToken(Constants.TwitterProperties.oauthAccessToken)
      .setOAuthAccessTokenSecret(Constants.TwitterProperties.oauthAccessTokenSecret)
      .build(),
    var params: mutable.HashMap[String,Any] = new mutable.HashMap[String,Any]())
  extends Searcher {

  /** Creates a TwitterFactory with the given configuration. */
  val twitterFactory = new TwitterFactory(config)


  /** Returns tweets that match a specified query.
    * @param query The query.
    * @return List of tweet results.
    */
  def search(query: String): java.util.List[Status] = {
    val twitter = twitterFactory.getInstance()
    val result = twitter.search(new Query(query))
    result.getTweets
  }


  /******** Searcher implementation starts here ********/

  /** Close all open streams and clear memory usage */
  def close() { /* nothing to close for now */ }

  /** Return the term frequency (i.e., the total number of time the term is found).
    *
    * @param term The query term.
    *
    * @return Total occurrence count of the term.
    */
  def termFreq(term: String): Long = search(term).length

  /** Return the document frequency for the term.
    *
    * @param term The query term.
    *
    * @return Number of documents containing the term.
    */
  def docFreq(term: String): Long = search(term).length

  /** Search and return highlighted snippets from the results.
    *
    * @param queryStr The query string.
    * @param numHits Maximum number of search hits to return.
    *
    * @return Array of highlighted text snippets from the results.
    */
  def searchHighlight(queryStr: String, numHits: Integer): Array[String] = {
    val results = search(queryStr).map(status => "@" + status.getUser.getScreenName + ": " + status.getText)
    results.slice(0, numHits).toArray[String]
  }

}

/** Search twitter. */
object RunTwitterSearcher {

  def main(args: Array[String]) {

    // Query string
    val queryStr = "twitter search api"

    // Search twitter and print results
    val searcher = new TwitterSearcher()
    val results = searcher.search(queryStr)
    results.foreach(status => println("@" + status.getUser.getScreenName + ": " + status.getText))
  }

}
