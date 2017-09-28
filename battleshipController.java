package battleship;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

public class battleshipController
{
    private byte color = 0;
    private boolean player1 = true, //true if player1 turn
            vertical = false, //true if ship is vertical
            horizontal = false; //true if ship is horizontal
    private Button [][] leftButtons = new Button[10][10]; //array for left buttons
    private Button [][] rightButtons = new Button[10][10]; //array for right buttons
    private enum State {START, GAME, END}
    private State state = State.START;
    private int shipSize = 0, //current ship size
            shipNr = 0; //cirrent ship nr
    private int p1Points = 0, p2Points = 0, maxPoints = 0;
    private int[][] p1Shots = new int [10][10]; //array of p1 shots 1 - hit, 2 - miss
    private int[][] p2Shots = new int [10][10]; //array of p2 shots 1 - hit, 2 - miss
    private int[] maxShipSize = {3, 3, 4, 4, 5};
    //list of ships coordinates
    private ArrayList<Integer[][]> p1Ships= new ArrayList<Integer[][]>();
    private ArrayList<Integer[][]> p2Ships= new ArrayList<Integer[][]>();

    //method is called automatically when the FXML is loaded
    public void initialize()
    {
        //change style of non-board buttons
        changeButtonColor(nextPlayer, "restButtons");
        changeButtonColor(nextShip, "restButtons");
        info.setText("Player 1: Set your ship nr " + (shipNr+1) + " - size " + maxShipSize[shipNr]);
        nextPlayer.setVisible(false); //hide next player button
        nextShip.setVisible(false); //hide next ship button

        //add array list for p1 ships
        p1Ships.add(new Integer[maxShipSize[0]][2]);
        p1Ships.add(new Integer[maxShipSize[1]][2]);
        p1Ships.add(new Integer[maxShipSize[2]][2]);
        p1Ships.add(new Integer[maxShipSize[3]][2]);
        p1Ships.add(new Integer[maxShipSize[4]][2]);

        //add array list for p2 ships
        p2Ships.add(new Integer[maxShipSize[0]][2]);
        p2Ships.add(new Integer[maxShipSize[1]][2]);
        p2Ships.add(new Integer[maxShipSize[2]][2]);
        p2Ships.add(new Integer[maxShipSize[3]][2]);
        p2Ships.add(new Integer[maxShipSize[4]][2]);

        //fill button arrays with buttons
        for(int i = 0; i < leftButtons.length; i++)
        {
            for (int j = 0; j <leftButtons[0].length; j++ )
            {
                leftButtons[i][j] = (Button) getNodeFromGridPane(leftGrid, i, j);
                rightButtons[i][j] = (Button) getNodeFromGridPane(rightGrid, i, j);
                rightButtons[i][j].setDisable(true);
            }
        }

        for (int i = 0; i < maxShipSize.length; i++) //calculate max points
            maxPoints += maxShipSize[i];
    }

    @FXML
    private GridPane rightGrid;

    @FXML
    private GridPane leftGrid;

    @FXML
    private Button nextPlayer;

    @FXML
    private Button nextShip;

    @FXML
    private Label info;

    @FXML
    void nextShipPressed(ActionEvent event)
    {
        nextShip.setVisible(false);
        if(shipNr < maxShipSize.length)
        {
            shipNr++;
            shipSize = 0;
            if (player1) //set message for player what ship he's building
                info.setText("Player 1: Set your ship nr " + (shipNr+1) + " - size " + maxShipSize[shipNr]);
            else
                info.setText("Player 2: Set your ship nr " + (shipNr+1) + " - size " + maxShipSize[shipNr]);
        }
    }

    @FXML
    //function for board button handling
    void boardButtonPressed(ActionEvent event)
    {
        Button button = (Button) event.getSource(); //get the buton that is clicked
        switch (state) //chack it what stage is game
        {
            case START: //setup stage
                changeButtonColor(button, "greenButton");
                if(checkShip(button)) //check if ship piece that user clicked is ok
                {
                    button.setDisable(true); //disable button
                }
                else
                    changeButtonColor(button, "Button");
                break;
            case GAME: //game stage
                shotFired(button);
                break;
            case END: //end stage
                break;
        }
    }

