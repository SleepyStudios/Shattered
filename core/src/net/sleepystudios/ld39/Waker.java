package net.sleepystudios.ld39;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by tudor on 31/07/2017.
 */
public class Waker {
    private String id;

    public Waker(String id) {
        this.id = id;
    }

    public String getServer() {
        try {
            String wakerLocation = getHttpResponse("http://sleepystudios.net/waker.txt");
            return getHttpResponse(wakerLocation + "/server?id=" + id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHttpResponse(String url) throws IOException {
        URL urlObj = new URL(url);
        URLConnection lu = urlObj.openConnection();

        // get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(lu.getInputStream()));
        return rd.readLine();
    }
}
