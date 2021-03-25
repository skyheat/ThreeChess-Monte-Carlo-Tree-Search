# threeChess
An AI framework for playing three player chess, as part of the CITS3001 unit at UWA

# Project

## Three Chess
The project will require you to research, implement and validate artifical intelligence for the board game ThreeChess. ThreeChess is a variation of chess played on a special board between three players, with the colours Blue, Green and Red. Each player takes turns moving their pieces, where the available moves depend on the type of piece. If your piece lands on a square occupied by an opponent's piece, the opponent's piece is removed from the board (captured) and the goal is to take one of your opponents' King. When a King is taken the game ends, and the person who took the King is the winner, the person who lost the King is the loser, and the third player neither wins nor loses.


## Authors 
- skyheat
- Jonathan Neo (21683439)

## Compiling Code

To compile the files, in the root directory use `javac -d bin src/threeChess/\*.java src/threeChess/agents/\*.java`

To run a basic game use `java -cp bin/ threeChess.ThreeChess`

To run a simulation use `.\Run-ThreeChessSimulation.ps1 -numberGames [number of games you wish to run]`

## Folder structure
```
threeChess
|___analysis
│   |   threeChessAnalysis.xlsx     [the raw analysis dataset]
|
└───report
│   │   threeChessReport.pdf        [the final submission report]
|
└───research                        [contains research papers]
| 
|___src                             [contains the src code for threeChess]
|   |   threeChess
|    ___
|       |   agents                  [contains the agents for submission]
|         ___
|            |  Agent21683439.java  [Agent for submission]
|            |  Agent22507198.java  [Agent for submission]
|
|   ProjectREADME.md                [Project README]
|   README.md                       [README on how to run the code and project structure]
|   Run-ThreeChessSimulation.ps1    [Code to run simulation]
|   TeamREADME.md                   [Internal Team README]
```
# ThreeChess-Monte-Carlo-Tree-Search
