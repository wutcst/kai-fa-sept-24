[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/u1xW62gh)

# Campus Dungeon

Campus Dungeon is a Java/Maven dungeon exploration game with a campus defense theme. It started as a command-line Zuul-style adventure prototype and has evolved into a BYOG-inspired graphical dungeon game with seeded world generation, Swing tile rendering, fog of war, JSON save/load, inventory, NPC quests, and replayable input tests.

The player explores a randomly generated campus dungeon, collects preparation materials, talks to NPCs, solves a small course-themed puzzle, and reaches the defense hall to complete a software engineering practice defense.

## Features

- Seeded random dungeon generation with an `80 x 40` tile world.
- Java Swing tile renderer and HUD.
- Player movement with wall collision, direction, step counting, and redraw.
- Fog of war with three states: unseen, seen, and currently visible.
- JSON save/load through `save/campus-dungeon-save.json`.
- Inventory and item pickup.
- NPC quest flow with `librarian`, `assistant`, and `teacher`.
- Course puzzle flow using the Maven answer `pom.xml`.
- Defense hall gate requiring `report`, `laptop`, `slides`, and `pass`.
- BYOG-style replay API through `GameEngine.playWithInputString(String input)`.
- JUnit coverage for world generation, movement, fog, save/load, inventory, NPC quests, and win condition.

## Quick Start

Requirements:

- JDK 8 compatible source target. The project currently builds with the local JDK using Maven source/target 8.
- Maven.

Run tests:

```bash
mvn test
```

Run the graphical game:

```bash
mvn -q dependency:build-classpath -Dmdep.outputFile=target/runtime-classpath.txt
java -cp "target/classes:$(cat target/runtime-classpath.txt)" cn.edu.whut.sept.dungeon.Main
```

Run with a custom seed:

```bash
java -cp "target/classes:$(cat target/runtime-classpath.txt)" cn.edu.whut.sept.dungeon.Main 20260614
```

The extra runtime classpath is needed because the game uses Gson for JSON save/load. A later packaging task can replace this with an executable jar.

## Controls

| Key/Input | Action |
|---|---|
| `W/A/S/D` | Move up/left/down/right |
| `E` | Interact with the current tile |
| `I` | Show inventory/status message |
| `:Q` | Save and quit in replay input |
| `L` | Load saved game in replay input |

Replay input supports:

```text
n<seed>s   start new game
l          load save
w/a/s/d    move
e          interact
i          inventory/status
:q         save and quit
!answer(pom.xml) answer the Maven puzzle in tests/replay mode
```

Example:

```java
GameResult result = new GameEngine().playWithInputString("n20260614sddae:q");
```

## Gameplay

The core loop is:

```text
generate dungeon -> explore through fog -> collect helper items -> talk to NPCs -> solve puzzle -> unlock defense hall
```

Main quest rules:

- `student-card` is needed before the librarian issues `report`.
- `usb` is needed before the assistant can prepare demo materials.
- The assistant asks a Maven puzzle; the replay answer is `!answer(pom.xml)`.
- The assistant grants `laptop` and `slides` after the puzzle is solved.
- The teacher checks `report + laptop + slides` and then grants `pass`.
- The defense hall requires `report + laptop + slides + pass`.

Winning the game shows a defense completion message with step count.

## Architecture

The project keeps rendering, rules, persistence, and world generation separate:

| Package | Responsibility |
|---|---|
| `cn.edu.whut.sept.dungeon.core` | Game engine, input parsing, game state, movement, replay API |
| `cn.edu.whut.sept.dungeon.world` | Tiles, positions, rooms, corridors, seeded world generation |
| `cn.edu.whut.sept.dungeon.entity` | Items, inventory, NPCs |
| `cn.edu.whut.sept.dungeon.quest` | Quest progress state |
| `cn.edu.whut.sept.dungeon.render` | Swing frame, tile panel, renderer, HUD |
| `cn.edu.whut.sept.dungeon.io` | JSON save/load |

Important boundary:

- GUI input calls `GameEngine.handleInput`.
- Tests call `GameEngine.playWithInputString`.
- Rendering reads `GameState`; it does not implement game rules.
- JSON saves the current game state rather than only the seed.

## Testing

Run:

```bash
mvn test
```

Current coverage includes:

- Same seed generates the same world.
- Different seeds generate different worlds.
- Generated rooms are reachable.
- Player movement, wall collision, direction, and step counting.
- Fog visibility and explored-state persistence.
- JSON save/load for player, world, inventory, items, NPCs, and quest state.
- Item pickup and inventory display.
- NPC quest progression and Maven puzzle answer.
- Defense hall rejection and completion.
- Renderer color behavior for player, unseen tiles, and seen tiles.

## Save Files

Default save path:

```text
save/campus-dungeon-save.json
```

The `save/` directory is ignored by Git, so local play saves are not committed.

## Project History

Campus Dungeon grew out of an earlier `Campus Defense Zuul` prototype. The prototype validated command-line rooms, items, NPC dialogue, task gates, scoring, and endings. After that, the project pivoted to a graphical dungeon game because random worlds, fog exploration, save/load, and replayable engine tests made for a stronger game and a better engineering demonstration.

The old Zuul history is kept as project evolution evidence. The current implementation lives under:

```text
src/main/java/cn/edu/whut/sept/dungeon
src/test/java/cn/edu/whut/sept/dungeon
```

Campus Dungeon feature track:

| Issue | PR | Result |
|---|---|---|
| #19 Project pivot decision | - | Closed with README and issue evidence |
| #20 Core engine and replay API | #31 | Merged |
| #21 Seeded world generation | #32 | Merged |
| #22 Player movement and visibility state | #33 | Merged |
| #23 Swing renderer and HUD | #34 | Merged |
| #24 JSON save/load | #35 | Merged |
| #25 Fog of war states | #36 | Merged |
| #26 Items, inventory, and defense goal | #37 | Merged |
| #27 NPC quests and course puzzle | #38 | Merged |

## Collaboration Notes

This repository is also used for a software engineering practice course, so its development history intentionally uses Issues, feature branches, Pull Requests, and verification notes.

Team roles:

| Member | GitHub | Main focus |
|---|---|---|
| Member A | `siolyn` | Game features, architecture, rules |
| Member B | `sand8-ui` | Tests, CI, packaging, report evidence, review |

Open quality/documentation tracks may still include CI, packaging, and final report evidence.

## AI Assistance Disclosure

AI tools were used to help analyze course requirements, discuss project direction, plan Issues, draft documentation, suggest code structure, and design verification commands. The implementation was reviewed, run, modified, and submitted through the repository workflow, with Maven tests and manual launch checks used as verification evidence.
