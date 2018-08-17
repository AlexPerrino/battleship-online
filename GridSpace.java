/**
* A GridSpace knows whether it is being occupied and whether it is out of play or not. A GridSpace
* also keeps track of its corresponding row and column, and features appropriate getter and setter
* methods.
*
* @author Alex Perrino
* @version November 29, 2017
*/
import javax.swing.JButton;
public class GridSpace extends JButton  {
   //Instance Variables
   private boolean isOccupied;
   private boolean isOutOfPlay;
   private int row;
   private int col;
   
   /**
       * Creates a GridSpace with the default values for its boolean variables and the given
       * row and col values.
       * @param row the row of the GridSpace
       * @param col the column of the GridSpace
       */   
   public GridSpace(int row, int col)   {
      super("");
      isOccupied = false;
      isOutOfPlay = false;
      this.row = row;
      this.col = col;
   }
   
   /**
       * @return true if the GridSpace is occupied, false otherwise.
       */   
   public boolean isOccupied()   {
      return isOccupied;
   }
   
   /**
       * @return true if the GridSpace is out of play, false otherwise.
       */  
   public boolean isOutOfPlay()   {
      return isOutOfPlay;
   }
   
   /**
       * @return the row of the GridSpace.
       */  
   public int getRow()  {
      return row;
   }
   
   /**
       * @return the column of the GridSpace.
       */ 
   public int getColumn()  {
      return col;
   }
   
   /**
       * @param dimensions the dimensions of the Grid.
       * @return the single digit index for the GridSpace.
       */ 
   public int getSingleIndex(int dimensions)   {
      return row * dimensions + col;
   }
   
   /**
       * Sets the GridSpace to be out of play.
       */
   public void setOutOfPlay() {
      isOutOfPlay = true;
   }
   
   /**
       * Sets whether the GridSpace is occupied.
       * @param taken whether the GridSpace is occupied or not
       */
   public void setOccupied(boolean taken)  {
      isOccupied = taken;
   }
}