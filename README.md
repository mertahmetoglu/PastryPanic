# 🥐 PastryPanic

A fast-paced 2D arcade game built in Java where you catch falling pastries to survive as long as possible.

## Gameplay

Your energy constantly depletes — eat falling pastries to replenish it, and dodge rotten food that drains it. The longer you survive, the faster and harder it gets.

## Features

- **Real-time game loop** with frame-based rendering and collision detection
- **Dynamic difficulty scaling** — speed, spawn rate, and rotten food frequency increase over time
- **Two playable characters** to choose from
- **High score system** that persists between sessions
- **Sound effects and background music**
- **Pause/resume** functionality

## Tech Stack

- **Language:** Java (JDK 11+)
- **Architecture:** Custom game loop, modular OOP across 5 classes
- **Tools:** VS Code, Git

## Project Structure

```
src/main/java/
├── PastryPanic.java     # Main game loop, rendering, input handling
├── FallingItem.java     # Physics and behavior of falling objects
├── ItemType.java        # Item definitions and properties
├── SoundManager.java    # Audio loading and playback
└── Assets.java          # Image asset management
```

## Getting Started

**Requirements:** Java JDK 11 or higher

```bash
git clone https://github.com/mertahmetoglu/PastryPanic.git
cd PastryPanic
```

Open in your IDE (IntelliJ, Eclipse, or VS Code) and run the `main` method in `PastryPanic.java`.

## Controls

| Key | Action |
|-----|--------|
| `←` / `A` | Move left |
| `→` / `D` | Move right |
| `Enter` | Select / Start |
| `Escape` | Pause / Resume |
| `R` | Return to menu |

## Course Context

Developed as part of the Challenge-Based Learning (CBL) course at **Eindhoven University of Technology (TU/e)**, Q1 2025.
