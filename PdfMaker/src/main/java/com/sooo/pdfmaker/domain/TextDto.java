package com.sooo.pdfmaker.domain;
import java.util.HashMap;

public class TextDto extends ContentDto {

   private String content;
   private int row;
   private int col;
   
   public String getContent() {
      return content;
   }
   public void setContent(String content) {
      this.content = content;
   }
   public int getRow() {
      return row;
   }
   public void setRow(int row) {
      this.row = row;
   }
   public int getCol() {
      return col;
   }
   public void setCol(int col) {
      this.col = col;
   }
}