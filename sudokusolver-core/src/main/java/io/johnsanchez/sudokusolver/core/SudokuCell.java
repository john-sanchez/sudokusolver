package io.johnsanchez.sudokusolver.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class SudokuCell extends Observable implements Observer {
	
	private Integer value;
	private final int row;
	private final int col;
	private final Set<Integer> flags = new HashSet<Integer>();
	private final Set<SudokuCell> partners = new HashSet<SudokuCell>();
	private SudokuCellGroup rowGroup;
	private SudokuCellGroup colGroup;
	private SudokuCellGroup group;

	public SudokuCell(int row, int col) {
		super();
		this.row = row;
		this.col = col;
	}
	
	public Integer getValue() {
		return value;
	}
	public void setValue(Integer value) {
		this.value = value;
		setAndNotify();
	}
	
	public Set<SudokuCell> getPartners() {
		return partners;
	}
	
	public void addFlag(int flag) {
		flags.add(flag);
//		setAndNotify();
	}
	public void removeFlag(int flag) {
		flags.remove(flag);
//		setAndNotify();
	}
	public void resetFlag(Integer... newFlags) {
		flags.clear();
		flags.addAll(Arrays.asList(newFlags));
	}
	public boolean hasFlag(int flag) {
		return flags.contains(flag);
	}
	public void addPartner(SudokuCell partner) {
		partners.add(partner);
//		setAndNotify();
	}
	public void removePartner(SudokuCell partner) {
		partners.remove(partner);
//		setAndNotify();
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}
	
	private void setAndNotify() {
		setChanged();
		notifyObservers();
	}

	public SudokuCellGroup getRowGroup() {
		return rowGroup;
	}

	public void setRowGroup(SudokuCellGroup rowGroup) {
		this.rowGroup = rowGroup;
	}

	public SudokuCellGroup getColGroup() {
		return colGroup;
	}

	public void setColGroup(SudokuCellGroup colGroup) {
		this.colGroup = colGroup;
	}

	public SudokuCellGroup getGroup() {
		return group;
	}

	public void setGroup(SudokuCellGroup group) {
		this.group = group;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof SudokuCell) {
			SudokuCell sudokuCell = (SudokuCell) o;
			sudokuCell.deleteObserver(this);
			deleteObserver(sudokuCell);
			removeFlag(sudokuCell.getValue());
		}
		
	}

	public Set<Integer> getFlags() {
		return flags;
	}

}
