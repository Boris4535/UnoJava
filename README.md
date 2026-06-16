# Java JavaFX Maven

## A fully functional UNO Game written in Java 

This project was developed as a university assignment for our Object-Oriented Programming (OOP) course in Java. 
The main objective of this application was to show a solid understanding of software architecture, OOP principles and teamwork.

Our goal was to fully implement the requested requirements, therefore what you will find is a 
fully functional UNO Game that is easy to play and follows all the official rules:
* **Single Player Mode:** in which you play against a diverse array of AI bot profiles, each with their own personality.
* **Multiple Game Modes:**
    * **Classic Game:** the standard UNO game where a single round determines the winner. The game ends as soon as a player successfully discards all cards from their hand.
    * **Score-Based Game:** A multi-round competitive mode where the overall match continues until a player reaches a specific target score (configured to 500 points by default).
* **Simulation Mode**: lets bots play against themselves while you witness the match.
* **Advanced Save System**: creates multiple save files, that you can choose to load and play in.
* **Detailed Statistics:** tracks match stats (like rounds, turns, challenges, and penalties) and shows global game averages across all matches played.
You can find them at the end of the match.
* **3 bots**: each with different personality traits, such as **stupid**, **clever** and **cheeky**. Which increases or decreases the game difficulty.

***

## Technical Details
* **Java Version:** 26 
* **Maven Version:** 25
* **Graphics Framework:** JavaFX (Version 26)
* **External Libraries:** Gson 
* **Main Class:** Launcher

***

## How to Install
### Prerequisites
Before playing, make sure you have installed in your machibe:
* a **JDK** (from 26 version onwards);
* **JavaFX** (from 25 version onwards);
* An **IDE** of your choice;

### Steps to Run the Game
1. **Clone or Download** the project by installing the source code as a ZIP file and extract it, or clone the repository using Git;
2. **Open** in the project in your IDE;
3. To **run the Game** just click on the run button, as the Launcher is tied to the main.
***
## How to Play

### 1. Normal Game
* Launch the game by clicking on "Run".
* Select **"Nuova partita"** (or load one from "Carica Partita" to resume a match) from the menu.
* Choose the **game mode** (single match or score based), how **many players** you want to play against (and their nature, human or bot?), tick (or untick) whichever **alternative rule** you prefer and start playing!

![Demo preview](https://media2.giphy.com/media/v1.Y2lkPTc5MGI3NjExaWdkemx5dWY5OGlsNWVoeXBydmh3aTY4Z210OTc2cm9oaTNhM2FmMCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/OXSTh4XUiaKdJYQ45r/giphy.gif)

* **Careful!** If a player has played a **+4 wild card** against you, you can fight them in a **challenge** to find out whether they illegally played the magic card against you. 

![Demo preview](https://media3.giphy.com/media/v1.Y2lkPTc5MGI3NjExd3p0NXY4bnlmdmpsZmgwbDk3aXI2em95ajR3ejJrdGJ4bzQybG9veCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/tCmybeNp45TyKw17Re/giphy.gif)

* **Don't forget** to call Uno by clicking on **"Chiama UNO"** or someone might make you notice and pay for it.
* To **exit the match** just press "**esc**" on your keyboard. You can choose save the match to 
resume the game later, or not!
* To view the **statistics** play the match until the **end**.

![Demo preview](https://media1.giphy.com/media/v1.Y2lkPTc5MGI3NjExMGV2c2x3amF2NTNxYWNha3VsNXV4Z3U3OXp6dXRkNDc4Ym91NjcwciZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/B76jtaQle0sulgXlJw/giphy.gif)


### 2. Simulation Mode
* From the second menu, tick the **"Simulation Mode"** box.
* Watch the bots play against each other automatically.
* At the end you will see the **statistics**.

### 3. Save and Load a Game
* **To Save:** Press "esc" on your keyboard,and choose to save the match.

![Demo preview](https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExaHhnOGZiNGhrYjg3NjR3bDlpbzUyamFiYXlieXhldnA3MXdjZG84diZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/Acj4SH0TPhw03In7xF/giphy.gif)

* **To Load:** From the main menu, click **"Carica Partita"** and select your saved match from the list.

![Demo preview](https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExYWN1MHRtZzBnOHM1NGVlYTdwaDhuYWc4Ym83aXE4bnB0N2g1MTVsZyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/ax90sFuseo6Ml3uTW7/giphy.gif)

***
## Coming features
What we would to add in the future!
* Sounds effects, costume cards and **whatever our teacher wants :)**
