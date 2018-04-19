
# Anypoint Template: Salesforce To Database Account Bidirectional Synchronization

+ [License Agreement](#licenseagreement)
+ [Use Case](#usecase)
+ [Considerations](#considerations)
	* [DB Considerations](#dbconsiderations)
	* [Salesforce Considerations](#salesforceconsiderations)
+ [Run it!](#runit)
	* [Running on premise](#runonopremise)
	* [Running on Studio](#runonstudio)
	* [Running on Mule ESB stand alone](#runonmuleesbstandalone)
	* [Running on CloudHub](#runoncloudhub)
	* [Deploying your Anypoint Template on CloudHub](#deployingyouranypointtemplateoncloudhub)
	* [Properties to be configured (With examples)](#propertiestobeconfigured)
+ [API Calls](#apicalls)
+ [Customize It!](#customizeit)
	* [config.xml](#configxml)
	* [businessLogic.xml](#businesslogicxml)
	* [endpoints.xml](#endpointsxml)
	* [errorHandling.xml](#errorhandlingxml)


# License Agreement <a name="licenseagreement"/>
Note that using this template is subject to the conditions of this [License Agreement](AnypointTemplateLicense.pdf).
Please review the terms of the license before downloading and using this template. In short, you are allowed to use the template for free with Mule ESB Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case <a name="usecase"/>
This Template should serve as a foundation for setting an online bi-directional sync of Accounts between Salesforce and Database instances with ability to specify filtering criteria.
The main behavior of this template is fetching data for changes (new or modified Accounts) that have occurred in Salesforce and Database instances during a certain defined period of time. For those Accounts, that were identified not present in the target instance, the integration triggers an insert or update operation on the existence of the object in the target instance taking the last modification of the object as the one that should be applied.

# Considerations <a name="considerations"/>

**Note:** This particular Anypoint Template illustrate the migration use case between Salesforce and Database, thus it requires Account instances to work.
The Anypoint Template comes packaged with SQL script to create the DB table that it uses. 
It is the user's responsibility to use provided script to create the table in an available schema and change the configuration accordingly.
The SQL script file can be found in [src/main/resources/](../master/src/main/resources/)

This template is customized for MySQL. To use it with different SQL implementation, some changes are necessary:
* update SQL script dialect to desired one
* replace MySQL driver library (or add another) dependency to desired one in [POM](pom.xml)
* update Database Config to suitable connection instead of `db:my-sql-connection` in global elements (config.xml)
* update connection configurations in `mule.*.properties` file

To make this Anypoint Template run, there are certain preconditions that must be considered. All of them deal with the preparations in both, 
that must be made in order for all to run smoothly. 
**Failing to do so could lead to unexpected behavior of the template.**

## DB Considerations <a name="dbconsiderations"/>

There may be a few things that you need to know regarding DB, in order for this template to work.

This Anypoint Template may be using date time/timestamp fields from the DB in order to do comparisons and take further actions.
While the template handles the time zone by sending all such fields in a neutral time zone, it can not handle **time offsets**.
We define as **time offsets** the time difference that may surface between date time/timestamp fields from different systems due to a differences in the system's internal clock.
The user of this template should take this in consideration and take the actions needed to avoid the time offset.

### As source of data

There are no particular considerations for this Anypoint Template regarding DB as data origin.
### DB as destination of data

There are no particular considerations for this Anypoint Template regarding DB as data destination.

## Salesforce Considerations <a name="salesforceconsiderations"/>

There may be a few things that you need to know regarding Salesforce, in order for this template to work.

In order to have this template working as expected, you should be aware of your own Salesforce field configuration.

### FAQ

 - Where can I check that the field configuration for my Salesforce instance is the right one?

    [Salesforce: Checking Field Accessibility for a Particular Field][1]

- Can I modify the Field Access Settings? How?

    [Salesforce: Modifying Field Access Settings][2]


[1]: https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US
[2]: https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US

### As source of data

If the user configured in the template for the source system does not have at least *read only* permissions for the fields that are fetched, then a *InvalidFieldFault* API fault will show up.

```
java.lang.RuntimeException: [InvalidFieldFault [ApiQueryFault [ApiFault  exceptionCode='INVALID_FIELD'
exceptionMessage='
Account.Phone, Account.Rating, Account.RecordTypeId, Account.ShippingCity
^
ERROR at Row:1:Column:486
No such column 'RecordTypeId' on entity 'Account'. If you are attempting to use a custom field, be sure to append the '__c' after the custom field name. Please reference your WSDL or the describe call for the appropriate names.'
]
row='1'
column='486'
]
]
```

### As destination of data

There are no particular considerations for this Anypoint Template regarding Salesforce as data destination.









# Run it! <a name="runit"/>
Simple steps to get Salesforce To Database Account Bidirectional Synchronization running.
In order to have your application up and running you just need to complete two simple steps:
 1. [Configure the application properties](#propertiestobeconfigured)
 2. Run it! ([on premise](#runonopremise) or [in Cloudhub](#runoncloudhub))

## Running on premise <a name="runonopremise"/>
In this section we detail the way you should run your Anypoint Template on your computer.


### Where to Download Mule Studio and Mule ESB
First thing to know if you are a newcomer to Mule is where to get the tools.

+ You can download Mule Studio from this [Location](http://www.mulesoft.com/platform/mule-studio)
+ You can download Mule ESB from this [Location](http://www.mulesoft.com/platform/soa/mule-esb-open-source-esb)


### Importing an Anypoint Template into Studio
Mule Studio offers several ways to import a project into the workspace, for instance: 

+ Anypoint Studio Project from File System
+ Packaged mule application (.jar)

You can find a detailed description on how to do so in this [Documentation Page](http://www.mulesoft.org/documentation/display/current/Importing+and+Exporting+in+Studio).


### Running on Studio <a name="runonstudio"/>
Once you have imported you Anypoint Template into Anypoint Studio you need to follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources
+ Complete all the properties required as per the examples in the section [Properties to be configured](#propertiestobeconfigured)
+ Once that is done, right click on you Anypoint Template project folder 
+ Hover you mouse over `"Run as"`
+ Click on  `"Mule Application"`


### Running on Mule ESB stand alone <a name="runonmuleesbstandalone"/>
Fill in all properties in one of the property files, for example in [mule.dev.properties] (./src/main/resources/mule.dev.properties) and run your app 
with the corresponding environment variable to use it. To follow the example, this will be `mule.env=dev`. 


## Running on CloudHub <a name="runoncloudhub"/>
While [creating your application on CloudHub](http://www.mulesoft.org/documentation/display/current/Hello+World+on+CloudHub) (Or you can do it later as a next step), you need to go to Deployment > Advanced to set all environment variables detailed in **Properties to be configured** as well as the **mule.env**.
In order to [create your application on CloudHub](http://www.mulesoft.org/documentation/display/current/Hello+World+on+CloudHub) 
you should to go to Deployment > Advanced to set all environment variables detailed in **Properties to be configured** as well as the **mule.env**.

### Deploying your Anypoint Template on CloudHub <a name="deployingyouranypointtemplateoncloudhub"/>
Mule Studio provides you with really easy way to deploy your Template directly to CloudHub, for the specific steps to do so please check this [link](http://www.mulesoft.org/documentation/display/current/Deploying+Mule+Applications#DeployingMuleApplications-DeploytoCloudHub)


## Properties to be configured (With examples) <a name="propertiestobeconfigured"/>
In order to use this Mule Anypoint Template you need to configure properties (Credentials, configurations, etc.) either in properties file or in CloudHub as Environment Variables. Detail list with examples:
### Application configuration
#### Application configuration
+ scheduler.frequency `10000`  
This are the milliseconds that will run between two different checks for updates in either Database instance and Salesforce instance.

+ scheduler.startDelay `0`

+ watermark.default.expression `2018-02-25T11:00:00.000Z`  
This property is an important one, as it configures what should be the start point of the synchronization. If the use case includes synchronization of every 
account created from the begining of the times, you should use a date previous to any account creation (perhaphs `1900-01-01T08:00:00.000Z` is a good choice). 
If you want to synchronize the accounts created from now on, then you should use a default value according to that requirement (for example, 
if today is April 21st of 2018 and it's eleven o'clock in London, then you could use the following value `2018-04-21T11:00:00.000Z`).

+ page.size `1000`


#### SalesForce Connector configuration
+ sfdc.username `jorge.drexler@mail.com`
+ sfdc.password `Noctiluca123`
+ sfdc.securityToken `avsfwCUl7apQs56Xq2AKi3X`
+ sfdc.integration.user.id `005n0000000T3QkAAK`

	**Note:** To find out the correct *sfdc.integration.user.id* value, please, refer to example project **Salesforce Data Retrieval** in [Anypoint Exchange](http://www.mulesoft.org/documentation/display/current/Anypoint+Exchange).

#### Database Connector configuration
+ db.host `localhost`
+ db.port `3306`
+ db.databaseName `dbname`
+ db.user `user-name`
+ db.password `user-password`
+ db.integration.user.id `user-id`

# API Calls <a name="apicalls"/>
Not relevant for this use case.


# Customize It!<a name="customizeit"/>
This brief guide intends to give a high level idea of how this Anypoint Template is built and how you can change it according to your needs.
As mule applications are based on XML files, this page will be organized by describing all the XML that conform the Anypoint Template.
Of course more files will be found such as Test Classes and [Mule Application Files](http://www.mulesoft.org/documentation/display/current/Application+Format), but to keep it simple we will focus on the XMLs.

Here is a list of the main XML files you'll find in this application:

* [config.xml](#configxml)
* [endpoints.xml](#endpointsxml)
* [businessLogic.xml](#businesslogicxml)
* [errorHandling.xml](#errorhandlingxml)


## config.xml<a name="configxml"/>
Configuration for Connectors and [Configuration Properties](http://www.mulesoft.org/documentation/display/current/Configuring+Properties) are set in this file. **Even you can change the configuration here, all parameters that can be modified here are in properties file, and this is the recommended place to do it so.** Of course if you want to do core changes to the logic you will probably need to modify this file.

In the visual editor they can be found on the *Global Element* tab.


## businessLogic.xml<a name="businesslogicxml"/>
This file holds the functional aspect of the template. Its main component is a [Batch Job](http://www.mulesoft.org/documentation/display/current/Batch+Processing), and it includes steps for both executing the synchronization from Salesforce to Database , and the other way around.



## endpoints.xml<a name="endpointsxml"/>
This file should contain every inbound and outbound endpoint of your integration app. 
In this particular template, this file contains a scheduler endpoint that query Salesforce and Database for updates using watermark.



## errorHandling.xml<a name="errorhandlingxml"/>
This is the right place to handle how your integration will react depending on the different exceptions. 
This file holds a [Error Handling](http://www.mulesoft.org/documentation/display/current/Error+Handling) that is referenced by the scheduler flow in the endpoints xml file.



