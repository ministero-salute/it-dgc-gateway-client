<h1 align="center">EU COVID-19 Certificate Gateway Client</h1>

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


# Table of contents

- [Context](#context)
- [Installation](#installation)
- [Contributing](#contributing)
  - [Contributors](#contributors)
- [Licence](#licence)
  - [Authors / Copyright](#authors-and-copyright)
  - [Third-party component licences](#third-party-component-licences)
  - [Licence details](#licence-details)


# Context
This repository contains the source code of the EU COVID-19 Certificate Gateway Client.

# Installation

### Prerequisites
 - [Open JDK 11](https://openjdk.java.net) 
 - [Maven](https://maven.apache.org)
 - [MongoDB](https://www.mongodb.com/)

#### Maven based build
This is the recommended way for taking part in the development.
Please check, whether following prerequisites are installed on your machine:
- [Open JDK 11](https://openjdk.java.net) or a similar JDK 11 compatible VM
- [Maven](https://maven.apache.org)
- [MongoDB](https://www.mongodb.com/) a MongoDB instance running locally

#### Build Docker Image
This project also supports building a Docker image.
First ensure you have a MongoDB instance running locally on `` mongodb://127.0.0.1:27017``, otherwise change the connection url in the test file: ```./src/test/resources/application.properties```.

To build the Docker image you first need to build the project from the root:

```shell script
git clone git@github.com:ministero-salute/it-eucert-gateway-client.git
cd it-eucert-gateway-client
mvn clean package
```
Then, copy the file ``application.properties`` contained in the path ``./src/main/resources`` into the ``./config`` folder:
```shell script
mkdir config
cp ./src/main/resources/application.properties ./config
```

By default the docker image uses a local mongodb instance running on  `` mongodb://127.0.0.1:27017``, you can always change the connection url by editing the envar in the enviroment section of the ``docker-compose.yml``:

```
environment:
    MONGO_DB_URI=mongodb://user:password@mongodb:27017/EGFSDB-dev
```

Also you need the DGCG (Digital Green Certificate Gateway) server running locally, you can install it from the public repo [Digital Green Certificate Gateway](https://github.com/eu-digital-green-certificates/dgc-gateway).

To properly work the client needs also:

- an external signature service (rest API) ``SIGN_EXTERNAL_URL=https://host/v1/sign``.

- the certificate for the connection in mTLS to the Digital Green Certificate Gateway Service (the country of origin must be defined in the "_country_" field of the certificate subject) and pack it into a Java Key Store.
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

## Contributors
Here is a list of repository contributors. Thank you to everyone involved for improving this project, day by day.

# Licence

## Authors and Copyright

Copyright 2021 (c) Presidenza del Consiglio dei Ministri.

Please check the [AUTHORS](AUTHORS) file for extended reference.

## Third-party component licences

[TODO]

## Licence details

The licence for this repository is a [GNU Affero General Public Licence version 3](https://www.gnu.org/licenses/agpl-3.0.html) (SPDX: AGPL-3.0). Please see the [LICENSE](LICENSE) file for full reference.






























