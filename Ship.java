/**
* A Ship knows its own length and whether it has been sunk and whether it has been set. A Ship also
* keeps a list of all the spaces it has been set to, and has appropriate getter and setter functions.
* 
* @author Alex Perrino
* @version November 29, 2017
*/
public class Ship  {
   //Instance Variables
   private boolean isSunk;
   private boolean isSet;
   private int length;
   private GridSpace[] spaces;
   
   /**
       * Creates a Ship with the default boolean values and the given length.
       * @param length the length of the ship
       */   
   public Ship(int length)   {
      isSunk = false;
      isSet = false;
      this.length = length;
      spaces = new GridSpace[length];
   }
   
   /**
       * @return true if the Ship has been sunk, false otherwise.
       */   
   public boolean isSunk()   {
      if (isSunk == false)
         isSunk = !checkShipInPlay();
      return isSunk;
   }
   
   /**
       * @return true if the Ship has been set, false otherwise.
       */   
   public boolean isSet()  {
      return isSet;
   }
   
   /**
       * Sets the Ship to have been set.
       */ 
   public void setShip() {
      isSet = true;
   }
   
   /**
       * @return the length of the Ship.
       */   
   public int getLength()  {
      return length;
   }
   
   /**
       * Resets the Ship's entire list of GridSpaces.
       */  
   public void clearSpaces()  {
      for (int i = 0; i < length; i++) {
         if (spaces[i] != null)
            spaces[i].setOccupied(false);
      }
   }
   
   /**
       * Sets the specified index of the Ship's GridSpace list to the specified space.
       * @param space the specified GridSpace
       * @param index the specified index of the Ship's GridSpace list
       */   
   public void setSpace(GridSpace space, int index)  {
      spaces[index] = space;
   }
   
   /**
       * @return true if the Ship is currently in play, false otherwise.
       */   
   public boolean checkShipInPlay() {
      for (int i = 0; i < spaces.length; i++) {
         if (!spaces[i].isOutOfPlay())
            return true;
      }
      return false;
   }
   
   /**
       * @param space the GridSpace which is being searched for a Ship.
       * @return true if the Ship is at the given space, false otherwise.
       */   
   public boolean atSpace(GridSpace space)   {
      for (int i = 0; i < spaces.length; i++) {
         if (space.getRow() == spaces[i].getRow() && space.getColumn() == spaces[i].getColumn())
            return true;
      }
      return false;
   }
   
   /**
       * Used for testing/output purposes, and displays all the spaces that the Ship has been set to.
       */   
   private void displaySpaces()   {
      for (int i = 0; i < spaces.length; i++) 
         System.out.println("ROW: " + spaces[i].getRow() + ", COL: " + spaces[i].getColumn());
   }
}