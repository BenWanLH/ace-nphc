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
- when updating user, user of the api is required to provide all the updated user's data even if they want to update only 1 field.

### Improvements

- Could have included the bonus of sorting.
- I probably should write my own exceptions handlers rather than using default springboot's BadRequestException
- update api could probably be more flexible to allow user to only specify what fields they want to update
- getAllUsers api should be paginated as the number of data increase

