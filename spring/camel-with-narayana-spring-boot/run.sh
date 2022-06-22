#/bin/bash
MAX=10

function check_application_alive() {
    for (( i = 0; i < $MAX ; i ++ ))
    do
        STATUS=$(curl -s http://localhost:8080/health | grep UP)
        if [ ! -z "$STATUS" ]; then
            break;
        fi
        sleep `timeout_adjust 2 2>/dev/null || echo 2`
    done
}

echo -n "Application Starting ... "
java -jar target/camel-with-narayana-spring-boot-5.12.8.Final-SNAPSHOT.jar 1>./target/test.log 2>&1 &
check_application_alive
if [ ! -z "$STATUS" ]; then
    echo "Done"
    # Commit Test
    echo "Running Commit Test"
    curl -s -X POST http://localhost:8080/user -d "name=test"
    curl -s http://localhost:8080/users

    # Rollback Test
    echo "Running Rollback Test"
    curl -s -X POST http://localhost:8080/user -d "name=bad"
    curl -s http://localhost:8080/users

    # Recover Test
    echo "Running Recovery Test"
    curl -s -X POST http://localhost:8080/user -d "name=halt"
    sleep `timeout_adjust 2 2>/dev/null || echo 2`

    # Restart application
    echo -n "Application Restarting ... "
    java -Drecover=true -jar target/camel-with-narayana-spring-boot-5.12.8.Final-SNAPSHOT.jar 1>./target/test.log 2>&1 &
    check_application_alive

    if [ ! -z "$STATUS" ]; then
        echo "Done"
        curl -s http://localhost:8080/users
        echo "Application Shutdown"
        curl -X POST -s http://localhost:8080/shutdown 1>/dev/null 2>&1
    fi
fi

