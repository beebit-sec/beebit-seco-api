# beebit-seco-api
Project **Beebit** provides a set of modules for developers who attempt to secure data communication.

Module **beebit-seco-api** is a RESTful API wrapper for providing security computation offloading. Most security computations, such as Ciphertext-Policy Attribute-Based Encryption (CP-ABE), are too computation-intensive to be performed on resource-restricted devices. However, security and privacy are demanded in most of Internet of Things (IoT) applications. As a SECurity-as-a-Service (SECaaS), beebit-seco-api can be installed in edge or cloud servers. Then, resource-restricted devices can request SECaaS to secure their data by simply submitting an HTTP request.

At this stage, only CP-ABE is supported. However, the design of beebit-seco-api is to be as extensible as possible. It is easy to add a new security computation in beebit-seco-api.

# Installation
## Database
Database is required to store user and project information.

Table: users

Filed Name | Type | Description
--- | --- | ---
uid | character(32) | User Id

Table: project

Filed Name | Type | Description
--- | --- | ---
pid | character(32) | Project Id
uid | character(32) | User Id of project owner

## Install beebit-cpabe-sdk
Java Native Interface (JNI) is applied to use native library of beebit-cpabe-sdk which project security computation of CP-ABE. 
Please refer to module [**beebit-cpabe-sdk**](https://github.com/ucanlab/beebit-cpabe-sdk) for installing this library.

## Install beebit-seco-api
Module beebit-seco-api is a Gradle-based project. With [IntelliJ IDEA](https://www.jetbrains.com/idea/), developers can simply import and run the porject.
- Connect database by modifying Utils.java.
- Pass -Djava.library.path=[path to beebit-cpabe-sdk library] to Java VM.

# User Manual
Congratulation! You can use beebit-seco-api as SECaaS now as the following steps.

**Step 1.** User registration (/user/register)

**Step 2.** Project creation (/project/create)

**Step 3.** CP-ABE initialization (/project/init/cpabe)

**Step 4a.** CP-ABE key generation (/cpabe/keygen)

**Step 4b.** CP-ABE data encryption (/cpabe/enc/data)

**Step 4c.** CP-ABE data decryption (/cpabe/dec/data)

For more information, refer to [API Reference](https://docs.google.com/document/d/1hGh1D-iPw0rFw56M9y_l2mXL866ln9mCNoBfmd_H0_E/edit?usp=sharing).
