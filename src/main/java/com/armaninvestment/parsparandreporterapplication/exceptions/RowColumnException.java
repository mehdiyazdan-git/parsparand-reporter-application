package com.armaninvestment.parsparandreporterapplication.exceptions;

public class RowColumnException extends RuntimeException {
    private final int rowNum;
    private final int colNum;

    public RowColumnException(int rowNum, int colNum, String message, Throwable cause) {
        super("Error in row " + rowNum + ", column " + colNum + ": " + message, cause);
        this.rowNum = rowNum;
        this.colNum = colNum;
    }

    public int getRowNum() {
        return rowNum;
    }

    public int getColNum() {
        return colNum;
    }
}
