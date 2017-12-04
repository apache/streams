package org.apache.streams.twitter.search;

import org.apache.streams.twitter.api.ThirtyDaySearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringJoiner;

public class SearchUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(SearchUtil.class);

  public static String toString(ThirtyDaySearchOperator operator) {
    return toStringJoiner(operator).toString();
  }

  public static StringJoiner toStringJoiner(ThirtyDaySearchOperator operator) {
    StringJoiner stringJoiner = new StringJoiner(" ");
    if(operator.getNot()) {
      stringJoiner.add("-");
    }
    stringJoiner.add("(");
    for( String keyword : operator.getKeywords()) {
      stringJoiner.add(keyword);
    }
    for( String emoji : operator.getEmojis()) {
      stringJoiner.add(emoji);
    }
    for( String exact_phrase : operator.getExactPhrases()) {
      stringJoiner.add(phrase(exact_phrase));
    }
    for( String from : operator.getFroms()) {
      stringJoiner.add("from:" + from);
    }
    for( String to : operator.getTos()) {
      stringJoiner.add("to:" + to);
    }
    for( String mention : operator.getMentions()) {
      stringJoiner.add("@" + mention);
    }
    for( String retweets_of : operator.getRetweetsOfs()) {
      stringJoiner.add("retweets_of:" + retweets_of);
    }
    for( String hashtag : operator.getHashtags()) {
      stringJoiner.add("#" + hashtag);
    }
    for( String url : operator.getUrls()) {
      stringJoiner.add("url:" + phrase(url));
    }
    for( String bio : operator.getBios()) {
      stringJoiner.add("bio:" + phrase(bio));
    }
    for( String bio_location : operator.getBioLocations()) {
      stringJoiner.add("bio_location:" + phrase(bio_location));
    }
    for( String bio_name : operator.getBioNames()) {
      stringJoiner.add("bio_name:" + bio_name);
    }
    for( String place : operator.getPlaces()) {
      stringJoiner.add("place:" + place);
    }
    for( String place_country : operator.getPlaceCountrys()) {
      stringJoiner.add("place_country:" + phrase(place_country));
    }
    for( String point_radius : operator.getPointRadiuses()) {
      stringJoiner.add("point_radius:" + point_radius);
    }
    for( String bounding_box : operator.getBoundingBoxes()) {
      stringJoiner.add("bounding_box:" + bounding_box);
    }
    for( String time_zone : operator.getTimeZones()) {
      stringJoiner.add("time_zone:" + time_zone);
    }
    if( operator.getHasImages() ) {
      stringJoiner.add("has:images");
    }
    if( operator.getHasLinks() ) {
      stringJoiner.add("has:links");
    }
    if( operator.getHasMedia() ) {
      stringJoiner.add("has:media");
    }
    if( operator.getHasImages() ) {
      stringJoiner.add("has:image");
    }
    if( operator.getHasVideos() ) {
      stringJoiner.add("has:video");
    }
    if(operator.getAnds().size() > 0) {
      for( ThirtyDaySearchOperator suboperator : operator.getAnds()) {
        stringJoiner.add("AND");
        stringJoiner.add(SearchUtil.toString(suboperator));
      }
    }
    if(operator.getOrs().size() > 0) {
      for( ThirtyDaySearchOperator suboperator : operator.getOrs()) {
        stringJoiner.add("OR");
        stringJoiner.add(SearchUtil.toString(suboperator));
      }
    }
    stringJoiner.add(")");
    return stringJoiner;
  }

  static String phrase(String in) {
    if( in.contains(" ")) {
      return "\"" + in + "\"";
    } else {
      return in;
    }
  }

}
