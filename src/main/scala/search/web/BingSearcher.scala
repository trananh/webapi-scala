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
import java.io.{InputStreamReader, BufferedReader}
import java.net.{HttpURLConnection, URL, URLEncoder}
import org.apache.commons.codec.binary.Base64
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import utils.Constants
import search.Searcher

/** A web searcher that uses the Azure Bing Search API to search web contents.
  *
  * Link to API details: http://go.microsoft.com/fwlink/?LinkID=272626&clcid=0x409
  *
  * @constructor Create a web searcher using Bing search.
  * @param accountKey The Bing account key to use.
  * @param params Additional search-specific parameters (stored as key-value pairs).
  *
  * @author trananh
  */
class BingSearcher(
                    var accountKey: String = Constants.BingProperties.accountKey,
                    var params: mutable.HashMap[String,Any] = new mutable.HashMap[String,Any]())
  extends Searcher {

  // Encode account key
  val accountKeyEncoded = new String(Base64.encodeBase64((accountKey + ":" + accountKey).getBytes()))

  /** Perform a Bing News search.
    *
    * We use a Composite search in order to have access to the total results,
    * but we do filter the contents to be from "news" only.
    *
    * @param query The query.
    * @param numPageHits Maximum number of search hits per search (page). This value is
    *                    capped by Bing's allowance (currently limited at 50).
    * @param startPoint Start position of results to request (default to 0).
    *
    * @return Bing search response.
    */
  def searchNews(query: String,
                 numPageHits: Integer = BingSearcher.DEFAULT_NEWS_MAX_PAGE_HITS,
                 startPoint: Integer = BingSearcher.DEFAULT_START_POINT): BingSearch =
    search(query, numPageHits, startPoint, "news")

  /** Perform a Bing search.
    *
    * We use a Composite search in order to have access to the total results,
    * but we do filter the contents to be "web" only.
    *
    * @param query The query.
    * @param numPageHits Maximum number of search hits per search (page). This value is
    *                    capped by Bing's allowance (currently limited at 50).
    * @param startPoint Start position of results to request (default to 0).
    * @param sources Source of the search (currently support web and news, default to web).
    *
    * @return Bing search response.
    */
  def search(query: String,
             numPageHits: Integer = BingSearcher.DEFAULT_WEB_MAX_PAGE_HITS,
             startPoint: Integer = BingSearcher.DEFAULT_START_POINT,
             sources: String = "web"): BingSearch = {

    // Sanity checks
    var length = Math.min(numPageHits, BingSearcher.DEFAULT_WEB_MAX_PAGE_HITS)
    length = Math.max(length, 1)

    // Create the query string
    val market = "en-us"
    val searchURL = BingSearcher.BASE_URL + "Composite?" +
      "Sources=" + URLEncoder.encode("'" + sources + "'", "UTF-8") + "&" +
      "Query=" + URLEncoder.encode("'" + query + "'", "UTF-8") + "&" +
      "Market=" + URLEncoder.encode("'" + market + "'", "UTF-8") + "&" +
      ("$top=" + length) + "&" + ("$skip=" + startPoint) + "&" + ("$format=Json")
    val url = new URL(searchURL)

    // Query using native HttpConnection
    val jsonResponse = new StringBuilder()
    val conn = url.openConnection().asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("GET")
    conn.setRequestProperty("Authorization", String.format("Basic %s", accountKeyEncoded))
    val br = new BufferedReader(new InputStreamReader(conn.getInputStream))
    var output = Option(br.readLine())
    while (output.isDefined) {
      jsonResponse.append(output.get)
      output = Option(br.readLine())
    }
    conn.disconnect()

    // Convert the json response string to Bing search results.
    new Gson().fromJson[BingResponse](jsonResponse.toString(), classOf[BingResponse]).d.results(0)
  }

  /** Perform a Bing search and return all top results.
    *
    * @param query The query.
    * @param numHits Maximum number of top search hits to return.
    *
    * @return Array of top Bing search results.
    */
  def searchAllPages(query: String, numHits: Integer = BingSearcher.DEFAULT_MAX_HITS): Array[BingWebResult] = {
    val results = new ListBuffer[BingWebResult]()
    var startPoint = BingSearcher.DEFAULT_START_POINT
    var nextBatch = search(query, numPageHits = BingSearcher.DEFAULT_WEB_MAX_PAGE_HITS, startPoint = startPoint)
    var totalHits = if (nextBatch.WebTotal.isEmpty) 0 else nextBatch.WebTotal.toInt
    while (results.length < math.min(numHits, totalHits) && nextBatch.Web.length > 0) {
      results.appendAll(nextBatch.Web)
      startPoint += nextBatch.Web.size
      nextBatch = search(query, numPageHits = BingSearcher.DEFAULT_WEB_MAX_PAGE_HITS, startPoint = startPoint)
      totalHits = nextBatch.WebTotal.toInt
    }
    results.slice(0, numHits).toArray
  }

  /** Extract snippets from top Bing search results.
    *
    * @param query The query.
    * @param numHits Maximum number of top search hits to return. This value is
    *                capped by Bing (currently limited at 1000).
    *
    * @return Array of formatted text snippets extracted from top Bing search results.
    */
  def snippets(query: String, numHits: Integer = BingSearcher.DEFAULT_MAX_HITS): Array[String] = {
    searchAllPages(query, numHits = numHits).map(r => r.Description)
  }

  /** Return the overall total number of hits estimated from the search.
    *
    * @param query The query.
    *
    * @return Estimated total number of hits.
    */
  def totalResults(query: String): Long = {
    val total = search(query, numPageHits = 1).WebTotal
    if (total.isEmpty)
      return 0
    total.toLong
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
  def searchHighlight(queryStr: String, numHits: Integer): Array[String] = ???

}

/** A Bing web result.
  * API Link: http://go.microsoft.com/fwlink/?LinkID=272626&clcid=0x409
  * @author trananh
  */
case class BingWebResult(ID: String,              /* Identifier */
                         Title: String,         /* Text specified in the HTML <title> tag of the page */
                         Description: String,   /* Description text of the web result */
                         DisplayUrl: String,    /* Web URL to display to the user */
                         Url: String            /* Full URL of the web result */
                          )

/** A Bing news result.
  * API Link: http://go.microsoft.com/fwlink/?LinkID=272626&clcid=0x409
  * @author trananh
  */
case class BingNewsResult(ID: String,             /* Identifier */
                          Title: String,        /* Headline for this result */
                          Url: String,          /* URL of this article */
                          Source: String,       /* Organization responsible for this article */
                          Description: String   /* Representative sample of this result */
                           )

/** A Bing search response, which contains a collection of search results
  * plus other metadata about the search.
  * API Link: http://go.microsoft.com/fwlink/?LinkID=272626&clcid=0x409
  * @author trananh
  */
case class BingSearch(ID: String,
                      WebTotal: String,                   /* Number of web results */
                      WebOffset: String,                  /* Current offset */
                      ImageTotal: String,
                      ImageOffset: String,
                      VideoTotal: String,
                      VideoOffset: String,
                      NewsTotal: String,                  /* Number of news results */
                      NewsOffset: String,                 /* Current offset */
                      SpellingSuggestionsTotal: String,
                      AlteredQuery: String,
                      AlterationOverrideQuery: String,
                      Web: Array[BingWebResult],
                      News: Array[BingNewsResult]
                       )

/** Metadata structure of a Bing search response */
case class BingResponseResults(results: Array[BingSearch])
case class BingResponse(d: BingResponseResults)


/** Bing search singleton object */
object BingSearcher {
  /* Base query URL */
  val BASE_URL = "https://api.datamarket.azure.com/Bing/Search/"

  val DEFAULT_MAX_HITS = 2000                 /* maximum number of hits per search */
  val DEFAULT_WEB_MAX_PAGE_HITS = 50          /* maximum length of each query */
  val DEFAULT_NEWS_MAX_PAGE_HITS = 15         /* maximum length of each query */
  val DEFAULT_START_POINT = 0                 /* default start position of results requested */
}


object RunBingSearcher {

  def main(args: Array[String]) {

    // Query string
    val queryStr = "bing search api"

    // Search web using Bing
    val searcher = new BingSearcher()
    val snippets = searcher.snippets(queryStr)
    snippets.foreach(s => println(s))

    // Print summary statistics
    println("\nTop results: " + snippets.size)
    println("\nTotal results: " + searcher.totalResults(queryStr))
  }

}
