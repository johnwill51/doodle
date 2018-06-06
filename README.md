# doodle: chat application

## Prerequisites

Backend:
- Java 8 compiler
- Maven

In case you wonder I coded the backend with Intellij.
Even if the project has a dependency on MongoDB it is not used as I had not time to add the DB layer.

Frontend:
- node
- npm

I tested the project on 
- Debian 8 
    - not up-to-date Chromium (did not check the version)
    - not up-to-date Firefox (did not check the version)
- Windows 7
    - Chrome 66
    - Firefox 56
    - IE Edge

## Instructions

Build the backend and run the tests
```
cd backend && mvn clean install
```

Build the frontend
```
cd ui && npm run build
```
This will build the frontend assets and copy them in
`backend/src/main/resources/public` where they will be served when running the server

Test the frontend
```
cd ui && npm run test // test:dev in watch mode
```

## Run the app

```
mvn exec:java -Dexec.mainClass="com.doodle.chat.Application"
```
This will start the server and you can navigate to `http:://localhost:9001/` to see the app running. You can try experimenting with different routes if you want.
If you want to run the frontend in dev mode, start the server then `cd ui && npm run start:dev`. The app will be available at `http:://localhost:9000/` and all requests will be proxyied by webpack dev server to port 9001.

## Considerations

The backend tests are more integration tests, whereas frontent tests are more unit tests.
I preferred concentrating on those tests and due to lack of time I could not implement the DB layer.
Also, the scroll functionnality in the UI is not implemented resulting in a view not following the incoming messages: they are there, you just need to scroll manually...
Another basic thing I did not have time to implement is user experience in regards to input validation and protection against injected scripts. 



