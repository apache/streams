package org.apache.streams.sprinklr;

import org.apache.juneau.remote.RemoteInterface;
import org.apache.juneau.remoteable.QueryIfNE;
import org.apache.juneau.rest.client.remote.RemoteMethod;
import org.apache.streams.sprinklr.api.PartnerAccountsResponse;

@RemoteInterface(path = "https://api2.sprinklr.com/api/v1/bootstrap/resources")
public interface Bootstrap {

    @RemoteMethod(method = "GET")
    public PartnerAccountsResponse getPartnerAccounts();

}