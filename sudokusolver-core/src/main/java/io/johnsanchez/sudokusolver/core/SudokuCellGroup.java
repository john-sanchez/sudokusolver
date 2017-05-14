/**
 * 
 */
package io.johnsanchez.sudokusolver.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
	private final Map<Integer, SudokuCell> locator = new HashMap<>();
	private final List<SudokuCell> contents = new ArrayList<SudokuCell>();
	
	private final SudokuBoard board;
	private final Set<SudokuCell> unsolved = new HashSet<>();
	private final Set<Integer> unsolvedValues = new HashSet<>();
	
	public SudokuCellGroup(SudokuBoard board, LineMode mode, Integer row, Integer col) {
		super();
		this.mode = mode;
		this.row = row;
		this.col = col;
		this.board = board;
		
		for (int i = 1; i<=9; i++) {
			unsolvedValues.add(i);
		}
	}
	
	public void addContent(SudokuCell cell) {
		cell.addObserver(this);
		contents.add(cell);
		if (cell.getValue() != null) {
			locator.put(cell.getValue(), cell);
		} else {
			unsolved.add(cell);
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
			if (sudokuCell.getValue() != null) {
				locator.put(sudokuCell.getValue(), sudokuCell);

				contents.forEach(cell -> {
					cell.removePossibility(sudokuCell.getValue());
					cell.removeFlag(sudokuCell.getValue());
				});
				unsolvedValues.remove(sudokuCell.getValue());
				unsolved.remove(sudokuCell);
			}
		}
	}
	
	public boolean has(int value) {
		return locator.containsKey(value);
	}
	
	public List<SudokuCell> getUnsolved() {
		return new ArrayList<>(unsolved);
	}

	public List<SudokuCell> getContents() {
		return contents;
	}
	
	public List<SudokuCell> getFlagged(int value) {
		List<SudokuCell> res = new ArrayList<>();
		contents.forEach(cell -> {
			if (cell.hasFlag(value)) {
				res.add(cell);
			}
		});
		return res;
	}

	public Set<Integer> getUnsolvedValues() {
		return new HashSet<>(unsolvedValues);
	}
	
	public SudokuCell locate(int value) {
		return locator.get(value);
	}

	public SudokuBoard getBoard() {
		return board;
	}

}
