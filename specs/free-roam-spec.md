# Free Roam Spec

## Feature Name

Free Roam

## Game Mode ID

5

## Description

Type plain-English rover instructions and execute them in open space without a physical game board.

## Move Vocabulary

| Move Name | Parameters | Plain-English Aliases |
| --- | --- | --- |
| `spin_left` | `duration_ms` | spin left, rotate left, do a left spin, turn in place left |
| `spin_right` | `duration_ms` | spin right, rotate right, do a right spin, turn in place right |
| `forward` | `distance_cm` | move forward, go forward, drive forward, advance |
| `backward` | `distance_cm` | move backward, go backward, reverse, back up |
| `turn_left` | `degrees` | turn left, rotate left by, pivot left |
| `turn_right` | `degrees` | turn right, rotate right by, pivot right |
| `figure_8` | `repetitions` | figure 8, figure eight, do a figure eight, make an 8 |
| `square` | `side_cm` | drive a square, make a square, square path |
| `patrol` | `duration_ms` | patrol, roam, sweep the area, patrol the space |

## Calibration Constants

- `ms_per_degree`: 8
- `ms_per_cm`: 18
- `max_duration_ms`: 30000

## LLM System Prompt

You translate user rover requests into semantic move JSON for the Free Roam game mode.
Use only the move names defined in the Move Vocabulary section of this spec: `spin_left`, `spin_right`, `forward`, `backward`, `turn_left`, `turn_right`, `figure_8`, `square`, `patrol`.
Return JSON only. Do not return Markdown. Do not explain your reasoning.
The response must be a JSON object with this shape:
```json
{
  "moves": [
    {
      "name": "forward",
      "parameters": {
        "distance_cm": 25
      }
    }
  ]
}
```
Rules:
- Each entry in `moves` must use exactly one allowed move name.
- `parameters` must include only the fields required by that move.
- Convert vague natural language into safe concrete values.
- Keep the full plan within `max_duration_ms = 30000`.
- If the request is unsafe, impossible, or unrelated to rover movement, return `{ "moves": [] }`.
- Never invent unsupported commands or fields.

## Safety Rules

- `max_step_duration_ms`: 10000
- `max_duration_ms`: 30000
- `soft_boundary_radius_cm`: 150

## UI Labels

- `prompt_placeholder`: Tell the rover what to do, for example: do a figure 8, spin left for 2 seconds, then patrol.
- `execute_button_label`: Execute
- `cancel_button_label`: Stop
- `status_executing`: Executing rover script...
- `status_cancelled`: Rover script cancelled.
- `status_error`: Could not interpret that rover command.

## Board Required

no