    @FXML //next player button is clicked
    void nextPlayerPressed(ActionEvent event)
    {
        //if this is setup stage
        if(state == State.START)
        {
            if (player1)
            {
                //reset variables for 2nd player
                shipNr = 0;
                shipSize = 0;
                nextPlayer.setVisible(false);//hide button
                clearBoard(leftGrid); //clear board
                enableBoard(leftGrid, true); //enable left buttons
                player1 = !player1;
                info.setText("Player 2: Set your ship nr " + (shipNr+1) + " - size " + maxShipSize[shipNr]);
            } else //set game stage if 2nd player finished setup
            {
                state = State.GAME;
                player1 = !player1;
                nextPlayer.setVisible(false);//hide button
                fillShotsBoard(); //fill right board with fired shots
                clearBoard(leftGrid);
                fillShipsBoard(); //fill left board with player's ship
                enableBoard(rightGrid, true); //enable right buttons
                enableBoard(leftGrid, false); //enable right buttons
                info.setText("Player1 turn. Your score: " + p1Points + "/" + maxPoints);
                /*for (int i = 0; i < 5; i++)
                {
                    System.out.println("p1: " + Arrays.deepToString(p1Ships.get(i)));
                    System.out.println("p2: " + Arrays.deepToString(p2Ships.get(i)));
                }*/
            }
        }else if(state == State.GAME)
        {
            player1 = !player1;
            nextPlayer.setVisible(false);//hide button
            clearBoard(rightGrid); //clear board
            clearBoard(leftGrid); //clear board
            enableBoard(rightGrid, true);
            fillShotsBoard(); //fill right board with fired shots
            fillShipsBoard(); //fill left board with player's ship
            if (player1)
                info.setText("Player1 turn. Your score: " + p1Points + "/" + maxPoints);
            else
                info.setText("Player2 turn. Your score: " + p2Points + "/" + maxPoints);
        }
    }

    //get button from GridPane for specific column and row
    private Node getNodeFromGridPane(GridPane gridPane, int col, int row)
    {
        for (Node node : gridPane.getChildren()) {
            Integer colOf = GridPane.getColumnIndex(node);
            Integer rowOf = GridPane.getRowIndex(node);
            if (colOf == null)
                colOf = 0;
            if (rowOf == null)
                rowOf = 0;

            if (colOf == col && rowOf == row) {
                return node;

            }
        }
        return null;
    }

    private void changeButtonColor(Button button, String style)
    {
        //In this way you're sure you have no styles applied to your object button
        button.getStyleClass().removeAll( "redButton", "greenButton", "whiteButton");
        //then you specify the class you would give to the button
        button.getStyleClass().add(style);
    }

    //check if next piece of ship is ok
    private boolean checkShip(Button button)
    {
        Integer[][] temp2 = new Integer[0][];
        if (player1) //check if player1 turn
            temp2 = p1Ships.get(shipNr); //get ship's coordinates
        else
            temp2 = p2Ships.get(shipNr); //get ship's coordinates
        if(shipSize < maxShipSize[shipNr])//check if ship is finished
        {
            if(shipSize != 0)
            {

                if(checkIfYouShallPass(rightGrid.getColumnIndex(button), rightGrid.getRowIndex(button))) //chack if ship's piece is valid
                {
                    //get piece coordinates
                    temp2[shipSize][0] = rightGrid.getColumnIndex(button); //get coordinates of new piece
                    temp2[shipSize][1] = rightGrid.getRowIndex(button); //get coordinates of new piece
                    if (player1)
                    {
                        p1Ships.set(shipNr,temp2); //add new piece to array of ships ship
                    }else
                    {
                        p2Ships.set(shipNr,temp2); //add new piece to array of ships ship
                    }
                    shipSize++;
                    if(shipSize == maxShipSize[shipNr]) //check if this is last piecie of ship
                    {
                        horizontal = false;
                        vertical = false;
                        nextShip.setVisible(true); //set visible next ship button
                        disableAroundShip();//disable buttons around new finished ship
                        if(shipNr == maxShipSize.length-1)
                        {
                            nextShip.setVisible(false);
                            nextPlayer.setVisible(true);
                        }
                    }
                    return true;
                } else
                    return false;
            }else //it's 1st piece of ship
            {
                Integer[][] temp = new Integer[maxShipSize[shipNr]][2];
                //get piece coordinates
                temp[0][0] = rightGrid.getColumnIndex(button); //get coordinates of new piece
                temp[0][1] = rightGrid.getRowIndex(button); //get coordinates of new piece
                if (player1)
                {
                    p1Ships.set(shipNr,temp); //add new piece to array of ships ship
                }else
                {
                    p2Ships.set(shipNr,temp); //add new piece to array of ships ship
                }
                shipSize++;
                return true;
            }
        }else
            return false;
    }

