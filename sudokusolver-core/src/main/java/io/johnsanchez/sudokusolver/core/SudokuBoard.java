package io.johnsanchez.sudokusolver.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import io.johnsanchez.sudokusolver.core.SudokuCellGroup.LineMode;

public class SudokuBoard implements Observer {

	/*
	 * Solving patterns:
	 * 1. not in the same row or column
	 * 2. not in the same group
	 * 3. same row for possible cell
	 * 4. 
	 * 
	 */
	private final SudokuCell[][] contents = new SudokuCell[9][9];
	private final Map<Integer, Set<SudokuCell>> locator = new HashMap<Integer, Set<SudokuCell>>();
	
	private final SudokuCellGroup[] rows = new SudokuCellGroup[9];
	private final SudokuCellGroup[] cols = new SudokuCellGroup[9];
	private final SudokuCellGroup[][] groups = new SudokuCellGroup[3][3];
	
	public SudokuBoard() {
		
		for (int i = 0; i < rows.length; i++) {
			rows[i] = new SudokuCellGroup(LineMode.ROW, i, null);
			cols[i] = new SudokuCellGroup(LineMode.COLUMN, null, i);
			
			int row = i/3;
			int col = i%3;
			groups[row][col] = new SudokuCellGroup(LineMode.GROUP, row, col);
		}
		
		// init board
		for (int i = 0; i < contents.length; i++) {
			for (int j = 0; j < contents[i].length; j++) {
				contents[i][j] = new SudokuCell(i, j);
				rows[i].addContent(contents[i][j]);
				cols[j].addContent(contents[i][j]);
				groups[i/3][j/3].addContent(contents[i][j]);
				contents[i][j].addObserver(this);
			}
		}
		
		for (int i = 1; i <= 9; i++) {
			locator.put(i, new HashSet<SudokuCell>());
		}
	}
	
	public void setValue(Integer value, int rowIndex, int colIndex) {
		contents[rowIndex][colIndex].setValue(value);
	}
	
	public void setValues(Integer[][] values) {
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values[i].length; j++) {
				if (values[i][j] != null) {
					setValue(values[i][j], i, j);
				}
			}
		}
	}

	public void update(Observable o, Object arg) {
		if (o instanceof SudokuCell) {
			SudokuCell sudokuCell = (SudokuCell) o;
			locator.get(sudokuCell.getValue()).add(sudokuCell);
		}
	}
	
	public Set<SudokuCell> locate(int value) {
		return locator.get(value);
	}

	public SudokuCellGroup[][] getGroups() {
		return groups;
	}

	public SudokuCellGroup[] getRows() {
		return rows;
	}

	public SudokuCellGroup[] getCols() {
		return cols;
	}
	
	public int countSolved() {
		int counter = 0;
		for (Set<SudokuCell> cells : locator.values()) {
			counter += cells.size();
		}
		return counter;
	}
	
	public Integer[][] getContentValues() {
		Integer[][] out = new Integer[contents.length][];
		for (int i = 0; i < contents.length; i++) {
			out[i] = new Integer[contents[i].length];
			for (int j = 0; j < contents[i].length; j++) { 
				out[i][j] = contents[i][j].getValue();
			}
		}
		return out;
	}
	
	public void log() {
		Integer[][] content = getContentValues();
		for (int i = 0; i < content.length; i++) {
			for (int j = 0; j < content[i].length; j++) {
				System.out.print((content[i][j] == null ? " " : content[i][j]) + " ");
			}
			System.out.println();
		}
		System.out.println("------------------------ Rem: " + (81-countSolved()));
	}
}
