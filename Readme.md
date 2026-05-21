# Adapter to send Telraam sensor data to DAVe
[DAVe](https://starwit-technologies.de/products/dave-open-source-traffic-count-analysis-for-modern-mobility-management/) is a software to analyse and visualize traffic data. Data is collected by a large number of sensors. This repository shall provide code, to transfer data from sensor offered by [Telraam](https://telraam.net/). See their web page, if you want to know more about their hardware.

## Planned Features
* Transfer data every 15 minutes
* Auto Detect traffic direction
* Auto detect all sensors in a geo-fenced region.

## Background
This section provides all info, to understand how to transfer data to DAVe and how to extract them from Telraam.

### Data Model in DAVe
In [DAVe](https://opensource.muenchen.de/de/software/dave.html) directions are defined as shown in the following image. Top side is pointing north. Following image shows an example for a configured intersection counting.

![](doc/dave-directions.jpg)

All collected data needs to be brought into this format.

### Sensor Doc
[API Definition](https://app.swaggerhub.com/apis-docs/telraam/Telraam-API/1.2.0)

# License & Contribution
This project is licensed under the AGPLv3 License - see the [LICENSE](LICENSE) file for details.