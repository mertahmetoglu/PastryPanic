# Pastry Panic

![Pastry Panic Gameplay](https://via.placeholder.com/900x600.png?text=Pastry%20Panic%20Gameplay)

**Pastry Panic** is a fast-paced, 2D arcade game. Players must catch delicious pastries falling from the sky to replenish their constantly depleting energy, while also avoiding rotten food. The game gets progressively harder the longer you survive. How long can you last without panicking?

---

## 🚀 Features

*   **Endless Gameplay:** Try to survive until your energy runs out.
*   **Dynamic Difficulty:** The game speeds up, drops more items, and increases the chance of rotten food appearing as you survive longer.
*   **Character Selection:** Choose between two different characters: Mert Ahmetoglu or Bera Uzun.
*   **High Score:** Your best time is saved, allowing you to challenge yourself in every game.
*   **Sound Effects and Music:** Background music and various sound effects make the game more immersive.
*   **Pause Function:** You can pause and resume the game with the `Escape` key.

---

## 🎮 How to Play

The goal of the game is simple: Survive as long as possible!

*   **Energy:** Your energy constantly depletes over time. Replenish it by eating delicious pastries.
*   **Dangers:** Avoid rotten food! Eating them will significantly reduce your energy.
*   **Score:** Every second you survive is added to your score.

### Kontroller

| Key                  | Action                               |
| -------------------- | ----------------------------------- |
| `Left Arrow` or `A`  | Moves the character to the left.     |
| `Right Arrow` or `D` | Moves the character to the right.    |
| `Enter`              | Makes selections in menus and starts the game. |
| `Escape`             | Pauses or resumes the game.          |
| `R`                  | Returns to the menu from the game over screen. |

---

## 🛠️ Setup and Running

Follow the steps below to run this project on your local machine.

### Requirements

*   Java Development Kit (JDK) 11 veya üstü.

### Running the Game

1.  **Clone the Project:**
    ```sh
    git clone <repository-url>
    cd PastryPanic
    ```

2.  **Running with an IDE (Recommended):**
    *   Open the project in your favorite Java IDE, such as IntelliJ IDEA, Eclipse, or VS Code.
    *   Find the `PastryPanic.java` file under the `src/main/java/` directory.
    *   Run the `main` method.
    *   (For VS Code users, you can use the "Run PastryPanic" option from the "Run and Debug" panel, as the project already includes a `launch.json` configuration.)

---

## 📂 Project Structure

The main source code of the project is located in the `src/main/java/` directory.

*   `PastryPanic.java`: The main class for the game. Manages the entire game loop, rendering, and user input.
*   `ItemType.java`: Defines the types and properties of falling items (pastries, rotten food).
*   `FallingItem.java`: A class representing each individual object falling on the screen.
*   `SoundManager.java`: A helper class for loading and playing sound effects and music.
*   `Assets.java`: A helper class for loading visual assets (images).

All visual and audio assets are located under the `src/main/resources/assets/` directory.