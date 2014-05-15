
# Anypoint Template: Salesforce to Database Account bidirectional sync

+ [License Agreement](#licenseagreement)
+ [Use Case](#usecase)
+ [Run it!](#runit)  
	* [Running on premise](#runonopremise)
    * [Running on CloudHub](#runoncloudhub)
	* [Properties to be configured](#propertiestobeconfigured)
+ [Customize It!](#customizeit)
    * [config.xml](#configxml)
    * [endpoints.xml](#endpointsxml)
    * [businessLogic.xml](#businesslogicxml)
    * [errorHandling.xml](#errorhandlingxml)
+ [Testing the Anypoint Template](#testingtheanypointtemplate)

# License Agreement <a name="licenseagreement"/>
Note that using this template is subject to the conditions of this [License Agreement](AnypointTemplateLicense.pdf).
Please review the terms of the license before downloading and using this template. In short, you are allowed to use the template for free with Mule ESB Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case <a name="usecase"/>

As a Salesforce admin, I want to have my accounts synchronized between Salesforce and Database organizations.

## Template overview <a name="templateoverview"/>

Let's say we want to keep Salesforce instance synchronized with Database instance. Then, the integration behavior can be summarized just with the following steps:

1. Ask Salesforce:
> *Which changes have there been since the last time I got in touch with you?*

2. For each of the updates fetched in the previous step (1.), ask Database:
> *Does the update received from Salesforce should be applied?*

3. If Database answer for the previous question (2.) is *Yes*, then *upsert* (create or update depending each particular case) Database with the belonging change

4. Repeat previous steps (1. to 3.) the other way around (using Database as source instance and Salesforce as the target one)

 Repeat *ad infinitum*:

5. Ask Salesforce:
> *Which changes have there been since the question I've made in the step 1.?*

And so on...
  
  
The question for recent changes since a certain moment is nothing but a [poll inbound][1] with a [watermark][2] defined.


# Run it! <a name="runit"/>

In order to have the template up and running just complete the two following steps:

 1. [Configure the application properties](#propertiestobeconfigured)
 2. Run it! ([on premise](#runonopremise) or [in Cloudhub](#runoncloudhub))

**Note:** This particular Anypoint Template illustrate the synchronization use case between SalesForce and a Database, thus it requires a DB instance to work.
The Anypoint Template comes packaged with a SQL script to create the DB table that uses. 
It is the user responsibility to use that script to create the table in an available schema and change the configuration accordingly.
The SQL script file can be found in [src/main/resources/account.sql] (../master/src/main/resources/account.sql)

## Properties to be configured<a name="propertiestobeconfigured"/>

### Application configuration
+ polling.frequency `10000`  
This are the milliseconds (also different time units can be used) that will run between two different checks for updates in Salesforce and Database

+ watermark.default.expression `2014-02-25T11:00:00.000Z`  
This property is an important one, as it configures what should be the start point of the synchronization.The date format accepted in SFDC Query Language is either *YYYY-MM-DDThh:mm:ss+hh:mm* or you can use Constants. [More information about Dates in SFDC][3]

### SalesForce Connector configuration
+ sfdc.username `jorge.drexler@mail.com`
+ sfdc.password `Noctiluca123`
+ sfdc.securityToken `avsfwCUl7apQs56Xq2AKi3X`
+ sfdc.url `https://login.salesforce.com/services/Soap/u/28.0`
+ sfdc.integration.user.id `005n0000000T3QkAAK`

### Database Connector configuration
+ database.url=jdbc:mysql://192.168.224.130:3306/mule?user=mule&password=mule
+ db.integration.user.id=mule@localhost

If it is required to connect to a different Database there should be provided the jar for the library and changed the value of that field in the connector.

## Running on premise <a name="runonopremise"/>

In this section we detail the way you have to run you Anypoint Temple on you computer.

### Running on Studio <a name="runonstudio"/>
Once you have imported you Anypoint Template into Anypoint Studio you need to follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources
+ Complete all the properties required as per the examples in the section [Properties to be configured](#propertiestobeconfigured)
+ Once that is done, right click on you Anypoint Template project folder 
+ Hover you mouse over `"Run as"`
+ Click on  `"Mule Application"`


### Running on Mule ESB stand alone <a name="runonmuleesbstandalone"/> 
Complete all properties in one of the property files, for example in [mule.prod.properties] (../blob/master/src/main/resources/mule.prod.properties) and run your app with the corresponding environment variable to use it. To follow the example, this will be `mule.env=prod`.


## Running on CloudHub <a name="runoncloudhub"/>

While [creating your application on CloudHub](http://www.mulesoft.org/documentation/display/current/Hello+World+on+CloudHub) (Or you can do it later as a next step), you need to go to Deployment > Advanced to set all environment variables detailed in **Properties to be configured** as well as the **mule.env**. 

### Deploying your Anypoint Template on CloudHub <a name="deployingyouranypointtemplateoncloudhub"/>
Mule Studio provides you with really easy way to deploy your Anypoint Template directly to CloudHub, for the specific steps to do so please check this [link](http://www.mulesoft.org/documentation/display/current/Deploying+Mule+Applications#DeployingMuleApplications-DeploytoCloudHub)

## Properties to be configured (With examples)<a name="propertiestobeconfigured"/>
In order to use this Anypoint Template you need to configure properties (Credentials, configurations, etc.) either in properties file or in CloudHub as Environment Variables. Detail list with examples:
...

# Customize It!<a name="customizeit"/>
This brief guide intends to give a high level idea of how this Anypoint Template is built and how you can change it according to your needs.
As mule applications are based on XML files, this page will be organised by describing all the XML that conform the Anypoint Template.
Of course more files will be found such as Test Classes and [Mule Application Files](http://www.mulesoft.org/documentation/display/current/Application+Format), but to keep it simple we will focus on the XMLs.

Here is a list of the main XML files you'll find in this application:

* [config.xml](#configxml)
* [dbConfiguration.xml](#dbconfigurationxml)
* [endpoints.xml](#endpointsxml)
* [businessLogic.xml](#businesslogicxml)
* [errorHandling.xml](#errorhandlingxml)


## config.xml<a name="configxml"/>
This file holds the configuration for Connectors and [Properties Place Holders](http://www.mulesoft.org/documentation/display/current/Configuring+Properties) are set in this file. 
**Even you can change the configuration here, all parameters that can be modified here are in properties file, and this is the recommended place to do it so.** 
Of course if you want to do core changes to the logic you will probably need to modify this file.

In the visual editor they can be found on the *Global Element* tab.

## dbConfiguration.xml<a name="dbconfigurationxml"/>
This file holds the configuration for Connectors and [Properties Place Holders][6]. 
Although you can update the configuration properties here, we highly recommend to keep them parameterized and modified them in the belonging property files.

For this particular template, what you will find in the config file is
* the configuration for Database instance that is being synced
* the property place holder configuration

## endpoints.xml<a name="endpointsxml"/>
This is the file where you will found the inbound and outbound sides of your integration app.
It is intented to define the application API.
...

## businessLogic.xml<a name="businesslogicxml"/>
This file holds the functional aspect of the Anypoint Template, directed by one flow responsible of conducting the business logic.
...


## errorHandling.xml<a name="errorhandlingxml"/>
This is the right place to handle how your integration will react depending on the different exceptions. 
This file holds a [Choice Exception Strategy](http://www.mulesoft.org/documentation/display/current/Choice+Exception+Strategy) that is referenced by the main flow in the business logic.
...


## Testing the Anypoint Template <a name="testingtheanypointtemplate"/>

You will notice that the Template has been shipped with test.
These devidi them self into two categories:

+ Unit Tests
+ Integration Tests

You can run any of them by just doing right click on the class and clicking on run as Junit test.

Do bear in mind that you'll have to tell the test classes which property file to use.
For you convinience we have added a file mule.test.properties located in "src/test/resources".
In the run configurations of the test just make sure to add the following property:

+ -Dmule.env=test

  [1]: http://www.mulesoft.org/documentation/display/current/Poll+Reference
  [2]: http://blogs.mulesoft.org/data-synchronizing-made-easy-with-mule-watermarks/
  [3]: http://www.salesforce.com/us/developer/docs/officetoolkit/Content/sforce_api_calls_soql_select_dateformats.htm

