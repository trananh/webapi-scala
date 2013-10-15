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

package utils

import java.io.{FileInputStream, File}
import java.util.Properties

/** Place to hold all global (i.e., system level) constants and properties.
  * This class will try to look for a properties file named "config.properties" and load
  * the properties if the file exists.
  *
  * @author trananh
  */
object Constants {

  /** Debug flag */
  val DEBUG = true


  /** Project root & config */
  val CONFIG_FILE = "config.properties"
  val APPLICATION_ROOT = System.getProperty("user.dir")


  /** Load configuration properties if file exists */
  private val propertiesFile = new File(APPLICATION_ROOT + "/" + CONFIG_FILE)
  private val config = new Properties()
  if (propertiesFile.exists()) {
    config.load(new FileInputStream(propertiesFile))
  }


  /** Google search properties */
  object GoogleProperties {
    var apiKey = "UNKNOWN"
    var cseID = "UNKNOWN"

    if (config.containsKey("google.apiKey")) apiKey = config.getProperty("google.apiKey")
    if (config.containsKey("google.cseID")) cseID = config.getProperty("google.cseID")
  }


  /** Twitter properties. */
  object TwitterProperties {
    var oauthConsumerKey = "UNKNOWN"
    var oauthConsumerSecret = "UNKNOWN"
    var oauthAccessToken = "UNKNOWN"
    var oauthAccessTokenSecret = "UNKNOWN"

    if (config.containsKey("twitter.oauth.consumerKey"))
      oauthConsumerKey = config.getProperty("twitter.oauth.consumerKey")
    if (config.containsKey("twitter.oauth.consumerSecret"))
      oauthConsumerSecret = config.getProperty("twitter.oauth.consumerSecret")
    if (config.containsKey("twitter.oauth.accessToken"))
      oauthAccessToken = config.getProperty("twitter.oauth.accessToken")
    if (config.containsKey("twitter.oauth.accessTokenSecret"))
      oauthAccessTokenSecret = config.getProperty("twitter.oauth.accessTokenSecret")
  }


  /** Faroo search properties */
  object FarooProperties {
    var apiKey = "UNKNOWN"

    if (config.containsKey("faroo.apiKey")) apiKey = config.getProperty("faroo.apiKey")
  }


  /** Bing search properties */
  object BingProperties {
    var accountKey = "UNKNOWN"

    if (config.containsKey("bing.accountKey")) accountKey = config.getProperty("bing.accountKey")
  }

}
