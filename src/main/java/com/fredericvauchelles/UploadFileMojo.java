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
import java.util.Arrays;

/**
 * Goal which touches a timestamp file.
 *
 * @goal upload-file
 */
public class UploadFileMojo
    extends AbstractMojo
{
    private static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    /**
     *
     * @parameter expression="${googleDrive.clientId}"
     * @required
     */
    private String clientId;
    /**
     *
     * @parameter expression="${googleDrive.clientSecret}"
     * @required
     */
    private String clientSecret;

    /**
     * @parameter expression="${googleDrive.authToken}"
     */
    private String authToken;

    /**
     * @parameter expression="${googleDrive.accessToken}"
     */
    private String accessToken;

    /**
     * Location of the file.
     * @parameter expression="${googleDrive.source}"
     * @required
     */
    private java.io.File source;

    /**
     * @paramter expression="${googleDrive.mimeType}"
     * @required
     */
    private String mimeType;

    public void execute()
        throws MojoExecutionException
    {
        getLog().debug("Start Upload File Mojo");
        getLog().info("Source file : " + source.getAbsolutePath());

        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
       
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, clientId, clientSecret, Arrays.asList(DriveScopes.DRIVE))
            .setAccessType("online")
            .setApprovalPrompt("auto").build();
        
        GoogleCredential credential = null;

        
        if(accessToken != null && !"".equals(accessToken)) {
            credential = new GoogleCredential();
            credential.setAccessToken(accessToken);
        }
        else if(authToken != null && !"".equals(authToken)) {
            try {
                GoogleTokenResponse response = flow.newTokenRequest(authToken).setRedirectUri(REDIRECT_URI).execute();
                credential = new GoogleCredential().setFromTokenResponse(response);    
            }
            catch(Exception e) {
                throw new MojoExecutionException(e.getMessage());
            }
        }
        else {
            String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
            getLog().info("Please open the following URL in your browser then use the authorization code:");
            getLog().info("  " + url);
            throw new MojoExecutionException("Access Token is not defined : checkout out " + url);
        }
        
        try{
            //Create a new authorized API client
            getLog().info("Access Token : " + credential.getAccessToken());
            Drive service = new Drive.Builder(httpTransport, jsonFactory, credential).build();

            //Insert a file  
            File body = new File();
            body.setTitle(source.getName());
            //body.setDescription("A test document");
            body.setMimeType(mimeType);
            
            FileContent mediaContent = new FileContent(mimeType, source);

            File file = service.files().insert(body, mediaContent).execute();
            getLog().info("File ID: " + file.getId());
        }
        catch(IOException e) {
            getLog().error(e.getMessage());
            throw new MojoExecutionException(e.getMessage());
        }
    }
}
