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

package search.web

import com.google.gson.Gson
import search.Searcher
import java.io.{InputStreamReader, BufferedReader}
import java.net.{HttpURLConnection, URL, URLEncoder}
import java.util.Date
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import utils.Constants

/** A web searcher that uses the Faroo Search API to search web contents.
  *
  * Link to API details: http://www.faroo.com/hp/api/api.html
  *
  * See [[search.web.RunFarooSearcher]] for an example usage.
  *
  * @constructor Create a web searcher using Faroo search.
  * @param apiKey The Faroo API authentication key to use.
  * @param params Additional search-specific parameters (stored as key-value pairs).
  *
  * @author trananh
  */
class FarooSearcher(
    var apiKey: String = Constants.FarooProperties.apiKey,
    var params: mutable.HashMap[String,Any] = new mutable.HashMap[String,Any]())
  extends Searcher {

  /** Wait (by hanging the thread) until we can make another Faroo query without exceeding
    * the query rate limit.
    */
  def waitForQueryLimit() {
    if (FarooSearcher.lastQueryTime.isDefined) {
      val elapsed =  new Date().getTime - FarooSearcher.lastQueryTime.get.getTime
      if (elapsed < FarooSearcher.QUERY_RATE_LIMIT) {
        Thread.sleep(FarooSearcher.QUERY_RATE_LIMIT - elapsed)
      }
    }
  }

  /** Perform a Faroo web search.
    *
    * @param query The query.
    * @param numPageHits Maximum number of search hits per search (page). This value is
    *                    capped by Faroo's allowance (currently limited at 10).
    * @param startPoint Start position of results to request (default to 1).
    *
    * @return Faroo search response.
    */
  def search(query: String,
             numPageHits: Integer = FarooSearcher.DEFAULT_MAX_PAGE_HITS,
             startPoint: Integer = FarooSearcher.DEFAULT_START_POINT): FarooSearch = {

    // Sanity checks
    var length = Math.min(numPageHits, FarooSearcher.DEFAULT_MAX_PAGE_HITS)
    length = Math.max(length, 1)

    // Wait until we can make another query
    waitForQueryLimit()

    // Create the query string
    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    val url = new URL(FarooSearcher.BASE_URL + "?q=" + encodedQuery +
      "&start=" + startPoint + "&length=" + length + "&key=" + this.apiKey)

    // Query using native HttpConnection
    val jsonResponse = new StringBuilder()
    val conn = url.openConnection().asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("GET")
    val br = new BufferedReader(new InputStreamReader(conn.getInputStream))
    var output = Option(br.readLine())
    while (output.isDefined) {
      jsonResponse.append(output.get)
      output = Option(br.readLine())
    }
    conn.disconnect()

    // Set last accessed time so we don't go over our rate limit
    FarooSearcher.lastQueryTime = Option(new Date())

    // Convert the json response string to faroo search results.
    new Gson().fromJson[FarooSearch](jsonResponse.toString(), classOf[FarooSearch])
  }

  /** Perform a Faroo search and return all top results.
    *
    * @param query The query.
    * @param numHits Maximum number of top search hits to return. This value is
    *                capped by Faroo (currently limited at 100).
    *
    * @return Array of top Faroo search results.
    */
  def searchAllPages(query: String, numHits: Integer = FarooSearcher.DEFAULT_MAX_HITS): Array[FarooResult] = {
    val results = new ListBuffer[FarooResult]()
    var startPoint = FarooSearcher.DEFAULT_START_POINT
    var nextBatch = search(query, numPageHits = FarooSearcher.DEFAULT_MAX_PAGE_HITS, startPoint = startPoint)
    while (results.length < numHits && nextBatch.results.length > 0) {
      results.appendAll(nextBatch.results)
      startPoint = startPoint + nextBatch.results.size
      nextBatch = search(query, numPageHits = FarooSearcher.DEFAULT_MAX_PAGE_HITS, startPoint = startPoint)
    }
    results.slice(0, numHits).toArray
  }

  /** Extract snippets from top Faroo search results.
    *
    * @param query The query.
    * @param numHits Maximum number of top search hits to return. This value is
    *                capped by Faroo (currently limited at 100).
    *
    * @return Array of formatted text snippets extracted from top Faroo search results.
    */
  def snippets(query: String, numHits: Integer = FarooSearcher.DEFAULT_MAX_HITS): Array[String] = {
    searchAllPages(query, numHits = numHits).map(r => r.kwic)
  }

  /** Return the overall total number of hits estimated from the search.
    *
    * 10.2013: This feature is currently unimplemented by Faroo but is planned for
    * future implementation. See http://www.faroo.com/hp/p2p/faq.html#top100
    *
    * We could consider just returning the top number of hits from the search.
    * {{{
    * search(term).count.toLong
    * }}}
    *
    * @param query The query.
    *
    * @return Estimated total number of hits.
    */
  def totalResults(query: String): Long = ???


  /******** Searcher implementation starts here ********/

  /** Close all open streams and clear memory usage */
  def close() { /* nothing to close for now */ }

  /** Return the term frequency (i.e., the total number of time the term is found).
    *
    * @param term The query term.
    *
    * @return Total occurrence count of the term.
    */
  def termFreq(term: String): Long = totalResults(term)

  /** Return the document frequency for the term.
    *
    * @param term The query term.
    *
    * @return Number of documents containing the term.
    */
  def docFreq(term: String): Long = totalResults(term)

  /** Search and return highlighted snippets from the results.
    *
    * @param queryStr The query string.
    * @param numHits Maximum number of search hits to return.
    *
    * @return Array of highlighted text snippets from the results.
    */
  def searchHighlight(queryStr: String, numHits: Integer): Array[String] = snippets(queryStr, numHits)

}