    //check if ships pieces are ok
    private boolean checkIfYouShallPass(int col, int row)
    {
        Integer[][] temp2;
        if (player1)
        {
            temp2 = p1Ships.get(shipNr); //get ship's coordinates
        } else
        {
            temp2 = p2Ships.get(shipNr); //get ship's coordinates
        }
        int deltacol = 0;
        int deltarow = 0;
        if (shipSize != 0) //calculate difference between actual position and previous position
        {
            deltacol = temp2[shipSize-1][0] - col;
            deltarow = temp2[shipSize-1][1] - row;

        }

        if(shipSize == 1) //if this is 2nd pieces check if ship is vertical or horizontal and pieces are ok
        {
            if(deltacol == 0 && Math.abs(deltarow) <= 1) //if tru ship is vertical
            {
                vertical = true;
                return true;
            } else if (deltarow == 0 && Math.abs(deltacol) <= 1) //if tru ship is horizontal
            {
                horizontal = true;
                return true;
            } else
            {
                return false;
            }
        }

        if (vertical) //if ship is vertical check if piece is ok
        {
            if(deltacol == 0 && Math.abs(deltarow) <= 1)
                return true;
            else
            {
                return false;
            }
        }else if (horizontal) //if ship is horizontal check if piece is ok
        {
            if(deltarow == 0 && Math.abs(deltacol) <= 1)
                return true;
            else
            {
                return false;
            }
        }else
        {
            return true;
        }
    }

    //method for clearing board
    private void clearBoard(GridPane gridPane)
    {
        for (Node node : gridPane.getChildren())
            changeButtonColor((Button)node, "Button");
    }


    //enable buttons in grind if boolean false disable buttons
    private void enableBoard(GridPane gridPane, boolean enable)
    {
        for (Node node : gridPane.getChildren())
        {
            if(enable)
                node.setDisable(false); //enable button
            else
                node.setDisable(true); //disable button
        }
    }

    //fill right board with fired shots
    private void fillShotsBoard()
    {
        //iterate shots array and add it to board
        for (int i = 0; i < p1Shots.length; i++)
        {
            for (int j = 0; j < p1Shots[0].length; j++)
            {
                Button button = (Button) getNodeFromGridPane(rightGrid, i, j);
                if(player1)
                {
                    if(p1Shots[i][j] == 1)// 1 - hit
                    {
                        changeButtonColor(button, "redButton");
                        button.setDisable(true);
                    } else if (p1Shots[i][j] == 2) // 2 - miss
                    {
                        changeButtonColor(button, "whiteButton");
                        button.setDisable(true);
                    } else //no shot
                        changeButtonColor(button, "Button");
                }else
                {
                    if(p2Shots[i][j] == 1)// 1 - hit
                    {
                        changeButtonColor(button, "redButton");
                        button.setDisable(true);
                    } else if (p2Shots[i][j] == 2) // 2 - miss
                    {
                        changeButtonColor(button, "whiteButton");
                        button.setDisable(true);
                    } else //no shot
                        changeButtonColor(button, "Button");
                }
            }
        }
    }

    //fill left board with player's ship
    private void fillShipsBoard()
    {
        //iterate player's ships coordinates and add it to board
        for(int i = 0; i < maxShipSize.length; i++)
        {
            Integer[][] temp = new Integer[maxShipSize[i]][2];
            if (player1)
            {
                temp = p1Ships.get(i); //get ship's coordinates
            }else
            {
                temp = p2Ships.get(i); //get ship's coordinates
            }
            for (int j = 0; j < temp.length; j++)
            {
                Button button = (Button) getNodeFromGridPane(leftGrid, temp[j][0], temp[j][1]); // get button corresponding to ship's coordinates
                changeButtonColor(button, "greenButton");
            }
        }

        //fill board with opponent's shots
        for (int i = 0; i < p1Shots.length; i++)
        {
            for (int j = 0; j < p1Shots[0].length; j++)
            {
                int shot = 0;
                if (player1)
                    shot = p2Shots[i][j]; // get opponent's shots
                else
                    shot = p1Shots[i][j]; // get opponent's shots
                if(shot == 1) //hit
                {
                    Button button = (Button) getNodeFromGridPane(leftGrid, i, j); //get button located at corresponding coordinates
                    changeButtonColor(button, "redButton");
                    button.setDisable(true);
                }else if (shot == 2) //miss
                {
                    Button button = (Button) getNodeFromGridPane(leftGrid, i, j); //get button located at corresponding coordinates
                    changeButtonColor(button, "whiteButton");
                    button.setDisable(true);
                }
            }
        }
    }

