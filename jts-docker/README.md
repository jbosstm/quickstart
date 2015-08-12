Narayana JTS in Docker example.
==================================================================================================
Author: Gytis Trikleris;
Level: Intermediate;
Technologies: Docker, JTS

What is it?
-----------

This example demonstrates how to use Narayana JTS deployed in Docker container.

Manual containers management
---------------------------

### Starting name server

    docker run -p 3528:3528 -it --name jacorb-name-server jboss/jacorb-name-server


### Starting transaction service

    docker run -p 4710:4710 -it --link jacorb-name-server:jacorb-name-server --name jts-transaction-service jboss/jts-transaction-service


Containers management with Kubernetes
-------------------------------------

    kubectl create -f kubernetes/jts-pod.yaml

Build and run the quickstart
-------------------------------

If you have started containers manually:

    mvn clean test -Pdocker -DNAME_SERVER_IP=<docker container's IP> -DNAME_SERVER_PORT=<name server's PORT, defaults to 3528> -DCLIENT_IP=<your IP accessible for the container>
    
If you have started containers with Kubernetes:

    mvn clean test -Pdocker -DNAME_SERVER_IP=$(kubectl get pod jts -o=template -t={{.status.podIP}}) -DCLIENT_IP=<your IP accessible for the container>

CLIENT_IP address, is your machines IP address accessible to the docker container. Depending on what connection you are using (ethernet, wlan) you have to pick the appropriate IP.


Mounting local repository as an object store
--------------------------------------------

NOTE: following steps work with manually managed containers.

It is possible to mount one of the local directories to be used as object store. This will allow you to store object store safe in case docker container is removed.

Extra parameter is needed in order to mount the directory. Run transaction service container:
    
    docker run -p 4710:4710 -it -v /tmp/tx-object-store:/home/tx-object-store --link jacorb-name-server:jacorb-name-server --name jts-transaction-service jboss/jts-transaction-service
    
Using Postgres as an object store
---------------------------------

NOTE: following steps work with manually managed containers.

Run Postgres container:

    docker run -e POSTGRES_USER=narayana -e POSTGRES_PASSWORD=narayana --name jdbc-object-store postgres
    
Run transaction service container:

    docker run -p 4710:4710 -it --link jacorb-name-server:jacorb-name-server --link jdbc-object-store:jdbc-object-store -v ${PATH_TO_THE_QUICKSTART}/src/test/resources/docker-narayana-lib:/home/lib -v ${PATH_TO_THE_QUICKSTART}/src/test/resources/docker-narayana-config:/home/narayana/etc --name jts-transaction-service jboss/jts-transaction-service