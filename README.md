self-assessment-assist-stubs-api
========================

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
The self-assessment-assist-stubs is designed to support the following:
- self-assessment-assist

It is build using
- Scala 2.13.8
- Java 11
- sbt 1.7.2

## Development Setup
Run the microservice from the console using: `sbt run` (starts on port 8343)

Start the service manager profile: `sm --start SELF_ASSESSMENT_ASSIST_STUBS`

## Highlighted SBT Tasks
| Task                    | Description                                                                                          | Command
:------------------------|:-----------------------------------------------------------------------------------------------------|:-----
 test                    | Runs the standard unit tests                                                                         | ```$ sbt test```
 it:test                 | Runs the integration tests                                                                           | ```$ sbt it:test ```
 dependencyCheck         | Runs dependency-check against the current project. It aggregates dependencies and generates a report | ```$ sbt dependencyCheck```
 dependencyUpdates       | Shows a list of project dependencies that can be updated                                             | ```$ sbt dependencyUpdates```
 dependencyUpdatesReport | Writes a list of project dependencies to a file                                                      | ```$ sbt dependencyUpdatesReport```|


## Local Dev and QA Test Instructions
The QA test instruction documentation for local, QA and dev journeys (individual and agent) can be seen [here](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?spaceKey=TR&title=QA).

## Runbook

You can access the ITSA/HMRC Assist Runbook [here](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=519668164)

## Support and Reporting Issues

- You can raise non-technical or platform-related issues with the [Software Development Support Team](https://developer.service.hmrc.gov.uk/developer/support)

## API Reference / Documentation
Available on the [HMRC Developer Hub](https://developer.qa.tax.service.gov.uk/api-documentation/docs/api/service/self-assessment-assist/1.0)

## License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")