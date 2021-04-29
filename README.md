<h1 align="center">Immuni European Federation Gateway Client</h1>

<div align="center">
<img width="256" height="256" src="img/logo.png">
</div>

<br />
<div align="center">
    <!-- CoC -->
    <a href="CODE_OF_CONDUCT.md">
      <img src="https://img.shields.io/badge/Contributor%20Covenant-v2.0%20adopted-ff69b4.svg" />
    </a>
    <a href="https://www.oracle.com/java/technologies/javase-jdk11-downloads.html">
      <img alt="java11"
      src="https://img.shields.io/badge/java-11-green">
    </a>
    <a href="https://github.com/PyCQA/bandit">
      <img alt="security: bandit"
      src="https://img.shields.io/badge/security-bandit-yellow.svg">
    </a>
</div>

<div align="center">
  <h3>
    </span>
    <a href="https://github.com/immuni-app/immuni-documentation">
      Documentation
    </a>
    <span> | </span>
    <a href="CONTRIBUTING.md">
      Contributing
    </a>
  </h3>
</div>


# Table of contents

- [Context](#context)
- [Installation](#installation)
- [Contributing](#contributing)
  - [Contributors](#contributors)
- [Licence](#licence)
  - [Authors / Copyright](#authors--copyright)
  - [Third-party component licences](#third-party-component-licences)
  - [Licence details](#licence-details)


# Context
This repository contains the source code of Immuni's European Federation Gateway Client developed for the interoperability between national backend servers of decentralised contact tracing applications to combat COVID-19.

This client acts as a bridge between the Immuni backend and the federation gateway developed by the EU.

Its main purpose is essentially to allow the upload of the Italian TEKs and download the foreign TEKs from the [federation gateway service](https://github.com/eu-federation-gateway-service/efgs-federation-gateway).

In this way both Italian citizens who go abroad and foreign citizens who come to Italy are able to exchange their Temporary Exposure Keys and get notified if tested positive for the SARS-CoV-2 virus.
This makes it possible to extend contact tracing to foreigners travelling in Italy and to Italians travelling abroad, without the need to download additional apps.

More detailed information about the European Federation Gateway Server (efgs) can be found in the following documents:

- [Software Design European Federation Gateway Service](https://github.com/eu-federation-gateway-service/efgs-federation-gateway/blob/master/docs/software-design-federation-gateway-service.md)

**Please take the time to read and consider the other repositories in full before digging into the source code or opening an Issue. They contain a lot of details that are fundamental to understanding the source code and this repository's documentation.**

# Installation

### Prerequisites
 - [Open JDK 11](https://openjdk.java.net) 
 - [Maven](https://maven.apache.org)

#### Maven based build
This is the recommended way for taking part in the development.
Please check, whether following prerequisites are installed on your machine:
- [Open JDK 11](https://openjdk.java.net) or a similar JDK 11 compatible VM
- [Maven](https://maven.apache.org)

#### Build Docker Image
This project also supports building a Docker image.

To build the Docker image you need to build the project from the root:

```shell script
git clone git@github.com:immuni-app/immuni-efgs-gateway-client.git
cd immuni-efgs-gateway-client
mvn clean package
```
For local testing first ensure you have a MongoDB and Redis instances running locally, then fill the envar in the enviroment section of the ``docker-compose.yml``:

```
environment:
    MONGO_DB_URI=mongodb://user:password@mongodb:27017/EGFSDB-dev
    REDIS_URL=redis://:password@redis:15166
```
Also you need the EFGS server running locally, you can install it from the public repo [European Federation Gateway Service](https://github.com/eu-federation-gateway-service/efgs-federation-gateway).

To properly work the client needs also:

- an external signature service (rest API) ``SIGN_EXTERNAL_URL=https://host/v1/sign``.

- the certificate for the connection in mTLS to the European Federation Gateway Service (the country of origin must be defined in the "_country_" field of the certificate subject) and pack it into a Java Key Store.
```
environment:
      - EFGS_BASE_URL=https://example.dgc.eu
      - SSLEFGS_JKS_PATH=/security/sslclient/ssldgc.jks
      - SSLEFGS_JKS_PASSWORD=password
      - SSLEFGS_CERT_PASSWORD=password
```
- the certificate for the connection in mTLS with the external signing service and pack it into a Java Key Store.
```
environment:
      - SIGN_EXTERNAL_URL=https://host/v1/sign
      - SSLDP_JKS_PATH=/security/sslclient/ssldp.jks
      - SSLDP_JKS_PASSWORD=password
      - SSLDP_CERT_PASSWORD=password
```
- TrustAnchor to verify the signature of member state certificates and pack it into a Java Key Store.
``` 
environment:
      - TRUST_JKS_PATH=/security/truststore/truststore.jks
      - TRUST_JKS_PASSWORD=password
      - TRUST_EFGS_ANCHOR_ALIAS=anchor_alias
```

Once the requirements above shown are satisfied open a shell with working directory and execute

```shell script
docker-compose up --build
```

# Contributing
Contributions are most welcome. Before proceeding, please read the [Code of Conduct](./CODE_OF_CONDUCT.md) for guidance on how to approach the community and create a positive environment. Additionally, please read our [CONTRIBUTING](./CONTRIBUTING.md) file, which contains guidance on ensuring a smooth contribution process.

To propose a feature request, please open an issue in the [Documentation repository](https://github.com/immuni-app/immuni-documentation). This lets everyone involved see it, consider it, and participate in the discussion. Opening an issue or pull request in this repository may slow down the overall process.

## Contributors
Here is a list of Immuni's contributors. Thank you to everyone involved for improving Immuni, day by day.

<a href="https://github.com/immuni-app/immuni-app/immuni-efgs-gateway-client">
  <img
  src="https://contributors-img.web.app/image?repo=immuni-app/immuni-efgs-gateway-client"
  />
</a>

# Licence

## Authors / Copyright

Copyright 2020 (c) Presidenza del Consiglio dei Ministri.

Please check the [AUTHORS](AUTHORS) file for extended reference.

## Third-party component licences

Please see the Technology Description’s [Backend Services Technologies](https://github.com/immuni-app/documentation/blob/master/Technology%20Description.md#backend-services-technologies) section, which also lists the corresponding licences.

## Licence details

The licence for this repository is a [GNU Affero General Public Licence version 3](https://www.gnu.org/licenses/agpl-3.0.html) (SPDX: AGPL-3.0). Please see the [LICENSE](LICENSE) file for full reference.










