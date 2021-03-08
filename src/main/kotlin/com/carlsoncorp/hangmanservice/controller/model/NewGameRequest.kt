package com.carlsoncorp.hangmanservice.controller.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Min
import javax.validation.constraints.Size

@ApiModel(description = "Class representing a request to create a new Hangman game")
class NewGameRequest () {
    @ApiModelProperty(notes = "Max number of guesses.", example = "8", required = false, position = 0)
    @Min(1)
    var maxNumberOfGuesses: Int? = null

    @ApiModelProperty(notes = "Secret word to guess.", example = "hangman", required = false, position = 1)
    @Size(min = 1)
    var secretWord: String? = null
}