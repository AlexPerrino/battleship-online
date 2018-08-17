/**
* A Grid stores a list of GridSpaces, and whether the Ships in the Grid have been set or not.
* The Class has appropriate getter and setter functions.
* 
* @author Alex Perrino
* @version November 29, 2017
*/
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridLayout;
import java.awt.*;
public class Grid extends JPanel implements BattleShippable  {
   //Instance Variables
   private GridSpace[][] grid;
   private boolean shipsSet;
   //Instance variables used for setting Ships
   private Ship currentShip;
   private int shipIndex;
   private GridSpace currentSpace1;
   private GridSpace currentSpace2;
   private JLabel shipStatus;
   
   /**
       * Creates a Grid with the default values, that is of the specified dimensions.
       */   
   public Grid()   {
      grid = new GridSpace[GRID_DIMENSION][GRID_DIMENSION];
      shipsSet = false;
      setLayout(new GridLayout(GRID_DIMENSION, GRID_DIMENSION));
      for (int i = 0; i < GRID_DIMENSION; i++)  {
         for (int j = 0; j < GRID_DIMENSION; j++)  {
            grid[i][j] = new GridSpace(i, j);
            GridSpace tmp = getSpace(i, j);
            tmp.setPreferredSize(new Dimension(50, 45));
            grid[i][j].addActionListener(e -> onClick(tmp));
            add(grid[i][j]);
         }
      }
      currentShip = null;
      shipIndex = 0;
      currentSpace1 = null;
      currentSpace2 = null;
   }
   
   /**
       * Creates a Grid with the default values, that is of the specified dimensions with a
       * give JLabel from the client for error reporting.
       * @param shipStatus a JLabel from the client for error reporting.
       */
   public Grid(JLabel shipStatus)   {
      this();
      this.shipStatus = shipStatus;
   }
   
   /**
       * Returns the GridSpace with the given row and col values.
       * @param row the row of the GridSpace
       * @param col the column of the GridSpace
       * @return the corresponding GridSpace
       */   
   public GridSpace getSpace(int row, int col)  {
      return grid[row][col];
   }

   /**
       * Returns the GridSpace with the index.
       * @param space the single digit index of the Grid matrix
       * @return the corresponding GridSpace
       */  
   public GridSpace getSpace(int space)  {
      int row = space / GRID_DIMENSION;
      int col = space % GRID_DIMENSION;
      return grid[row][col];
   }
   
   /**
       * Sets the Grid to have its ships declared as set.
       */
   public void shipsAreSet()  {
      shipsSet = true;
   }
   
   /**
       * Sets the Grid's currentShip to newShip.
       * @param newShip the new currentShip
       */  
   public void setCurrentShip(Ship newShip)  {
      currentShip = newShip;
   }
   
   /**
       * Sets the Grid's shipIndex to newIndex.
       * @param newIndex the new shipIndex
       */  
   public void setShipIndex(int newIndex)  {
      shipIndex = newIndex;
   }
   
   /**
       * Sets the next unset (null) currentSpace to this GridSpace. If the next is currentSpace1, then it will be set
       * to space. If the next is currentSpace2, then it will be set to space. If both of the currentSpaces are
       * set it will attempt to set a ship between the two by calling the setShip function. No matter the outcome
       * of the setShip function call it will set both of the currentSpaces back to null.
       * @param space the given GridSpace
       */  
   public void setSpace(GridSpace space)  {
      if (currentSpace1 == null) 
         currentSpace1 = space;
      else
         currentSpace2 = space;
      if (currentSpace1 != null && currentSpace2 != null)   {
         if (currentShip.isSet())
            shipStatus.setText(getShipString(shipIndex) + 
               " has already been set!");   
         else if (setShip())
            shipStatus.setText(getShipString(shipIndex) + " successfully set!");
         else
            shipStatus.setText(getShipString(shipIndex) + 
               " unsuccessfully set, try again!");
         currentSpace1 = null;
         currentSpace2 = null;
      }
   }
   
