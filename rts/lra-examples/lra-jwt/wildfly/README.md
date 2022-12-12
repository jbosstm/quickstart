# Securing LRA coordinator endpoints over Wildfly server using JWT

 This quickstart will demonstrate how to secure LRA coordinator endpoints using JSON Web Tokens(JWTs) when the lra-coordinator is deployed on WildFly server

 The `lra-coordinator WAR` expects to be the same as one at https://github.com/jbosstm/narayana/blob/main/rts/lra/coordinator-war/pom.xml. But the purpose is to compile into this one with a different web.xml, which would be used to define the Authentication/Authorization mechanism.


## Follow along below steps:
1. Setup web.xml before deploying the **lra-coordinator** war file. Here we define the security mechanism for application endpoints i.e. JWT and also define the **Roles** and **endpoints** mapping for authorization.
    > In the application we have defined two Roles i.e. `admin` and `participant` just for the demonstration purpose. Please check the recommended `role` to `endpoint` mapping ( here [lra-jwt/README.md](../README.md) ) for making best use of this feature.

2. Prepare **WildFly Server** for JWT autorization:
    1. Create keystore using below command under path `$JBOSS_HOME/standalone/configuration`
        >cd $JBOSS_HOME/standalone/configuration

        >keytool -genkey -alias alias -keyalg RSA -keysize 2048 -keystore jwt.keystore -storepass secret -keypass secret
           
          >
            
            What is your first and last name?
               [Unknown]:  localhost
            What is the name of your organizational unit?
               [Unknown]:  wildfly
            What is the name of your organization?
               [Unknown]:  jboss
            What is the name of your City or Locality?
               [Unknown]:  Bangalore
            What is the name of your State or Province?
               [Unknown]:  Karnataka
            What is the two-letter country code for this unit?
               [Unknown]:  IN
            Is CN=localhost, OU=wildfly, O=jboss, L=Bangalore, ST=Karnataka, C=IN correct?
               [no]:  yes
            
    2. Include subsystems required for JWT
        >Start the wildFly server and run the below command for quick setup (You can also check [configure-elytron.cli](configure-elytron.cli) to know more about the commands that are run for setting up the subsystems).
        `$JBOSS_HOME/bin/jboss-cli.sh --connect --file=configure-elytron.cli`
    
        **Note:** We can revert back subsystem JWT setting by running `$JBOSS_HOME/bin/jboss-cli.sh --connect --file=restore-configuration.cli`
    
    3. Deploy Narayana LRA coordinator war file under path `$JBOSS_HOME/standalone/deployments`
    
3.  Generating **JWT** token:
    1. Copy the keystore from `$JBOSS_HOME/standalone/configuration` to client [resources](client/src/main/resources) folder.
    2. Run `createJwt` method present in `JwtManager.java` class
    
                Here is how you can get access token:
                1. Run "mvn exec:java"
                2. Check the console, you would see the output as "$ROLE jwt : Bearer $access_token $path_called"
                3. Now save this token value to a variable "$access_token" and run the above command.
              
 
    
 4 .  Call endpoints using `CURL` command and can test around
   	
  > Example: `curl -s -X POST -H "Authorization: Bearer $access_token" http://localhost:8080/lra-coordinator/lra-coordinator/start`
                                                           
   Here `$access_token` is that, we generated in step 3.
    

  ##Access the Application
  
  Before you run the client, make sure you have already successfully deployed the LRA-coordinator to the server in the previous step.
  
  Type the following command to execute the client in client directory.
  
  > $ mvn exec:java

You can play around with the client code and check the behaviour of defined ROLEs in web.xml file

## Creating JWT token using Keycloak

This Quickstart shows the manual generation of JWT token for the quick showcase purpose but normally Keycloak is recommended for this purpose.
The Keycloak is recommended for this because now the generation of the secured JWT token is not the responsibility of user, now it will be generated automatically with the latest security specifications by Keycloak.

One of the quickstart to use Keycloak could be found here: https://blogs.nologin.es/rickyepoderi/index.php?/archives/164-Authenticating-a-JWT-token-in-wildfly.html

For more info about Keycloak authorization: https://www.keycloak.org/docs/4.8/authorization_services/