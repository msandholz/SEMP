# Simple Energy Management Protocol
Implementation of the Simple Energy Management Protocol (SEMP).


## Smart Appliance Enabler
This is a project of Axel Müller that provides similar functionality than my code.  
It can be found at https://github.com/camueller/SmartApplianceEnabler

To install the docker version follow this instructions:
1. YAML-Datei
Für den Smart Appliance Enabler existiert eine vorkonfigurierte YAML-Datei, für die ein Verzeichnis angelegt werden muss, um sie danach herunterzuladen:

```
pi@raspberrypi:~ $ sudo mkdir -p /home/pi/IoT-Stack/smartapplianceenabler/compose
pi@raspberrypi:~ $ sudo wget https://github.com/camueller/SmartApplianceEnabler/raw/master/run/etc/docker/compose/docker-compose.yaml -P /home/pi/IoT-Stack/smartapplianceenabler/compose
```

Hinweise zu den notwendigen Anpassungen finden sich als Kommentare in der Datei selbst. Das Docker-Volume sae wird automatisch beim Start erstellt, falls noch nicht vorhanden.

2. Edit Docker-Compose File:

```
pi@raspberrypi:~ $ sudo nano /home/pi/IoT-Stack/smartapplianceenabler/compose/docker-compose.yaml
```


3. Install Docker-Compose:

```
pi@raspberrypi:~ $ cd /home/pi/IoT-Stack/smartapplianceenabler/compose/
pi@raspberrypi:~IoT-Stack/smartapplianceenabler/compose/ docker-compose up -d

```

4. Remove Docker-Compose

```shell
pi@RasPi-Server:~/IoT-Stack/smartapplianceenabler/compose $ docker-compose down
```