   /**
       * Function call that is set to the GridSpaces' actionListener. If the shipsSet == false then
       * it will call the setSpace function with the current space. If the ships have been set then
       * the function will no longer do anything.
       * @param space the given GridSpace
       */  
   public void onClick(GridSpace space)   {
      if (!shipsSet)
         setSpace(space);
   }
   
   /**
       * Attempts to set a Ship's location to the grid with currentShip, currentSpace1, and currentSpace2.
       * It will first attempt to check if the two spaces are on the same row. If they are on the same row,
       * it checks if the difference in their columns is equal to the length of the currentShip - 1. If that is true
       * then it will set the appropriate GridSpaces to be occupied and each GridSpace to the list of GridSpaces 
       * in the currentShip. If they are not on the same row it does the opposite with the place of row and column
       * reversed.
       * @return true if the ship was successfully set, false otherwise
       */  
   public boolean setShip()   {
      if (currentSpace1.getSingleIndex(GRID_DIMENSION) > currentSpace2.getSingleIndex(GRID_DIMENSION))
         swapCurrentSpaces();
      if (currentSpace1.getRow() == currentSpace2.getRow()) {
         if (Math.abs(currentSpace1.getColumn() - currentSpace2.getColumn()) == currentShip.getLength() - 1)   {
            int col = currentSpace1.getColumn();
            int row = currentSpace1.getRow();
            int spaceIndex = 0;
            for (int i = col; i <= currentSpace2.getColumn(); i++) {
               GridSpace currentSpace = getSpace(row, i);
               if (currentSpace.isOccupied())   {
                  currentShip.clearSpaces();
                  return false;
               }
               currentSpace.setOccupied(true);
               currentShip.setSpace(currentSpace, spaceIndex++);
            }
            currentShip.setShip();
            updateGridDisplay('O');
            return true;
         }
         else
            return false;
      }
      else if (currentSpace1.getColumn() == currentSpace2.getColumn()) {
         if (Math.abs(currentSpace1.getRow() - currentSpace2.getRow()) == currentShip.getLength() - 1)   {
            int row = currentSpace1.getRow();
            int col = currentSpace1.getColumn();
            int spaceIndex = 0;
            GridSpace currentSpace;
            for (int i = row; i <= currentSpace2.getRow(); i++) {
               currentSpace = getSpace(i, col);
               if (currentSpace.isOccupied())   {
                  currentShip.clearSpaces();
                  return false;
               }
               currentSpace.setOccupied(true);
               currentShip.setSpace(currentSpace, spaceIndex++);
            }
            currentShip.setShip();
            updateGridDisplay('O');
            return true;
         }
         else
            return false;
      }
      return false;
   }
   
   /**
       * Updates the occupied GridSpaces of the Grid with the given symbol. Any occupied space
       * will be set to contain the given symbol in the default font (Black, Times New Roman, 20pt).
       * @param symbol the symbol to update any occupied spaces with
       */  
   public void updateGridDisplay(char symbol)  {
      for (int i = 0; i < GRID_DIMENSION; i++)  {
         for (int j = 0; j < GRID_DIMENSION; j++)  {
            GridSpace space = getSpace(i, j);
            if (space.isOccupied())  {
               space.setForeground(Color.BLACK);
               space.setFont(new Font("Times New Roman", Font.PLAIN, 20));
               space.setText("" + symbol);
            }
            else
               space.setText("");
         }
      }
   }
   
   /**
       * Swaps currentSpace1 with currentSpace2 if necessary.
       */  
   public void swapCurrentSpaces()   {
      GridSpace tempSpace = currentSpace1;
      currentSpace1 = currentSpace2;
      currentSpace2 = tempSpace;
   }
   
   /**
       * Updates the given GridSpaces with a red 'X' if the shot was a hit, and a black 'X' if it missed.
       * @param space the GridSpace which was fired at
       * @param success whether the shot was a hit or not
       */  
   public void updateSpace(GridSpace space, boolean success)  {
      if (success)
         space.setForeground(Color.RED);
      else
         space.setForeground(Color.BLACK);
      space.setFont(new Font("Times New Roman", Font.PLAIN, 20));
      space.setText("X");
   }
}