# First Voices API Gateway

## Build Status

[![CircleCI](https://circleci.com/gh/plasticviking/firstvoices/tree/master.svg?style=svg&circle-token=ed6235db269a792f89a323cc89930dc486e08847)](https://circleci.com/gh/plasticviking/firstvoices/tree/master)

## Prerequisites
`npm install -g redoc-cli`

## Generating Documents
`gradle resolve` to generate API documentation

`redoc-cli bundle openapi/FirstVoicesAPI.json` to create a readable HTML document

## Starting Service
***For local testing***

`./gradlew appRun`

## Build for Lambda Deploy

`./gradlew buildZip` will produce a suitable lambda deployment package

## Environment Vars

Required environment settings for local or lambda container execution

 - *REDIS_URL* (optional, caching disabled if blank)
 - *JWKS_URL*
 - *NUXEO_HOST*
 - *NUXEO_USERNAME*
 - *NUXEO_PASSWORD*
