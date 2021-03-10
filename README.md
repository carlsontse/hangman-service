# hangman-service

## Assumptions
* If player 1 makes a guess, shares the same link w/ player 2 that the turn still belongs to player 1. It's not until 
player 1 makes the guess that player 2 then gets to guess after. (Another option could be to just wait until a player does a guess first)
* If player 1 starts a new game, shares the same link w/ player 2 and then clicks to start a new game, we consider it 
abandoned and player 2 continues playing with himself. 

## Notes
### Time Spent
~ 10 hours

### REST decision
* I went with REST because it serves web applications quite nicely w/ JSON responses and I also felt the APIs fit the REST model very well.
* I prefer an API first approach which is to build out the Open API definitions first (instead of generating the swagger docs after implementation) 
  but that requires more tooling to keep the definitions and service in sync (which i haven't done before w/ REST/Open API).

### Storage
* In general, my go-to storage has been mongodb/DynamoDB simply for simplicity sake (infrastructure setup, etc.). I think a NOSQL solution
would work just fine with representing the entities such as the Game and Guess. The model is quite simple with not much relationships. We'd have to think
  about sharding, primary, secondary, etc. I think that with a game like this, updates are important having the latest updates show up for multiple players.

### Feature Idea List
* Supporting other locales (no clue if other languages lend well to this, i think latin based ones?)
* Fun things like names for the game, which becomes like a 'room' as well, have a chat box to talk trash
* Add user accounts to keep track of wins/losses
* Have a game lobby to see all the games and join them

## How To
### Prerequisites
* JDK 12
* Maven 3.6.1 

### Run tests
`mvn test`

### View Jacoco report
1. `mvn test jacoco:report`
1. Open `/target/site/jacoco/index.html`

### Run locally w/ Maven
`mvn spring-boot:run`

### Access Swagger API definitions
Assuming port 8080, the url is: `http://localhost:8080/v1/hangman-service/swagger-ui.html`

## Operational Excellence
* All metrics can be viewed here: `http://localhost:8080/v1/hangman-service/actuator/prometheus`. All the HTTP metrics are
  already in there so you can see how many successful or failure calls per API. This is great for being able to setup alerts per API.
  We can ship these metrics using some prometheus scraper in infrastructure and dump them to some time series DB.
* All logs are sent to output and ultimately to infrastructure to handle the storage. 
* There's an MDC filter that contains the session ID and we can use it to store other session/contextual stuff in there passed in the headers. 
    These MDC values show up in the logs to help debug sessions.
* I won't have time to do this but we can include a Docker file to build an image that just runs the Spring Boot Jar, or even better Maven
has a plugin that will do this plus upload it to a Docker repository to plug into any CI.