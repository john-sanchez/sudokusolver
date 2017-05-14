/**
 * 
 */
package io.johnsanchez.sudokusolver.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.johnsanchez.sudokusolver.core.SudokuCellGroup.LineMode;

/**
 * @author user
 *
 */
public class SolverCore {
	
	public void solveNext(SudokuBoard board, int value) {
		Set<SudokuCell> cells = board.locate(value);

		if (cells.size() == 9) {
			return;
		}

		List<SudokuCellGroup> unsolvedGroup = new ArrayList<SudokuCellGroup>();
		SudokuCellGroup[][] groups = board.getGroups();
		for (int i = 0; i < groups.length; i++) {
			addToUnsolved(groups[i], value, unsolvedGroup);
		}


		solve(unsolvedGroup, value);
		
		unsolvedGroup.removeIf(group -> group.has(value));
		solve(unsolvedGroup, value);
		
		unsolvedGroup.clear();
		addToUnsolved(board.getRows(), value, unsolvedGroup);
		solve(unsolvedGroup, value);
		
		unsolvedGroup.clear();
		addToUnsolved(board.getCols(), value, unsolvedGroup);
		solve(unsolvedGroup, value);
	}

	private void addToUnsolved(SudokuCellGroup[] groups, int value, List<SudokuCellGroup> unsolvedGroup) {
		for (SudokuCellGroup group : groups) {
			if (!group.has(value)) {
				unsolvedGroup.add(group);
			}
		}
	}

	/**
	 * Tries to solve for a group. If a cell has the only possibility to be a <code>value</code>, then it is the value.
	 * It will also try to reduce the possibility for the following: (2) (1,4) (1,2,4) to 2 (1,4) (1,4) and thereby solving 2 in the process
	 * @param unsolvedGroup
	 * @param value
	 */
	private void solve(List<SudokuCellGroup> unsolvedGroup, int value) {
		// flag and/or solve
		unsolvedGroup.forEach(group -> {
			List<SudokuCell> unsolved = group.getUnsolved();
			unsolved.removeIf(cell -> !cell.hasPossibility(value));
			if (unsolved.size() == 1) {
				SudokuCell cell = unsolved.get(0);
				cell.setValue(value);
			} else if (unsolved.size() == 2) {
				SudokuCell cell1 = unsolved.get(0);
				SudokuCell cell2 = unsolved.get(1);

				Integer commonFlag = getCommonFlag(cell1, cell2, value);

				cell1.addFlag(value);
				cell2.addFlag(value);

				cell1.addObserver(cell2);
				cell2.addObserver(cell1);
				
				if (cell1.getRow() == cell2.getRow()) {
					SudokuCellGroup lineGroup = cell1.getRowGroup();
					removePossibility(lineGroup, value, cell1, cell2);
				}
				
				if (cell1.getCol() == cell2.getCol()) {
					removePossibility(cell1.getColGroup(), value, cell1, cell2);
				}
				
				if (commonFlag != null) {
					cell1.resetFlag(commonFlag, value);
					cell2.resetFlag(commonFlag, value);
					cell1.setLock(cell2);
					cell2.setLock(cell1);
				}
			} else if (unsolved.size() == 3) {
				SudokuCell cell1 = unsolved.get(0);
				SudokuCell cell2 = unsolved.get(1);
				SudokuCell cell3 = unsolved.get(2);
				
				if (cell1.getRow() == cell2.getRow() && cell1.getRow() == cell3.getRow()) {
					removePossibility(cell1.getRowGroup(), value, cell1, cell2, cell3);
				}
				
				if (cell1.getCol() == cell2.getCol() && cell1.getCol() == cell3.getCol()) {
					removePossibility(cell1.getColGroup(), value, cell1, cell2, cell3);
				}
			}
		});
	}
	
	private void reducePossibility(SudokuBoard board) {
		reducePossibility(board.getRows());
		reducePossibility(board.getCols());
	}

	private void reducePossibility(SudokuCellGroup[] groups) {
		for (SudokuCellGroup group : groups) {
			reducePossibility(group);
		}
	}
	
