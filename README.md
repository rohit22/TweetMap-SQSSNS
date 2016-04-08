# TweetMap

The goal of this project is to map tweets based on keywords. It is primarly motivated towards building a scalable application using amazon big data stack. 

The technologies used by us in this project are

- Amazon CloudSearch
- Twitter streaming API
- WebSockets
- Tomcat 

**Data Sourcing:** Amazon streaming API needs keywords to track. We gave nearly 30 keywords based on the latest trends and interesting topics. We had to give a large set of words for tracking owing to the constraints we placed on the information of a tweet.

- Constraint 1: We are mostly interested in tweets in english language.
- Constraint 2: As our end goal is to map them on the world map, we restricted ourselves to tweets that have the location information.

**Data Indexing:** We used Amazon CloudSearch to index and search the data. It is a scalable solution with http end points to upload a document as well as search the index. It also provides the functionality for geographical search, which we intend to leverage. We index data primarily on 5 fields, *id, text, lattitude, longitude, original tweet* (for any future processing). 

**Application:** Our application is built using web sockets to minimize the client-server interaction and also to make our application dynamic, which gives us a chance to load tweets on the map realtime. We then deployed the application using tomcat server. 

**Basic application flow:** As soon as the application is launched, we start displaying tweets real time on the map. Once the user enters a keyword, we will display only those tweets that have the keyword in them. Due to restrictions on streaming api, we are not able to dynamically modify the keywords to track. We are still working on bypassing this restriction so that we can give the user a real-time search capability over the entire streaming data rather than restric ourselves to a set of keywords.

**Code:** The files contain all the codes (including codes that are being used to experiment)

- **com.aws:** *UploadTweets.java:* This class is used to interact with Amazon CloudSearch. It contains functions to upload a file containing 50 tweets (CloudSearch supports batch upload of files). 

- **com.java.src:** *TwitterStreamConsumer.java:* This class gets the data from the twitter streaming API, forms json objects (one to upload to CloudSearch and other to send to the application front end), uses the function in CloudSearch to upload the file containing tweets.

- **com.java.src:** *TwitterStreamServer.java:* This class contains a main file and this can be used to run TwitterStreamConsumer standalone. This funcionality is primarily used to index twitter data offline. 

- **com.utils:** Contains the utilities files for parsing and creating json objects at different points in the workflow.

- **com.websocket:** *GetTweets2.java:* File that is the server side end point for the websocket. It contains the function mappings and necessary invocations.

- **WebContent:** *index.html:* The UI of our application. It interacts with GetTweets2.java to get relevant tweets for displaying. 

- *pom.xml:* Contains all the dependencies of the project.

This is a maven project and can directly imported as one using maven and eclipse. This is a dynamic web project in eclipse and the war file of it can be easily deployed to a tomcat instance.
