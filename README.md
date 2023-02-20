self-assessment-assist-stubs-api
========================

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

## Requirements
- Scala 2.12.x
- Java 8
- sbt 1.6.x
- [Service Manager](https://github.com/hmrc/service-manager)

## Development Setup
Run the microservice from the console using: `sbt run`

Start the service manager profile: `sm --start`

## Highlighted SBT Tasks
| Task                    | Description | Command
:------------------------|:------------|:-----
 test                    | Runs the standard unit tests | ```$ sbt test```
 func:test               | Runs the functional tests | ```$ sbt func:test ```
 dependencyCheck         | Runs dependency-check against the current project. It aggregates dependencies and generates a report | ```$ sbt dependencyCheck```
 dependencyUpdates       |  Shows a list of project dependencies that can be updated | ```$ sbt dependencyUpdates```
 dependencyUpdatesReport | Writes a list of project dependencies to a file | ```$ sbt dependencyUpdatesReport```|

## Run Tests
Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

## QA Test Instructions
The QA test instruction documentation for local, QA and dev journeys (individual and agent) can be seen [here](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?spaceKey=TR&title=QA).

## To view the OAS documention
To view documentation locally, ensure the Self Assessment Assist API is running.

Then go to http://localhost:9680/api-documentation/docs/openapi/preview and enter the full URL path to the YAML file with the appropriate port and version:

```
http://localhost:8342/api/conf/1.0/application.yaml
```

## Support and Reporting Issues

- You can raise non-technical or platform-related issues with the [Software Development Support Team](https://developer.service.hmrc.gov.uk/developer/support)

## API Reference / Documentation
Available on the [HMRC Developer Hub](https://developer.qa.tax.service.gov.uk/api-documentation/docs/api/service/self-assessment-assist/1.0)

## License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")