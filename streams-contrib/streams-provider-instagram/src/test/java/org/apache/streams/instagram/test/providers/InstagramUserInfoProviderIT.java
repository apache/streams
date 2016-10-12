package org.apache.streams.instagram.test.providers;

import com.google.common.collect.Lists;
import org.apache.streams.instagram.provider.recentmedia.InstagramRecentMediaProvider;
import org.apache.streams.instagram.provider.userinfo.InstagramUserInfoProvider;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

/**
 * Created by sblackmon on 10/12/16.
 */
public class InstagramUserInfoProviderIT {

    @Test
    public void testInstagramUserInfoProvider() throws Exception {

        String configfile = "./target/test-classes/InstagramUserInfoProviderIT.conf";
        String outfile = "./target/test-classes/InstagramUserInfoProviderIT.stdout.txt";

        String[] args = new String[2];
        args[0] = configfile;
        args[1] = outfile;

        InstagramUserInfoProvider.main(args);

        File out = new File(outfile);
        assert (out.exists());
        assert (out.canRead());
        assert (out.isFile());

        FileReader outReader = new FileReader(out);
        LineNumberReader outCounter = new LineNumberReader(outReader);

        while(outCounter.readLine() != null) {}

        assert (outCounter.getLineNumber() >= 1);

    }
}
