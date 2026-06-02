# Adapter to send Telraam sensor data to DAVe
[DAVe](https://starwit-technologies.de/products/dave-open-source-traffic-count-analysis-for-modern-mobility-management/) is a software to analyse and visualize traffic data. Data is collected by a large number of sensors. This repository shall provide code, to transfer data from sensor offered by [Telraam](https://telraam.net/). See their web page, if you want to know more about their hardware.

## Mission Goal
This software shall read data from Telraam's sensor network and convert counting data such, that DAVe can create node stream diagrams. Diagrams look like so:
![](doc/node-stream-diagram.jpg)

### Target Features
* Auto detect all sensors in a geo-fenced region
* Check if for every sensor a counting spot in DAVe exists
* Auto Detect traffic direction (north, south,...)
* Transfer count data every 15 minutes

## Traffic Data Background
This section provides all info, to understand how to transfer data to DAVe and how to extract them from Telraam.

### Data Model in DAVe
In [DAVe](https://opensource.muenchen.de/de/software/dave.html) directions are defined as shown in the following image. Top side is pointing north. Following image shows an example for a configured intersection counting.

![](doc/dave-directions.jpg)

All collected data needs to be brought into this format.

### Telraam Sensor Doc
Data for Telraam sensors can be accessed via a centralized API. See [API Definition](https://app.swaggerhub.com/apis-docs/telraam/Telraam-API/1.2.0)for more details. There are also some [examples](doc/telraam.md) how to query this API.

## Technical Architecture
Application is implemented using Spring Boot. Core is a scheduled task, that reads data from Telraam REST API and send converted date to DAVe. 

### Configuration
Both endpoints need configuration data including authentication. DAVe uses openID connect to authenticate API access whereas Telraam needs an API key.
For all config items see 

# License & Contribution
This project is licensed under the AGPLv3 License - see the [LICENSE](LICENSE) file for details.