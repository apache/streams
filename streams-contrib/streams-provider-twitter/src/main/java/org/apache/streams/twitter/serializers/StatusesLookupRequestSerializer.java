package org.apache.streams.twitter.serializers;

import org.apache.streams.twitter.api.StatusesLookupRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.juneau.BeanContext;
import org.apache.juneau.BeanMap;
import org.apache.juneau.BeanSession;
import org.apache.juneau.PropertyStore;
import org.apache.juneau.serializer.OutputStreamSerializer;
import org.apache.juneau.serializer.SerializerSession;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Maybe this can be used to populate the parameters other than id
 * on the restclient when calling statuses/lookup?
 */
public class StatusesLookupRequestSerializer extends OutputStreamSerializer {

  public StatusesLookupRequestSerializer(PropertyStore propertyStore) {
    super(propertyStore);
  }

  @Override
  protected void doSerialize(SerializerSession serializerSession, Object o) throws Exception {

    assertThat("o instanceof StatusesLookupRequest", o instanceof StatusesLookupRequest);

    BeanSession beanSession = BeanContext.DEFAULT.createSession();

    BeanMap<StatusesLookupRequest> m = beanSession.toBeanMap((StatusesLookupRequest)o);

    List<String> paramList = m.entrySet().stream().map(param -> param.getKey()+"="+param.getValue().toString()).collect(Collectors.toList());

    String params = StringUtils.join(paramList, '&');

    StringReader reader = new StringReader(params);
    IOUtils.copy(reader, serializerSession.getOutputStream(), "UTF-8");
  }

}
