# JavaCard Tutorial

[![Build Status](https://travis-ci.org/bertrandmartel/javacard-tutorial.svg?branch=master)](https://travis-ci.org/bertrandmartel/javacard-tutorial)
[![Coverage Status](https://coveralls.io/repos/github/bertrandmartel/javacard-tutorial/badge.svg?branch=master)](https://coveralls.io/github/bertrandmartel/javacard-tutorial?branch=master)

Examples from Eric Vétillard's [tutorial](http://javacard.vetilles.com/tutorial/) re-arranged in a Gradle project using [JavaCard gradle plugin](https://github.com/bertrandmartel/javacard-gradle-plugin) with additional tests

Each example is referenced as a Gradle module with :

* source under `main` sourceSet (sdk version can be selected from build.gradle) 
* simulation tests using [JCardSim](https://jcardsim.org/)
* unit test using JUnit
* tests on smartcard device using [jnasmartcardio](https://github.com/jnasmartcardio/jnasmartcardio) with [apdu4j logger](https://github.com/martinpaljak/apdu4j)
* quick testing scripts defining some gradle task to send defined apdu sequence 

It's also possible :
* to use `GPTool` and `GlobalPlatform` class under `test` which provide the same power as [GlobalPlatformPro](https://github.com/martinpaljak/GlobalPlatformPro) tool
* to use [`GPExec` gradle task](https://github.com/bertrandmartel/javacard-gradle-plugin#custom-global-platform-pro-task) to call [GlobalPlatformPro](https://github.com/martinpaljak/GlobalPlatformPro) from gradle to create custom tasks

## Setup

```bash
git clone git@github.com:bertrandmartel/javacard-tutorial.git
cd javacard-tutorial
git submodule update --init
```

## Build all examples 

```bash
./gradlew build
```

## jc101-1c : basic applet

http://javacard.vetilles.com/2006/09/17/hello-world-smart-card/

#### install

The following will build, delete applet if existing, install the applet : 
```bash
./gradlew :jc101-1c:build :jc101-1c:installJavaCard
```
#### run simulation tests

```bash
./gradlew :jc101-1c:test
```

#### run tests on smartcard

```bash
./gradlew :jc101-1c:test -DtestMode=smartcard
```

#### send apdu

From [gradle script task](https://github.com/bertrandmartel/javacard-tutorial/blob/master/jc101-1c/build.gradle#L20-L33) :

```bash
./gradlew :jc101-1c:sendHello
```

## jc101-2c : a simple counter

http://javacard.vetilles.com/2006/10/30/jc101-2c-a-simple-counter-for-smart-card-developers/

#### install

The following will build, delete applet if existing, install the applet : 
```bash
./gradlew :jc101-2c:build :jc101-2c:installJavaCard
```

#### run simulation tests

```bash
./gradlew :jc101-2c:test
```

#### run tests on smartcard

```bash
./gradlew :jc101-2c:test -DtestMode=smartcard
```

#### send apdu

From [gradle script task](https://github.com/bertrandmartel/javacard-tutorial/blob/master/jc101-2c/build.gradle#L19-L48) :

* balance

```bash
./gradlew :jc101-2c:balance
```

* credit 5

```bash
./gradlew :jc101-2c:credit
```

* debit 5
  
```bash
./gradlew :jc101-2c:debit
```

## jc101-password : password application

* http://javacard.vetilles.com/2006/11/07/jc101-3c-a-real-application/
* http://javacard.vetilles.com/2007/02/06/jc101-4c-a-basic-password-manager/
* http://javacard.vetilles.com/2008/01/07/jc101-5c-data-management-and-transactions/
* http://javacard.vetilles.com/2008/01/15/jc101-6c-specifying-the-apdus/
* http://javacard.vetilles.com/2008/04/05/jc101-7c-processing-apdus-12/
* http://javacard.vetilles.com/2008/04/09/jc101-8c-processing-apdu%E2%80%99s-22/

#### install

The following will build, delete applet if existing, install the applet : 
```bash
./gradlew :jc101-password:build :jc101-password:installJavaCard
```

#### run simulation tests

```bash
./gradlew :jc101-password:test
```

#### run tests on smartcard
 
```bash
./gradlew :jc101-password:test -DtestMode=smartcard
```

#### send apdu

From [gradle script task](https://github.com/bertrandmartel/javacard-tutorial/blob/master/jc101-password/build.gradle#L19-L56) :

* add password entry 

```bash
./gradlew :jc101-password:addPassword
```

* delete password entry 

```bash
./gradlew :jc101-password:removePassword
```

* list identifiers 

```bash
./gradlew :jc101-password:listPassword
```

## jc101-password-pin : password application with PIN security

* http://javacard.vetilles.com/2008/04/16/jc101-9c-authentication-and-lifecycle/
* http://javacard.vetilles.com/2008/04/21/jc101-10c-adding-a-password-and-state-management/

#### install

The following will build, delete applet if existing, install the applet : 
```bash
./gradlew :jc101-password-pin:build :jc101-password-pin:installJavaCard
```

#### run simulation tests

```bash
./gradlew :jc101-password-pin:test
```

#### run tests on smartcard

```bash
./gradlew :jc101-password-pin:test -DtestMode=smartcard
```

#### send pin code apdu

* set pin code

```bash
./gradlew :jc101-password-pin:setPinCode
```

#### send apdu

From [gradle script task](https://github.com/bertrandmartel/javacard-tutorial/blob/master/jc101-password-pin/build.gradle#L24-L81) :

* add password entry 

```bash
./gradlew :jc101-password-pin:addPassword
```

* delete password entry 

```bash
./gradlew :jc101-password-pin:removePassword
```

* list identifiers 

```bash
./gradlew :jc101-password-pin:listPassword
```