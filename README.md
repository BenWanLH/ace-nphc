# Prerequisite

Java version: java openjdk-17.0.2

If you are using a different version of java for your own projects, consider downloading asdf
https://asdf-vm.com/ as it helps you to manage different runtime versions such as Java, node etc .

### To Start

To run simply run from the root folder `./gradlew bootRun`


### To Test

To test simply run from the root folder `./gradlew test`


### Assumptions 

- User would stick to the format described when uploading csv file  
- Csv always have header
- when updating user, all previous user info needs to be provided else it will be overwritten.

### Improvements

- Could have included the bonus of filtering and sorting.
- I probably should write my own exceptions handlers rather than using default springboot's BadRequestException


