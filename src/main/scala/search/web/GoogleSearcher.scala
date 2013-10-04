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

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.customsearch.Customsearch
import com.google.api.services.customsearch.CustomsearchRequestInitializer
import com.google.api.services.customsearch.model.Result
import search.Searcher
import java.util
import java.util.Date
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import utils.Constants

/** A web searcher that uses the Google Custom Searcher API to search web contents.
  *
  * Link to API details: https://developers.google.com/custom-search/
  *
  * See [[search.web.RunGoogleSearcher]] for an example usage.
  * 
  * @constructor Create a web searcher using Google search.
  * @param apiKey The Google API access key to use.
  * @param engineUID The custom search engine ID to use.
  * @param params Additional search-specific parameters (stored as key-value pairs).
  * 
  * @author trananh
  */
class GoogleSearcher(
    var apiKey: String = Constants.GoogleProperties.apiKey,
    var engineUID: String = Constants.GoogleProperties.cseID,
    var params: mutable.HashMap[String,Any] = new mutable.HashMap[String,Any]())
  extends Searcher {

  /** Perform a Google search.
    *
    * @param query The query.
    * @param numHits Maximum number of search hits to return. This value is
    *                capped by Google's allowance (currently limited at 100).
    * @param numPageHits Maximum number of search hits per search. This value is
    *                    capped by Google's allowance (currently limited at 10).
    *
    * @return List of Google search results.
    */
  def google(query: String,
             numHits: Integer = GoogleSearcher.DEFAULT_MAX_HITS,
             numPageHits: Integer = GoogleSearcher.DEFAULT_MAX_PAGE_HITS)
    : java.util.List[Result] = {

    // Sanity check
    val max = Math.min(numHits, GoogleSearcher.DEFAULT_MAX_HITS)
    val num = Math.min(numPageHits, GoogleSearcher.DEFAULT_MAX_PAGE_HITS)

    // Build custom search engine
    val builder = new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(), new GsonFactory(), null)
    builder.setCustomsearchRequestInitializer(new CustomsearchRequestInitializer(apiKey))
    builder.setApplicationName(GoogleSearcher.APPLICATION_NAME)

    // Build request
    val request = builder.build().cse().list(query)
    request.setCx(engineUID)
    request.setNum(num)

    // Get results
    val results: java.util.List[Result] = new util.ArrayList[Result]()

    // Execute search
    var search = request.execute()
    results.addAll(search.getItems)
    while (search.getQueries.containsKey("nextPage") &&
      search.getQueries.get("nextPage").get(0).getStartIndex <= max - num + 1) {
      request.setStart(search.getQueries.get("nextPage").get(0).getStartIndex.toLong)
      search = request.execute()
      results.addAll(search.getItems)
    }

    results
  }

  /** Return the total number of hits from the search.
    *
    * @param query The query.
    *
    * @return Total number of hits.
    */
  def totalResults(query: String): Long = {
    // Build custom search engine
    val builder = new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(), new GsonFactory(), null)
    builder.setCustomsearchRequestInitializer(new CustomsearchRequestInitializer(apiKey))
    builder.setApplicationName(GoogleSearcher.APPLICATION_NAME)

    // Build request
    val request = builder.build().cse().list(query)
    request.setCx(engineUID)
    request.setNum(1)

    // Execute search
    val search = request.execute()
    search.getQueries.get("request").get(0).getTotalResults
  }

  /** Extract snippets Google search results.
    *
    * @param query The query.
    * @param numHits Maximum number of search hits to return. This value is
    *                capped by Google's allowance (currently limited at 100).
    * @param numPageHits Maximum number of search hits per search. This value is
    *                    capped by Google's allowance (currently limited at 10).
    *
    * @return Array of formatted text snippets extracted from Google search results.
    */
  def snippets(query: String,
               numHits: Integer = GoogleSearcher.DEFAULT_MAX_HITS,
               numPageHits: Integer = GoogleSearcher.DEFAULT_MAX_PAGE_HITS): Array[String] = {

    val start = new Date()
    if (Constants.DEBUG) println("Google search snippets started for: " + query)

    // Google search for results
    val results: java.util.List[Result] = google(query, numHits, numPageHits)

    // Extract snippets from search results
    val snippets = new ArrayBuffer[String]()
    val it = results.iterator()
    while (it.hasNext) {
      val r = it.next
      snippets += r.getSnippet
    }

    if (Constants.DEBUG) {
      println("Retrieved " + snippets.length + " hits")
      println("Search completed. Total time: " + (new Date().getTime - start.getTime) + " milliseconds")
    }

    snippets.toArray
  }
  
  
  /******** Searcher implementation starts here ********/
  
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
  def searchHighlight(queryStr: String, numHits: Integer): Array[String] = {
    snippets(queryStr, numHits,
      params.getOrElse("numPageHits", GoogleSearcher.DEFAULT_MAX_PAGE_HITS).asInstanceOf[Integer])
  }

}

/** Google search singleton object */
object GoogleSearcher {

  /** Currently, we can only retrieve up to DEFAULT_MAX_PAGE_HITS number
    * of search hits at a time, for up to DEFAULT_MAX_HITS total hits overall
    * for any particular query.
    *
    * This is a limitation imposed by the current Google Custom Search API.
    */
  val DEFAULT_MAX_HITS = 100
  val DEFAULT_MAX_PAGE_HITS = 10
  val APPLICATION_NAME = "demo-application/1.0"
}

/** Google search demo */
object RunGoogleSearcher {

  def main(args: Array[String]) {

    // Query string
    val queryStr = "google search api"

    // Search web
    val searcher = new GoogleSearcher()
    val results = searcher.snippets(queryStr)
    for (r <- results) {
      println(r)
    }

    // Print summary statistics
    println("\nDoc frequency: " + searcher.docFreq(queryStr))
  }
  
}