/**
 * 
 */
package io.johnsanchez.sudokusolver.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * @author user
 *
 */
public class SudokuCellGroup implements Observer {
	
	public static enum LineMode {
		ROW, COLUMN, GROUP
	}
	
	private final LineMode mode;
	private final Integer row;
	private final Integer col;
	private final Map<Integer, SudokuCell> locator = new HashMap<Integer, SudokuCell>();
	private final List<SudokuCell> contents = new ArrayList<SudokuCell>();
	
	public SudokuCellGroup(LineMode mode, Integer row, Integer col) {
		super();
		this.mode = mode;
		this.row = row;
		this.col = col;
	}

//	public List<SudokuCell> getContents() {
//		return contents;
//	}
	
	public void addContent(SudokuCell cell) {
		cell.addObserver(this);
		contents.add(cell);
		if (cell.getValue() != null) {
			locator.put(cell.getValue(), cell);
		}
		if (mode == LineMode.ROW) {
			cell.setRowGroup(this);
		} else if (mode == LineMode.COLUMN) {
			cell.setColGroup(this);
		} else if (mode == LineMode.GROUP) {
			cell.setGroup(this);
		}
	}

	public Integer getRow() {
		return row;
	}
	public Integer getCol() {
		return col;
	}

	public LineMode getMode() {
		return mode;
	}

	public void update(Observable o, Object arg) {
		if (o instanceof SudokuCell) {
			SudokuCell sudokuCell = (SudokuCell) o;
			locator.put(sudokuCell.getValue(), sudokuCell);
		}
	}
	
	public boolean has(int value) {
		return locator.containsKey(value);
	}
	
	public List<SudokuCell> getUnsolved() {
		List<SudokuCell> unsolved = new ArrayList<>();
		contents.forEach(cell -> {
			if (cell.getValue() == null) {
				unsolved.add(cell);
			}
		});
		return unsolved;
	}
	
	public List<SudokuCell> getUnsolved(Set<Integer> rows, Set<Integer> cols, int value) {
		List<SudokuCell> unsolved = new ArrayList<>();
		getUnsolved().forEach(cell -> {
			if (!rows.contains(cell.getRow()) 
					&& !cols.contains(cell.getCol())) {
				if (cell.getPartners().isEmpty() || cell.hasFlag(value)) {
					unsolved.add(cell);
				}
			}
		});
		return unsolved;
	}

	public List<SudokuCell> getContents() {
		return contents;
	}

}
