/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.streams.twitter.api;

import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.Place;

import org.apache.juneau.http.annotation.Query;
import org.apache.juneau.remote.RemoteInterface;
import org.apache.juneau.rest.client.remote.RemoteMethod;

import java.util.List;

/**
 * Interface for /geo methods.
 *
 * @see <a href="https://developer.twitter.com/en/docs/geo/place-information/overview">https://developer.twitter.com/en/docs/geo/place-information/overview</a>
 * @see <a href="https://developer.twitter.com/en/docs/geo/places-near-location/overview">https://developer.twitter.com/en/docs/geo/places-near-location/overview</a>
 */
@RemoteInterface(path = "https://api.twitter.com/1.1/geo")
public interface Geo {

  /**
   * Returns all the information about a known place.
   *
   * @param place_id A place in the world. These IDs can be retrieved from geo/reverse_geocode.
   * @return {@link org.apache.streams.twitter.pojo.Place}
   * @see <a href="https://developer.twitter.com/en/docs/geo/place-information/api-reference/get-geo-id-place_id">https://developer.twitter.com/en/docs/geo/place-information/api-reference/get-geo-id-place_id</a>
   *
   */
  @RemoteMethod(method ="GET", path = "/id/{place_id}.json")
  public Place geoid(@Query("place_id") String place_id);

  /**
   * Search for places that can be attached to a statuses/update. Given a latitude and a longitude pair, an IP address, or a name, this request will return a list of all the valid places that can be used as the place_id when updating a status.
   *
   * @param parameters A place in the world. These IDs can be retrieved from geo/reverse_geocode.
   * @return {@link org.apache.streams.twitter.api.GeoSearchResponse}
   * @see <a href="https://developer.twitter.com/en/docs/geo/places-near-location/api-reference/get-geo-search">https://developer.twitter.com/en/docs/geo/places-near-location/api-reference/get-geo-search</a>
   *
   */
  @RemoteMethod(method ="GET", path = "/search.json")
  public GeoSearchResponse geosearch(@Query(name = "*", skipIfEmpty = true) GeoSearchRequest parameters);

  /**
   * Given a latitude and a longitude, searches for up to 20 places that can be used as a place_id when updating a status.
   *
   * @param parameters A place in the world. These IDs can be retrieved from geo/reverse_geocode.
   * @return {@link org.apache.streams.twitter.api.GeoSearchResponse}
   * @see <a href="https://developer.twitter.com/en/docs/geo/places-near-location/api-reference/get-geo-reverse_geocode">https://developer.twitter.com/en/docs/geo/places-near-location/api-reference/get-geo-reverse_geocode</a>
   *
   */
  @RemoteMethod(method ="GET", path = "/reverse_geocode.json")
  public GeoSearchResponse reverseGeocode(@Query(name = "*", skipIfEmpty = true) GeoSearchRequest parameters);

}
