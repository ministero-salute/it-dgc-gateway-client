<h1 align="center">EU Digital COVID Certificate Gateway Client</h1>

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
This repository contains the source code of the EU Digital COVID Certificate Gateway Client.

The Gateway Client is part of the national backends and periodically downloads the public keys that are distributed through the DGCG. It is the only point of communication of the national backends with the European Gateway, also allowing the Member State to upload its national DSC (Document Signer Certificate) with which the digital certificates are signed.

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
git clone git@github.com:ministero-salute/it-dgc-gateway-client.git
cd it-dgc-gateway-client
mvn clean package
```

Then, copy the file ``application.properties`` contained in the path ``./src/main/resources`` into the ``./it-dgc-gateway-client/config`` folder:
```shell script
mkdir -p it-dgc-gateway-client/config
cp ./src/main/resources/application.properties ./it-dgc-gateway-client/config
```

You need also a ``security`` folder which must contain both an ``sslclient`` and a ``truststore`` directory:
```shell script
mkdir -p security/sslclient
mkdir -p security/truststore
```

By default the docker image uses a local mongodb instance running on  `` mongodb://127.0.0.1:27017``, you can always change the connection url by editing the envar in the enviroment section of the ``docker-compose.yml``:

```
environment:
    MONGO_DB_URI=mongodb://user:password@mongodb:27017/DGC-dev
```

Also you need the DGCG (Digital Green Certificate Gateway) server running locally, you can install it from the public repo [Digital Green Certificate Gateway](https://github.com/eu-digital-green-certificates/dgc-gateway).

To properly work the client needs also:

- an external signature service (rest API) ``SIGN_EXTERNAL_URL=https://host/v1/sign``.

- the certificate for the connection in mTLS to the Digital Green Certificate Gateway Service (the country of origin must be defined in the "_country_" field of the certificate subject) and pack it into a Java Key Store.
```
environment:
      - DGC_BASE_URL=https://example.dgc.eu
      - SSLDGC_JKS_PATH=/security/sslclient/ssldgc.jks
      - SSLDGC_JKS_PASSWORD=password
      - SSLDGC_CERT_PASSWORD=password
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
      - TRUST_DGC_ANCHOR_ALIAS=anchor_alias
```

Once the requirements above shown are satisfied open a shell with working directory and execute

```shell script
docker-compose up --build
```
#### Dependencies

The project has been implemented in [Java 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html).

[Maven](http://maven.apache.org/) is used for dependency management. Maven is a build manager tool and mostly used in java projects. Maven was built on a central concept of project object model (POM).

The ```pom.xml``` contains all necessary information about the project, as well as configurations of plugins to be used during the build process.

The backend services follow a micro-service architecture, where each critical functionality is deployed as its own component. Components are distributed in dedicated [Docker](https://www.docker.com/) images, Docker being an industry standard platform for the containerization and virtualization of software.

The following dependencies are used to implement the business logic:

- **[spring data mongo](https://spring.io/projects/spring-data-mongodb).** A libray which provides integration with the MongoDB document database. Key functional areas of Spring Data MongoDB are a POJO centric model for interacting with a MongoDB DBCollection and easily writing a Repository style data access layer. Released as an open-source project under the Apache 2.0 licence.
- **[Lombok](https://projectlombok.org/).** A Java library tool that generates code for minimizing boilerplate code. The library replaces boilerplate code with easy-to-use annotations.For example, by adding a couple of annotations, you can get rid of code clutters, such as getters and setters methods, constructors, hashcode, equals, and toString methods, and so on.
Lombok is an open-source project released under the MIT licence.
- **[springdoc-openapi](https://springdoc.org/).** A library that helps automating the generation of API documentation using spring boot projects. springdoc-openapi works by examining an application at runtime to infer API semantics based on spring configurations, class structure and various annotations. Released as an open-source project under the Apache 2.0 licence.
- **[BouncyCastle](https://www.bouncycastle.org/).** a Java library that complements the default Java Cryptographic Extension (JCE). In this introductory article, we're going to show how to use BouncyCastle to perform cryptographic operations, such as encryption and signature. BouncyCastle is released as an open-source project under an adaptation of the MIT X11 licence.
- **[AkamaiOPEN-edgegrid-java](https://developer.akamai.com/libraries).** A Java library for EdgeGrid Client Authentication. Provides a client independent implement as well as concrete implementations for REST-assured and Google HTTP Client Library for Java integration. AkamaiOPEN-edgegrid-java is released as an open-source project under an adaptation of the Apache 2.0 licence.
- **[JaCoCo](https://www.eclemma.org/jacoco/trunk/doc/maven.html).** A Maven plug-in that provides the JaCoCo runtime agent to your tests and allows basic report creation. JaCoCo is released as an open-source project under the EPL 2.0 licence.

# Contributing
Contributions are most welcome. Before proceeding, please read the [Code of Conduct](./CODE_OF_CONDUCT.md) for guidance on how to approach the community and create a positive environment. Additionally, please read our [CONTRIBUTING](./CONTRIBUTING.md) file, which contains guidance on ensuring a smooth contribution process.

## Contributors
Here is a list of repository contributors. Thank you to everyone involved for improving this project, day by day.

# Licence

## Authors and Copyright

Copyright 2021 (c)  Ministero della Salute.

Please check the [AUTHORS](AUTHORS) file for extended reference.

## Third-party component licences

## Licence details

The licence for this repository is a [GNU Affero General Public Licence version 3](https://www.gnu.org/licenses/agpl-3.0.html) (SPDX: AGPL-3.0). Please see the [LICENSE](LICENSE) file for full reference.




















