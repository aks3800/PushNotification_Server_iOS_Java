/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aks.pushprovider;

import com.notnoop.apns.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import static org.apache.commons.httpclient.params.HttpMethodParams.USER_AGENT;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Akshat
 */
public class PushNoti {

    int id = 4;
    String title = "Push Notification";
    String body = "Notification Body. Everything is fully fuctional.";
    String category = "recipe";
    String expiryDate = "2018-03-10 06:34:49";
    ApnsService service;

    public PushNoti() {
        /*
        Location of your APNS Certificate.
        */
        service = APNS.newService().withCert("locationOfYourCetificate/PushCertificates.p12", "certificate_password").withSandboxDestination().build();
    }

    private void sendGet() throws Exception {

        /*
        
        Server URL to fetch the list of your APNS Device Tokens.
        
         */
        String url = "http://yourDomain/APNS/api/device_details_list";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("GET");
        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        parseJSON(response.toString());
    }

    public void notificatioSentResponse(String payload, int id, String token, String expiryDate) throws MalformedURLException, UnsupportedEncodingException, IOException {
        payload = payload.replaceAll("\"", "'");
        /*
        Sample Payload String Creation.
        */
        
        String postDataa = "{\"device_token\" : \"" + token + "\",\"notification_id\" : " + id + ",\"payload\" : \"" + payload + "\",\"expiry_date\" : \"" + expiryDate + "\"}";

        URL url = new URL("http://yourDomain/APNS/api/notification_details_list");
        byte[] postDataBytes = postDataa.getBytes("UTF-8");
        System.out.println(postDataa);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        StringBuilder sb = new StringBuilder();
        for (int c; (c = in.read()) >= 0;) {
            sb.append((char) c);
        }
        String response = sb.toString();
        System.out.println("response : " + response);
    }

    public void sendNotification1(String token) throws UnsupportedEncodingException, IOException {
        //String customPayload = "{\"aps\":{\"alert\":{\"body\" : \"Push Noti Body\", \"title\" : \"Push Noti Title\"},\"badge\":1,\"sound\":\"default\",\"mutable-content\":1, \"content-available\":1,\"category\": \"recipe\"},\"media-url\":\"https://i.imgur.com/t4WGJQx.jpg\"}";
        String customPayload = "{\"aps\":{\"alert\":{\"body\" : \"" + body + "\", \"title\" : \"" + title + id + "\"},\"badge\":1,\"sound\":\"default\", \"category\": \"" + category + "\"},\"id\": \"" + id + "\"}";
        notificatioSentResponse(customPayload, id, token, expiryDate);
        id++;
        //String token = "2FCD9E86E2C231070B9A9E027DFF3BE68F8B60857EC3C79F8DD58F8F7D7A24E4";
        this.service.push(token, customPayload);

    }

    public void parseJSON(String jsonString) throws ParseException, IOException {

        Object obj = new JSONParser().parse(jsonString);
        JSONArray array = (JSONArray) obj;

        for (int i = 0; i < array.size(); i++) {
            JSONObject jo = (JSONObject) array.get(i);
            String token = (String) jo.get("device_token");
            String name = (String) jo.get("device_name");
            System.out.println("Sending notification to " + name);
            sendNotification1(token);
        }

    }

    public static void main(String[] args) throws Exception {
        PushNoti obj = new PushNoti();

        obj.sendGet();
    }

}
