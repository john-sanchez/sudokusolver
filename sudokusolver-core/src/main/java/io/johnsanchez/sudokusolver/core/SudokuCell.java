package io.johnsanchez.sudokusolver.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class SudokuCell extends Observable implements Observer {

	private boolean given;
	private Integer value;
	private final int row;
	private final int col;
	private final Set<Integer> flags = new HashSet<Integer>();
	private final Set<Integer> possibilities = new HashSet<>();
//	private final Set<SudokuCell> partners = new HashSet<SudokuCell>();
	private SudokuCellGroup rowGroup;
	private SudokuCellGroup colGroup;
	private SudokuCellGroup group;
	private SudokuCell lock;

	public SudokuCell(int row, int col) {
		super();
		this.row = row;
		this.col = col;

		for (int i = 1; i <=9; i++) {
			possibilities.add(i);
		}
	}

	public Integer getValue() {
		return value;
	}
	public void setValue(Integer value) {
		if (rowGroup.has(value) || colGroup.has(value) || group.has(value)) {
			throw new RuntimeException("Trying to set " + value + " even though it is already in one of the groups (" + row + ", " + col +") " + rowGroup.has(value) +" " + colGroup.has(value) +" " + group.has(value));
		}

		this.value = value;
		Set<Integer> removedFlags = new HashSet<>(flags);
		flags.clear();
		setAndNotify(removedFlags.isEmpty() ? null : removedFlags);
	}
	public void addFlag(int flag) {
		flags.add(flag);
		//		setAndNotify();
	}
	public void removeFlag(int flag) {
		flags.remove(flag);
	}
	public void removePossibility(int value) {
		possibilities.remove(value);
	}
	public boolean hasPossibility(int value) {
		return possibilities.contains(value);
	}
	public void resetFlag(Integer... newFlags) {
		Set<Integer> removedFlags = new HashSet<>(flags);
		flags.clear();
		flags.addAll(Arrays.asList(newFlags));
		possibilities.clear();
		possibilities.addAll(flags);
		removedFlags.removeIf(val -> flags.contains(val));
		setAndNotify(removedFlags.isEmpty() ? null : removedFlags);
	}
	public boolean hasFlag(int flag) {
		return flags.contains(flag);
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	private void setAndNotify(Object args) {
		setChanged();
		notifyObservers(args);
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
			if (sudokuCell.getValue() != null) {
				sudokuCell.deleteObserver(this);
				deleteObserver(sudokuCell);
				removeFlag(sudokuCell.getValue());

//				if (sudokuCell.getPartners().contains(this) && getFlags().size() == 1) {
//					setValue(getFlags().iterator().next());
//				}
				
				if (this.equals(sudokuCell.getLock()) && getFlags().size() == 1) {
					setValue(getFlags().iterator().next());
				}
			} 
			if (arg instanceof Set<?>) {
				@SuppressWarnings("unchecked")
				Set<Integer> set = (Set<Integer>) arg;
				for (int flag : new HashSet<Integer>(flags)) {
					if (set.contains(flag)) {
						sudokuCell.deleteObserver(this);
						deleteObserver(sudokuCell);
						setValue(flag);
						break;
					}
				}
			}

		}

	}

	public Set<Integer> getFlags() {
		return flags;
	}

	public Set<Integer> getPossibilities() {
		return possibilities;
	}
	
	public List<Integer> getPossibilitiesAsList() {
		return new ArrayList<>(possibilities);
	}

	public SudokuCell getLock() {
		return lock;
	}

	public void setLock(SudokuCell lock) {
		this.lock = lock;
	}

	public boolean isGiven() {
		return given;
	}

	public void setGiven(boolean given) {
		this.given = given;
	}

}
