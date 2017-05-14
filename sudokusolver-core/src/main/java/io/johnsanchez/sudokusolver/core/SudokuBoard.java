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
	
	private final Set<SudokuCell> unsolved = new HashSet<>();
	private final Set<SudokuCell> solved = new HashSet<>();
	
	private final Map<Integer, Set<SudokuCell>> locator = new HashMap<Integer, Set<SudokuCell>>();
	
	private final SudokuCellGroup[] rows = new SudokuCellGroup[9];
	private final SudokuCellGroup[] cols = new SudokuCellGroup[9];
	private final SudokuCellGroup[][] groups = new SudokuCellGroup[3][3];
	
	public SudokuBoard() {
		
		for (int i = 0; i < rows.length; i++) {
			rows[i] = new SudokuCellGroup(this, LineMode.ROW, i, null);
			cols[i] = new SudokuCellGroup(this, LineMode.COLUMN, null, i);
			
			int row = i/3;
			int col = i%3;
			groups[row][col] = new SudokuCellGroup(this, LineMode.GROUP, row, col);
		}
		
		// init board
		for (int i = 0; i < contents.length; i++) {
			for (int j = 0; j < contents[i].length; j++) {
				contents[i][j] = new SudokuCell(i, j);
				rows[i].addContent(contents[i][j]);
				cols[j].addContent(contents[i][j]);
				groups[i/3][j/3].addContent(contents[i][j]);
				unsolved.add(contents[i][j]);
				contents[i][j].addObserver(this);
			}
		}
		
		for (int i = 1; i <= 9; i++) {
			locator.put(i, new HashSet<SudokuCell>());
		}
	}
	
	public void setValue(Integer value, int rowIndex, int colIndex) {
		contents[rowIndex][colIndex].setValue(value);
		contents[rowIndex][colIndex].setGiven(true);
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
			if (sudokuCell.getValue() != null) {
				locator.get(sudokuCell.getValue()).add(sudokuCell);
				
				unsolved.remove(sudokuCell);
				solved.add(sudokuCell);
			}
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
	
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	public void logFlags() {
		for (int i = 0; i < contents.length; i++) {
			for (int j = 0; j < contents[i].length; j++) {
				if (contents[i][j].getValue() == null) {
					if (contents[i][j].getFlags().size() <= 2 && !contents[i][j].getFlags().isEmpty()) {
						boolean spaced = false;
						int k = 0;
						System.out.print(ANSI_RED + "(");
						for (int flag : contents[i][j].getFlags()) {
							System.out.print(flag + (!spaced ? " " : ""));
							spaced = true;
							k++;
						}
						System.out.print((k < 2 ? " " : "") + ") " + ANSI_RESET);
					} else {
						System.out.print(ANSI_RED + "(   ) " + ANSI_RESET);
					}
				} else {
					SudokuCell cell = contents[i][j];
					System.out.print("  " + (!cell.isGiven() ? ANSI_GREEN : "") +  cell.getValue() + (!cell.isGiven() ? ANSI_RESET : "") + "   ");
				}
			}
			System.out.println();
		}
		System.out.println("------------------------ Rem: " + (81-countSolved()));
	}

	public Set<SudokuCell> getUnsolved() {
		return new HashSet<>(unsolved);
	}
}
