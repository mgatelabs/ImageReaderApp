package com.mgatelabs.imagereaderapp.shared;

/**
 * Created by mmgat on 9/7/2017.
 */

public class MapTransfer {

    private int columns;
    private int rows;
    private int width;
    private int bpp;

    private int startingOffset;
    private int nextRowOffset;
    private int rowSkip;

    private int blockSize;
    private int preSkip;
    private int postSkip;

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getWidth () {
        return width;
    }

    public void setWidth (int width) {
        this.width = width;
    }

    public int getRowSkip () {
        return rowSkip;
    }

    public void setRowSkip (int rowSkip) {
        this.rowSkip = rowSkip;
    }

    public int getBpp () {
        return bpp;
    }

    public void setBpp (int bpp) {
        this.bpp = bpp;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getStartingOffset() {
        return startingOffset;
    }

    public void setStartingOffset(int startingOffset) {
        this.startingOffset = startingOffset;
    }

    public int getNextRowOffset() {
        return nextRowOffset;
    }

    public void setNextRowOffset(int nextRowOffset) {
        this.nextRowOffset = nextRowOffset;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public int getPreSkip() {
        return preSkip;
    }

    public void setPreSkip(int preSkip) {
        this.preSkip = preSkip;
    }

    public int getPostSkip() {
        return postSkip;
    }

    public void setPostSkip(int postSkip) {
        this.postSkip = postSkip;
    }
}
