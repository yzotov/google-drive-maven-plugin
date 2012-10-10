Google Drive Maven Plugin
======================

Introduction
----------------------

You can use this plugin to connect and upload files to Google Drive


Setup
----------------------

# Setup Google Application

You must create a an installed application access in your Google Console API (http://www.google.fr/url?sa=t&rct=j&q=google+console+api&source=web&cd=1&cad=rja&ved=0CCMQFjAA&url=http%3A%2F%2Fcode.google.com%2Fapis%2Fconsole&ei=nZp1UOi-LaTX0QXvxIG4DA&usg=AFQjCNFikY2jzXn9SOuZu0UcyS-59LlsTw)

Once you have you clientId and clientSecret, you can create a property file with :

clientId=<YOUR CLIENT ID>
clientSecret=<YOUR CLIENT SECRET>

And use this property file as googleClientProperties argument for the maven plugin

# Setup OAuth2 Token

Once your application is setup, you must create an OAuth Token to use Google Drive API.

Use : mvn google-drive-maven-plugin:connect to connect to the API.

The first time, the application will fail and the log will give you an URL where you must connect with a web browser.
This page will ask you to authorize the application to access you Google Drive account. Accept, and then copy the token generated.

Add an entry in the googleClientProperties file :
authToken=<YOUR GENERATED AUTHORIZATION TOKEN>

Relaunch the command :
mvn google-drive-maven-plugin:connect

This time, it must succeed. At this point, maven will have created a file to store an access token and a refresh token to use Google Drive API.

Upload file
----------------------

Use the upload-file goal to upload file to Google Drive.

Goals
----------------------

* connect
Use it to generate OAuth2 tokens for connections

* upload-file
Upload a file to Google Drive

F.A.Q.
----------------------

Q: I have an exception when I use the upload-file and I already have connected to the Google Drive API
A: You may have to perform the connect operation again : the tokens given by Google have an expiration date, and you must get new token in this case.