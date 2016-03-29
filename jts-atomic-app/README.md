Atomic App with JTS and Jacorb containers
===

Author: Gytis Trikleris
Level: Intermediate
Technologies: Vagrant, Docker, JTS


What is it?
---

This example demonstrates how to use Atomic App for running "JTS Transaction Service" and "Jacorb Name Server" containers on Atomic Host running on Vagrant.


Setup Atomic host
---

1. vagrant up
2. vagrant ssh
3. sudo atomic host upgrade
4. sudo systemctl reboot
5. vagrant reload


Run JTS Atomic App
---

1. vagrant ssh
2. sudo atomic run gytis/jts-atomic-app --destination jts-atomic-app
3. Run https://github.com/jbosstm/quickstart/tree/master/jts-docker with name server IP set to 192.168.33.10
4. sudo atomic stop gytis/jts-atomic-app jts-atomic-app
5. sudo docker rm jts-transaction-service
6. sudo docker rm jacorb-name-server


Notes
---

1. This Vagrant machine has a hardcoded IP address 192.168.33.10 which is used to access application. If you need to change it, do it in both Vagrantfile and Nulecule files.
2. We use --name flag for docker run command to name our containers in order to be able to link them together. Therefore, after stopping the app you'll need to manually remove the containers (see steps 5 and 6 of "Run JTS Atomic App"). Otherwise, when running app again, atomic app will try to create new containers and will fail because of name collision.
