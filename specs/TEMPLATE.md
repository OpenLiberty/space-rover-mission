# Rover Feature Spec Template

## Feature Name

[fill in short feature name]

## Game Mode ID

[fill in numeric game mode id]

## Description

[fill in one-sentence description shown in the game mode selector UI]

## Move Vocabulary

| Move Name | Parameters | Plain-English Aliases |
| --- | --- | --- |
| [fill in move name] | [fill in accepted parameters] | [fill in aliases the LLM should recognise] |

## Calibration Constants

- `ms_per_degree`: [fill in milliseconds per degree of turn]
- `ms_per_cm`: [fill in milliseconds per centimetre of forward or backward motion]
- `max_duration_ms`: [fill in maximum total script duration in milliseconds]

## LLM System Prompt

[fill in the exact system prompt to send to the LLM]

## Safety Rules

- `max_step_duration_ms`: [fill in maximum duration per step in milliseconds]
- `max_duration_ms`: [fill in maximum total duration in milliseconds]
- `soft_boundary_radius_cm`: [fill in soft boundary radius in centimetres]

## UI Labels

- `prompt_placeholder`: [fill in prompt input placeholder text]
- `execute_button_label`: [fill in execute button label]
- `cancel_button_label`: [fill in cancel button label]
- `status_executing`: [fill in executing status message]
- `status_cancelled`: [fill in cancelled status message]
- `status_error`: [fill in error status message]

## Board Required

[fill in yes or no]
