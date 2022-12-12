#Securing lra-coordinator endpoints using JWT

> This quickstart will demonstrate how to secure LRA-coordinator endpoints using JSON Web Tokens(JWTs) when the lra-coordinator is deployed on various application servers

Currently, here we only demonstrate **WFLY** as the application server, configured with JWT authorization.

## Narayana JWT-LRA Role mapping recommendation

The lra-endpoints listed in below table can be found in [Coordinator.java](https://github.com/jbosstm/narayana/blob/main/rts/lra/coordinator/src/main/java/io/narayana/lra/coordinator/api/Coordinator.java) and [RecoveryCoordinator.java](https://github.com/jbosstm/narayana/blob/main/rts/lra/coordinator/src/main/java/io/narayana/lra/coordinator/api/RecoveryCoordinator.java)

| lra-endpoints        | Allowed Roles   | 
| ------------- |:-------------:|
| getAllLRAs      | client | 
| getLRAStatus     | client      | 
| getLRAInfo |    client   | 
| startLRA      | client | 
| renewTimeLimit     | client      | 
| getNestedLRAStatus |    client   | 
| closeLRA      | client | 
| cancelLRA     | client      | 
| joinLRAViaBody |    client   | 
| leaveLRA |    client   | 
| ------------------------------------|-----------  | 
| completeNestedLRA (NESTED)      | system | 
| compensateNestedLRA (NESTED)     | system      | 
| forgetNestedLRA (NESTED) |    system   | 
| ------------------------------------|-----------  | 
| getCompensator      | admin | 
| replaceCompensator     | admin      | 
| getRecoveringLRAs |    admin   | 
| getFailedLRAs      | admin | 
| deleteFailedLRA     | admin      | 

## Brief info about recommended configuration

In the above mentioned table we can find the recommended **lra-endpoints role mapping**.
We have defined three roles i.e. **client**, **system** and **admin** for securing the lra-endpoints.
Dividing the responsibility for accessing the endpoints among mentioned three roles would make the endpoints more secure.

We can configure the endpoints to role mapping using **web.xml** configuration file that would be present in **lra-coordinator** war.
1. **<login-config>** Tag - `*BEARER_TOKEN* defines the JWT security mechanism`
     
              <login-config>
                  <auth-method>BEARER_TOKEN</auth-method>
                  <realm-name>jwt-realm</realm-name>
              </login-config>
              
2. **<security-role>** Tag - `Defines the all possible security roles.`

               <security-role>
                       <role-name>client</role-name>
               </security-role>
               <security-role>
                       <role-name>system</role-name>
              </security-role>
              <security-role>
                       <role-name>admin</role-name>
               </security-role>
3. **<security-constraint>** Tag - `Defines role to endpoint mapping`

          <security-constraint>
              <web-resource-collection>
                  <web-resource-name>Define the endpoint that is to be secured</web-resource-name>
                  <url-pattern>/lra-coordinator/endpoint</url-pattern>
              </web-resource-collection>
              <auth-constraint>
                  <role-name>admin</role-name>
              </auth-constraint>
          </security-constraint>
      
      Here in the third step we can define all the possible recommended role to endpoint mapping as shown in the above table.
      
      Defining all the above demonstrated tags in **web.xml** file will enable the **JWT** security mechanism in the lra-coordinator war which would be deployed in the WildFly application server.