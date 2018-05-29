package com.developmentontheedge.be5.server.util;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpRequests
{
    private static final String USER_AGENT = "Mozilla/5.0";

    public static String sendGet(String url)
    {
        try
        {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK)
            {
                return null;
            }

            BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null)
            {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }
}
