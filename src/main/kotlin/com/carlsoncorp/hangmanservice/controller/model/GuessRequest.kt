package com.carlsoncorp.hangmanservice.controller.model

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Size

@ApiModel(description = "Class representing a request to perform a guess")
class GuessRequest () {
    @ApiModelProperty(notes = "Guess letter.", example = "a", required = true, position = 0)
    @Size(min = 1, max = 1)
    var letter: String? = null
}