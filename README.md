# hangman-service

## Assumptions
* If player 1 makes a guess, shares the same link w/ player 2 that the turn still belongs to player 1. It's not until 
player 1 makes the guess that player 2 then gets to guess after. (Another option could be to just wait until a player does a guess first)
* If player 1 starts a new game, shares the same link w/ player 2 and then clicks to start a new game, we consider it 
abandoned and player 2 continues playing with himself. 

## How To





## Operational Excellence
- Add metrics interceptor that captures success & error metrics per API
- All logs should be sent to output and ultimately to infrastructure. 