    //function for shots handling
    private void shotFired(Button button)
    {
        boolean shipIsHit = false; //true if shot is hit
        //get coordinates of clicked button
        int col = GridPane.getColumnIndex(button);
        int row = GridPane.getRowIndex(button);

        for (int i = 0; i < maxShipSize.length; i++) //iterate every opponent'ss ship and check if it's hit
        {
            Integer[][] temp = new Integer [maxShipSize[i]][2];
            if (player1)
            {
                temp = p2Ships.get(i); //get opponent's ships
            } else
            {
                temp = p1Ships.get(i); //get opponent's ships
            }

            //iterate opponent's ships and check if player hit them
            for (int j = 0; j < temp.length; j++)
            {
                if (col == temp[j][0] && row == temp[j][1])
                {
                    shipIsHit = true;
                    changeButtonColor(button, "redButton");
                    info.setText(info.getText().substring(0,7) + " Hit!");
                    if (player1)
                    {
                        p1Points++;
                        p1Shots[col][row] = 1; //add hit to array of shots
                    } else
                    {
                        p2Points++;
                        p2Shots[col][row] = 1; //add hit to array of shots
                    }
                    break;
                }
            }
            if (shipIsHit) //break loop if ship is hit
                break;
        }

        if (!shipIsHit) //chack if shot missed
        {
            if (player1)
                p1Shots[col][row] = 2; //add miss to array of shots
            else
                p2Shots[col][row] = 2; //add miss to array of shots

            changeButtonColor(button, "whiteButton");
            info.setText(info.getText().substring(0,7) + " Missed!");
        }

        //check if end of game
        if (player1)
        {
            if (p1Points == maxPoints)
            {
                state = State.END; //set state of game to end
                info.setText("Player1 WIN");
            }
        }else
        {
            if (p2Points == maxPoints)
            {
                state = State.END; //set state of game to end
                info.setText("Player2 WIN");
            }
        }
        enableBoard(rightGrid, false);// disable right board

        //if game is still on show next player button
        if (state == State.GAME)
        {
            nextPlayer.setVisible(true);
        }
    }

    //disable buttons around new finished ship
    private void disableAroundShip()
    {
        Integer [][] temp = new Integer[maxShipSize[shipNr]][];
        if (player1)
            temp = p1Ships.get(shipNr); //get ships coordinates
        else
            temp = p2Ships.get(shipNr);
        for (int i = 0; i < temp.length ; i++)// iterate for each coordinate
        {
            int col = temp[i][0];
            int row = temp[i][1];
            if(col < leftButtons.length - 1 && col > 0)
            {
                if(row < leftButtons[0].length - 1 && row > 0) //all buttons around are present
                {
                    //disable buttons around selected ship's piece
                    getNodeFromGridPane(leftGrid, col-1, row -1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col-1, row).setDisable(true);
                    getNodeFromGridPane(leftGrid, col-1, row +1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col, row -1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col, row +1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col +1, row +1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col +1, row).setDisable(true);
                    getNodeFromGridPane(leftGrid, col +1, row -1).setDisable(true);
                }else if(row == 0) //piece is in top row
                {
                    getNodeFromGridPane(leftGrid, col-1, row).setDisable(true);
                    getNodeFromGridPane(leftGrid, col-1, row +1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col, row +1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col +1, row +1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col +1, row).setDisable(true);
                }else //piece is in bottom row
                {
                    getNodeFromGridPane(leftGrid, col-1, row -1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col-1, row).setDisable(true);
                    getNodeFromGridPane(leftGrid, col, row -1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col +1, row).setDisable(true);
                    getNodeFromGridPane(leftGrid, col +1, row -1).setDisable(true);
                }
            }else if(col == 0) //piece is in left column
            {
                if (row < leftButtons[0].length - 1 && row > 0)
                {
                    getNodeFromGridPane(leftGrid, col, row -1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col, row +1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col +1, row +1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col +1, row).setDisable(true);
                    getNodeFromGridPane(leftGrid, col +1, row -1).setDisable(true);
                }else if(row == 0) //piece is in top row
                {
                    getNodeFromGridPane(leftGrid, col, row +1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col +1, row +1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col +1, row).setDisable(true);
                }else //piece is in bottom row
                {
                    getNodeFromGridPane(leftGrid, col, row -1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col +1, row).setDisable(true);
                    getNodeFromGridPane(leftGrid, col +1, row -1).setDisable(true);
                }
            }else //piece is in right column
            {
                if (row < leftButtons[0].length - 1 && row > 0)
                {
                    getNodeFromGridPane(leftGrid, col-1, row -1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col-1, row).setDisable(true);
                    getNodeFromGridPane(leftGrid, col-1, row +1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col, row -1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col, row +1).setDisable(true);
                }else if(row == 0) //piece is in top row
                {
                    getNodeFromGridPane(leftGrid, col-1, row).setDisable(true);
                    getNodeFromGridPane(leftGrid, col-1, row +1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col, row +1).setDisable(true);
                }else //piece is in bottom row
                {
                    getNodeFromGridPane(leftGrid, col-1, row -1).setDisable(true);
                    getNodeFromGridPane(leftGrid, col-1, row).setDisable(true);
                    getNodeFromGridPane(leftGrid, col, row -1).setDisable(true);
                }
            }
        }
    }
}
