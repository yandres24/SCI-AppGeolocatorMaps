package com.sci.www.sci_appgeolocator.Classes;


import com.sci.www.sci_appgeolocator.UrlRepository;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Administrador on 06/07/2015.
 */
public class Login {

    public boolean IsAutetication(String user, String password){

        BufferedReader bf=null;
        String content=null;
        String respon;
        HttpClient httpClient=new DefaultHttpClient();
        HttpGet httpGet=new HttpGet(UrlRepository.URL_IsvalidLogin +"?user="+user+"&password="+password);
        try
        {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            //HttpEntity entity = httpResponse.getEntity();
            //content= EntityUtils.toString(entity);
            InputStream contnt = httpResponse.getEntity().getContent();

            BufferedReader buffer = new BufferedReader(new InputStreamReader(contnt));
            String s ="";
            String response = "";
// populate the response string which will be passed later into the post execution
            while ((s = buffer.readLine()) != null) {
                response += s;
            }

            JSONObject jsonResult = new JSONObject(response);
            String description = jsonResult.getString("Responds");

            if (description.equals("False")){
                throw new Exception(content);
            }

        }

        catch(Exception ex)
        {
           return false;
        }
        return true;
    }

    public class respondLogin {
        public String Message;
    }
}
