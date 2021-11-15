---
title: Fama endpoint
keywords: 
last_updated: May 10, 2018
tags: []
summary: "Detailed description of the API of the Fama endpoint."
---

## Overview

This endpoint allows access to the information provided by [Fama](https://www.fama.io) through its REST API. Features
of this endpoint are:

- Create persons in Fama
- Add social media profiles
- Get report metrics of people 
- Webhook to receive events from Fama (currently Person Complete Callback)

Please make sure you take a look at the documentation from Fama as features are based on its API. Even though there is 
still no documentation online, you can contact support and get it using the [website](https://web.fama.io) for clients.

## Configuration

Before configuring the endpoint you will need to have an account for in Fama able to login in their [website](https://web.fama.io). 

### User Name 

This is the user name managed by Fama.

### User Password 

This is the password managed by Fama.

### Webhook token

String token to validate event requests coming from Fama. By default a random hash is generated. This token and generated 
webhook URL must be provided to Fama by email using a password protected PDF. Contact [Brian](mailTo:brian@fama.io) or 
[Jordan](mailTo:jordan@fama.io) 

## Javascript API

The Javascript API of the endpoint is based on the [Fama](https://web.fama.io) REST API,
so you should see their documentation for details on parameters and data formats. If there are differences
they will be explained here.

### Person

For now, API only allows to work with functions related to persons.


#### Create

```js
var createdPersonInfo = app.endpoints.fama.persons.create(personInfo);
```

Create a new person from an object with the person details.

Samples:

```js
var person = {
   firstName: 'Example',
   lastName: 'Test', 
   emailList: [{email:'example@domain.com'}]
};
var created = app.endpoints.fama.persons.create(person);
if (created.uuid) {
    //do something
}
```

#### Add social media profile

```js
var profile = app.endpoints.fama.persons.addSocialMediaProfile(uuid, profileObject);
```

Adds a social media profile to person. The profile object only contains a key called `value` and it must contain a valid 
complete URL to a social network profile.

From technical support: Send the Add Person callback to add the person. Immediately after you receive the `uuid`, add the 
social media profiles. If you wait too long and the report starts processing then the profiles will not be added.

Samples:

```js
var profile = app.endpoints.fama.persons.addSocialMediaProfile('b2327872-aec2-11e7-b902-05cee4ab6590', {value: "https://www.facebook.com/john.doe"});
if (profile.value) {
    //do something with the Fama auto-generated profile identifier
}
```

#### Get report metrics

```js
var metrics = app.endpoints.fama.persons.getReport(uuid);
```

Retrieves report metrics of person based on `uuid`. Keep in mind this method should be invoked after 
`Person complete callback` was called (see below).

Samples:

```js
var person = app.endpoints.fama.persons.getReport('b2327872-aec2-11e7-b902-05cee4ab6590');
```

#### Get report PDF

```js
var metrics = app.endpoints.fama.persons.getReportPdf(uuid, fileName);
```

Retrieves report metrics of person based on `uuid` in a PDF file that is uploaded to platform using optional `fileName`. 
Keep in mind this method should be invoked after `Person complete callback` was called (see below).

Samples:

```js
var fileRef = app.endpoints.fama.persons.getReportPdf('b2327872-aec2-11e7-b902-05cee4ab6590', 'myReport.pdf');
sys.files.share(fileRef.fileId);
```

## Events

### Webhook (Person Complete Callback)

For now, default `Webhook` event is provided to be configured. Since Fama only dispatch one callback (`Person Complete Callback`)
this will be package that information into the event and dispatch it to listeners.

As it was mentioned above, to enable this event developers must provide to Fama the `webhookURL` generated for this endpoint
and the `webhookToken`. This last element, must be sent by Fama as a header called `token`. Please, give this information
to your contact in Fama.

## About SLINGR

SLINGR is a low-code rapid application development platform that accelerates development, with robust architecture for integrations and executing custom workflows and automation.

[More info about SLINGR](https://slingr.io)

## License

This endpoint is licensed under the Apache License 2.0. See the `LICENSE` file for more details.
