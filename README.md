## Synopsis
An implementation of a watermark RESTful Web Service that can watermark documents.
Book publications include topics in business, science and media. Journals
don’t include any specific topics. A document (books, journals) has a title, author and a watermark
property. An empty watermark property indicates that the document has not been watermarked yet.  

The watermark service is asynchronous. For a given content document the service is
returning a ticket, which can be used to poll the status of processing. If the watermarking is finished the
document can be retrieved with the ticket. The watermark of a book or a journal is identified by
setting the watermark property of the object. For a book the watermark includes the properties
content, title, author and topic. The journal watermark includes the content, title and author.

## Requirements

You'll need Java 8 to compile and run the application. You'll also need maven to build it.

## How do I run it?

You can run the application using

mvn spring-boot:run

Or you can build the JAR file with

mvn clean package

and run the JAR by typing

java -jar target/watermark-service-1.0-SNAPSHOT.jar

If you are experiencing problems starting the embedded tomcat instance at the default port 8080 you might want to try and change the port by issuing
java -jar target/watermark-service-1.0-SNAPSHOT.jar --server.port=8181

## Implementation Details

For a quick overview of the object oriented model of the problem have a look under the folder /uml
Each document is saved in memory but not persisted. The implementation makes no assumptions in regards to 
the documents it can handle. Each and every document is treated the same no matter if the same document is 
sent multiple times, i.e. it will be saved as a different entry in memory and will be assigned a different ticket.
An enhancement can be made on that matter in the sense that the same ticket can be returned for documents 
that have already been received for watermark processing.

The ticket id is implemented as a simple atomic integer starting from 1 which increments on every watermark request.

The logs are produced under the LOGS/ folder in the location you run the application
and written to a file with pattern WATERMARK_{yyyy-MM-dd}.log
The topic can be one of the three type: Business, Science or Media. Failure to provide exactly one of them in the request 
will result to a ConversionFailedException.
An improvement can be made so that we can register exception handlers by means of controller advice
for some or any of the exceptions thrown by the controller.

## Allowed API operations

  1. You can post a specific document (book or journal) for which a watermark is needed. If the json contains the topic element the json object
  of the request is mapped to a book otherwise it is mapped to a journal.
  
  curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST http://localhost:8080/watermark/create -d "{ \"title\" : \"A Brief History of Time\", \"author\" : {\"firstName\" : \"Stephen\", \"lastName\" : \"Hawking\"}, \"topic\" : \"Science\" }"

  2. An equivalent GET operation is defined which has effectively the same impact.
  Send a document for watermarking and return a ticket which can be used by a consecutive 
  call to the service (see step 3 below) to retrieve the actual watermark.
  
  curl -i -G  http://localhost:8080/watermark/create\?title\=Progress\%20in\%20Earth\%20and\%20Planetary\%20Science\&authorFirstName\=Akio\&authorLastName\=Suzuki
  
  3. The -i option will give you the response output. Use the id of the ticket returned by the previous call and append it as a request parameter.
  If there is no watermark yet, either because the ticket does not exist or because the (simulated) process of watermark is not yet finished
  the response body will be empty.
  
  curl -i -G  http://localhost:8080/watermark/get\?ticket\=1

    
  
You can find some usage examples of the tool specifically for our application under examples/request-examples.txt

ENJOY!
