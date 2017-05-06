/**
 * 
 */
package io.johnsanchez.sudokusolver.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author user
 *
 */
public class SolverCore {
	
	private static Set<Integer> create1To9() {
		Set<Integer> set = new HashSet<>();
		for (int i = 1; i <=9; i++) {
			set.add(i);
		}
		return set;
	}
	
	public void solveNext(SudokuBoard board, int value) {
		Set<SudokuCell> cells = board.locate(value);
		
		if (value == 3 && board.countSolved() >= 81-6) {
			System.out.println();
		}
		
		if (cells.size() == 9) {
			return;
		}
		
		Set<Integer> rows = new HashSet<>();
		Set<Integer> cols = new HashSet<>();
		cells.forEach(cell -> {
			rows.add(cell.getRow());
			cols.add(cell.getCol());
		});
		
		List<SudokuCellGroup> unsolvedGroup = new ArrayList<SudokuCellGroup>();
		SudokuCellGroup[][] groups = board.getGroups();
		for (int i = 0; i < groups.length; i++) {
			for (int j = 0; j < groups.length; j++) {
				SudokuCellGroup group = groups[i][j];
				if (!group.has(value)) {
					unsolvedGroup.add(group);
				}
			}
		}
		unsolvedGroup.forEach(group -> {
			List<SudokuCell> unsolved = group.getUnsolved(rows, cols, value);
			if (unsolved.size() == 1) {
				unsolved.get(0).setValue(value);
			} else if (unsolved.size() == 2) {
				SudokuCell cell1 = unsolved.get(0);
				SudokuCell cell2 = unsolved.get(1);
				
				Integer commonFlag = getCommonFlag(cell1, cell2, value);
				
				cell1.addFlag(value);
				cell2.addFlag(value);
				
				cell1.addObserver(cell2);
				cell2.addObserver(cell1);
				
				if (commonFlag != null) {
					cell1.addPartner(cell2);
					cell2.addPartner(cell1);
					
					cell1.resetFlag(commonFlag, value);
				}
			} 
		});
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
		Set<Integer> nums = create1To9();
		List<SudokuCell> cells = new ArrayList<>();
		group.getContents().forEach(cell -> {
			if (cell.getValue() != null) {
				nums.remove(cell.getValue());
			} else {
				cells.add(cell);
			}
		});
		
		if (nums.size() == 1 && !cells.isEmpty()) {
			cells.get(0).setValue(nums.iterator().next());
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
		
		int solved = board.countSolved();
		int counter = 0;
		while (counter < 5) {			
			solveMissing(board);
			board.log();
			for (int i = 1; i <=9; i++) {				
				solveNext(board, i);
			}
			int currSolved = board.countSolved();
			if (solved == currSolved) {
				counter++;
			} else if (currSolved == 81) {
				break;
			} else {
				counter = 0;
			}
			solved = currSolved;
			board.log();
		}
		
		return board;
	}

}
