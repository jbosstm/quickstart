Narayana JTS in Docker example.
==================================================================================================
Author: Gytis Trikleris;
Level: Intermediate;
Technologies: Docker, JTS

What is it?
-----------

This example demonstrates how to use Narayana JTS deployed in Docker container.

Starting name server
--------------------

Build image

    git clone https://github.com/jboss-dockerfiles/narayana.git
    cd jacorb-name-server
    docker build -t name-server .

Run container

    docker run -p 3528:3528 -it --name name-server name-server


Starting transaction service
----------------------------

Build image

    git clone https://github.com/jboss-dockerfiles/narayana.git
    cd jts-transaction-service
    docker build -t transaction-service .

Run container

    docker run -p 4710:4710 -it --link name-server:name-server --name transaction-service transaction-service


Build and run the quickstart
-------------------------------

First of all you have to make sure that JacORB name service and Narayana JTS images are running. Clone them from the following repository and follow the instructions there: https://github.com/jboss-dockerfiles/narayana.

To run test, execute folowing command:

    mvn clean test -Pdocker -DNAME_SERVER_IP=<docker container's IP> -DNAME_SERVER_PORT=<name server's PORT, defaults to 3528> -DCLIENT_IP=<your IP accessible for the container>

CLIENT_IP address, is your machines IP address accessible to the docker container. Depending on what connection you are using (ethernet, wlan) you have to pick the appropriate IP.


Mounting local repository as an object store
--------------------------------------------

It is possible to mount one of the local directories to be used as object store. This will allow you to store object store safe in case docker container is removed.

Extra parameter is needed in order to mount the directory. Run transaction service container:
    
    docker run -p 4710:4710 -it -v /tmp/tx-object-store:/home/tx-object-store --link name-server:name-server --name transaction-service transaction-service
    
Using Postgres as an object store
---------------------------------

Run Postgres container:

    docker run -e POSTGRES_USER=narayana -e POSTGRES_PASSWORD=narayana --name jdbc-object-store postgres
    
Run transaction service container:

    docker run -p 4710:4710 -it --link name-server:name-server --link jdbc-object-store:jdbc-object-store -v ${PATH_TO_THE_QUICKSTART}/src/test/resources/docker-narayana-lib:/home/lib -v ${PATH_TO_THE_QUICKSTART}/src/test/resources/docker-narayana-config:/home/narayana/etc --name transaction-service transaction-service