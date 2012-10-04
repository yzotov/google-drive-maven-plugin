package com.fredericvauchelles;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 *
 * @goal connect
 */
public class ConnectMojo extends AbstractMojo
{
    private static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    private static GoogleAuthorizationCodeFlow getFlow(String clientId, String clientSecret) {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
       
        return new GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, clientId, clientSecret, Arrays.asList(DriveScopes.DRIVE))
            .setAccessType("online")
            .setApprovalPrompt("auto").build();
    }

    public static GoogleCredential getCredential(java.io.File googleAccessProperties, java.io.File googleClientProperties) throws IOException, Exception{
        Properties accessProperties = new Properties();
        accessProperties.load(new FileInputStream(googleAccessProperties));
        String accessToken = accessProperties.getProperty("accessToken");
        String authToken = accessProperties.getProperty("authToken");

        Properties clientProperties = new Properties();
        clientProperties.load(new FileInputStream(googleClientProperties));
        String clientId = clientProperties.getProperty("clientId");
        String clientSecret = clientProperties.getProperty("clientSecret");

        if(clientId == null)
            throw new Exception("clientId is not defined in " + googleClientProperties.getAbsolutePath());

        if(clientSecret == null)
            throw new Exception("clientSecret is not defined in " + googleClientProperties.getAbsolutePath());

        GoogleAuthorizationCodeFlow flow = getFlow(clientProperties.getProperty("clientId"), clientProperties.getProperty("clientSecret"));
        
        GoogleCredential credential = null;
        
        if(accessToken != null && !"".equals(accessToken)) {
            credential = new GoogleCredential();
            credential.setAccessToken(accessToken);
        }
        else if(authToken != null && !"".equals(authToken)) {
            GoogleTokenResponse response = flow.newTokenRequest(authToken).setRedirectUri(REDIRECT_URI).execute();
            credential = new GoogleCredential().setFromTokenResponse(response);    
        }
        else {
            String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
            throw new Exception("Access Token is not defined : checkout out " + url);
        }
        
        // Write connection properties
        Properties props = new Properties();
        props.setProperty("accessToken", credential.getAccessToken());
        props.setProperty("authToken", authToken);
        props.store(new FileOutputStream("src/main/resources/googleDrive.tmp.properties"), null);

        return credential;
    }

    public static Drive getDriveService(java.io.File googleAccessProperties, java.io.File googleClientProperties)
        throws IOException, Exception {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        return new Drive.Builder(httpTransport, jsonFactory, getCredential(googleAccessProperties, googleClientProperties)).build();
    }

    /**
     * @parameter
     * @required
     */
    private java.io.File googleClientProperties;

    /**
     * @parameter
     * @required
     */
    private java.io.File googleAccessProperties;

    public void execute()
        throws MojoExecutionException
    {
        getLog().info("Google Connect : OAuth2 authentification");

        if(!googleClientProperties.exists())
            throw new MojoExecutionException(googleClientProperties.getAbsolutePath() + " does not exists");

        try {
            getCredential(googleAccessProperties, googleClientProperties);    
        }
        catch(Exception e){
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