	// provision to reduce the possibility for a group.
	// e.g. (1,2) (1,2) (1,2,4) should be -> (1,2) (1,2) (4)
	private void reducePossibility(SudokuCellGroup group) {
		List<SudokuCell> unsolved = group.getUnsolved();
		if (unsolved.size() <= 2) {
			return;
		}
		Map<Integer, List<SudokuCell>> map = new HashMap<>();
		group.getUnsolvedValues().forEach(value -> map.put(value, new ArrayList<>()));
		for (SudokuCell cell : unsolved) {
			if (cell.getPossibilities().size() != 2) {
				continue;
			}
			cell.getPossibilities().forEach(possibility -> map.get(possibility).add(cell));
		}
		
		Set<Integer> processed = new HashSet<>();
		for (Entry<Integer, List<SudokuCell>> entry : map.entrySet()) {
			if (processed.contains(entry.getKey())) {
				continue;
			}
			
			if (entry.getValue().size() == 2) {
				SudokuCell cell1 = entry.getValue().get(0);
				SudokuCell cell2 = entry.getValue().get(1);
				if (cell1.getPossibilities().containsAll(cell2.getPossibilities())) {
					cell1.getPossibilities().forEach(value -> {
						removePossibility(group, value, cell1, cell2);
						processed.add(value);
					});
				}
				
			}
			
			processed.add(entry.getKey());
		}
	}

	private void removePossibility(SudokuCellGroup lineGroup, int value, SudokuCell... cells) {
		List<SudokuCell> lineUnsolved = new ArrayList<>(lineGroup.getContents());
		lineUnsolved.removeAll(Arrays.asList(cells));
		lineUnsolved.forEach(cell -> cell.removePossibility(value));
	}
	
	public void solveBrutefully(SudokuBoard board) {
		for (SudokuCell cell : board.getUnsolved()) {
			if (cell.getPossibilities().size() == 1) {
				cell.setValue(cell.getPossibilities().iterator().next());
			}
		}
	}
	
	public void solveBrutefully(SudokuCellGroup[] line, LineMode lineMode) {
		for (SudokuCellGroup group : Arrays.asList(line)) {
			group.getUnsolvedValues().forEach(value -> {
				List<SudokuCell> unsolved = group.getUnsolved();
				unsolved.removeIf(cell -> !cell.hasPossibility(value));
				if (unsolved.size() == 1) {
					SudokuCell cell = unsolved.get(0);
					System.out.println("Setting " + value + " (" + cell.getRow() + ", " + cell.getCol() + ")");
					cell.setValue(value);
				}
			});
		}
	}

	public void solveMissing(SudokuBoard board) {
		for (SudokuCellGroup sudokuCellGroup : board.getRows()) {
			solveMissing(sudokuCellGroup);
		}
		for (SudokuCellGroup sudokuCellGroup : board.getCols()) {
			solveMissing(sudokuCellGroup);
		}
		for (int i = 0; i < board.getGroups().length; i++) {
			for (int j = 0; j < board.getGroups()[i].length; j++) {
				solveMissing(board.getGroups()[i][j]);
			}
		}
	}

	public void solveMissing(SudokuCellGroup group) {		
		if (group.getUnsolvedValues().size() == 1 && !group.getUnsolved().isEmpty()) {
			group.getUnsolved().get(0).setValue(group.getUnsolvedValues().iterator().next());
		}
	}

	public Integer getCommonFlag(SudokuCell cell1, SudokuCell cell2, Integer value) {
		for (Integer flag : cell1.getFlags()) {
			if (flag == value) {
				continue;
			}
			if (cell2.hasFlag(flag)) {
				return flag;
			}
		}
		return null;
	}

	public SudokuBoard solve(Integer[][] values) {
		SudokuBoard board = new SudokuBoard();
		board.setValues(values);
		board.logFlags();
		try {
			int solved = board.countSolved();
			int counter = 0;
			while (counter < 5) {			
				int currSolved = board.countSolved();
				solveMissing(board);
				currSolved = logIfMore(board, currSolved, "After missing");
				for (int i = 1; i <=9; i++) {
					solveNext(board, i);
					currSolved = logIfMore(board, currSolved, "After next " + i);
				}
				
				solveBrutefully(board);
				currSolved = logIfMore(board, currSolved, "After brute");
				
				reducePossibility(board);
				
//				solveBrutefully(board.getRows(), LineMode.ROW);
//				currSolved = logIfMore(board, currSolved, "After brute (ROW)");
//				
//				solveBrutefully(board.getCols(), LineMode.COLUMN);
//				currSolved = logIfMore(board, currSolved, "After brute (COL)");
				
				if (solved == currSolved) {
					counter++;
				} else if (currSolved == 81) {
					break;
				} else {
					counter = 0;
				}
				System.out.println("Looping");
				solved = currSolved;
			}
	
			System.out.println("After all");
			board.logFlags();
		} catch (RuntimeException e) {
			System.err.println("ERROR");
			board.logFlags();
			throw e;
		}
		return board;
	}

	private int logIfMore(SudokuBoard board, int currSolved, String message) {
		if (board.countSolved() > currSolved) {
			System.out.println(message);
			board.logFlags();					
		}
		currSolved = board.countSolved();
		return currSolved;
	}

}
