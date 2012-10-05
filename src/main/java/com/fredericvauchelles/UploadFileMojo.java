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
import java.util.Arrays;
import java.util.Properties;

/**
 * Goal which touches a timestamp file.
 *
 * @goal upload-file
 */
public class UploadFileMojo
    extends AbstractMojo
{
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

    /**
     * Location of the file.
     * @parameter 
     * @required
     */
    private java.io.File source;

    /**
     * @paramter
     * @required
     */
    private String mimeType;

    public void execute()
        throws MojoExecutionException
    {
        getLog().debug("Start Upload File Mojo");
        getLog().info("Source file : " + source.getAbsolutePath());

        try{
            Drive service = ConnectMojo.getDriveService(googleAccessProperties, googleClientProperties, getLog());

            //Insert a file  
            File body = new File();
            body.setTitle(source.getName());
            //body.setDescription("A test document");
            body.setMimeType(mimeType);
            
            FileContent mediaContent = new FileContent(mimeType, source);

            File file = service.files().insert(body, mediaContent).execute();
            getLog().info("File ID: " + file.getId());
        }
        catch(Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
