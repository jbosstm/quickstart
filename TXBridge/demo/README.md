Build and deploy the demo:

	mvn install
	cp service/target/txbridge-demo-service.jar $JBOSS_HOME/standalone/deployments/
	cp client/target/txbridge-demo-client.jar $JBOSS_HOME/standalone/deployments/


Start JBoss AS:
---------------

ensure JBoss AS is started with XTS enabled. This can be done by specifying the standalone-xts configuration when starting JBoss AS:

	cd $JBOSS_HOME
	./bin/standalone.sh --server-config=../../docs/examples/configs/standalone-xts.xml

Visit http://localhost:8080/txbridge-demo-client/ to run the demo.

See docs/TransactionBridgingGuide for further details on the demo app.