/** A Faroo related article.  For Trending news only (src=news and empty q).
  * API Link: http://www.faroo.com/hp/api/api.html#returnvalues
  * @author trananh
  */
case class FarooRelated(title: String,        /* Title */
                        url: String,          /* URL */
                        domain: String)       /* Domain */

/** A Faroo search result.
  * API Link: http://www.faroo.com/hp/api/api.html#returnvalues
  * @author trananh
  */
case class FarooResult(title: String,         /* Article title */
                       kwic: String,          /* Article snippet with keyword in context */
                       url: String,           /* Article url */
                       iurl: String,          /* Main article image url */
                       domain: String,        /* Domain */
                       author: String,        /* Article author */
                       news: Boolean,         /* Article is from newspapers, magazines and blogs, OR other */
                       date: Long,            /* Publishing date (milliseconds from the beginning of 1970) */
                       related: Array[FarooRelated])

/** A Faroo search response, which contains a collection of search results
  * plus other metadata about the search.
  * API Link: http://www.faroo.com/hp/api/api.html#returnvalues
  * @author trananh
  */
case class FarooSearch(results: Array[FarooResult],
                       query: String,         /* Query suggestion */
                       count: Integer,        /* Number of results found */
                       start: Integer,        /* Start position of results requested */
                       length: Integer,       /* Number of results requested */
                       time: Long,            /* Search time (pure search latency in milliseconds) */
                       suggestions: Array[String])


/** Faroo search singleton object */
object FarooSearcher {
  val BASE_URL = "http://www.faroo.com/api"   /* base query URL */

  val QUERY_RATE_LIMIT = 1000                 /* milliseconds to wait between queries */

  val DEFAULT_MAX_HITS = 100                  /* maximum number of hits per search (enforced by Faroo) */
  val DEFAULT_MAX_PAGE_HITS = 10              /* maximum length of each query */
  val DEFAULT_START_POINT = 1                 /* default start position of results requested */

  var lastQueryTime: Option[Date] = None      /* time of previous query */
}


/** Faroo search demo */
object RunFarooSearcher {

  def main(args: Array[String]) {

    // Query string
    val queryStr = "search api"

    // Search web using Faroo
    val searcher = new FarooSearcher()
    val snippets = searcher.snippets(queryStr)
    snippets.foreach(s => println(s))

    // Print summary statistics
    println("\nTop results: " + snippets.size)
  }

}
