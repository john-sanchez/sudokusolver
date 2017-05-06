package io.johnsanchez.sudokusolver.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
    
    public static void main(String[] args) {
    	List<Integer[]> list = new ArrayList<>();
		try (Scanner scanner = new Scanner(AppTest.class.getResourceAsStream("/test_cases.txt"))) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				char[] chars = line.toCharArray();
				Integer[] vals = new Integer[9];
				for (int i = 0; i < vals.length && i < chars.length; i++) {
					if (chars[i] == ' ') {
						vals[i] = null;
					} else {
						vals[i] = Integer.parseInt(chars[i] + "");
					}
				}
				list.add(vals);
			}
		}
		Integer[][] out = list.toArray(new Integer[9][]);
		SolverCore core = new SolverCore();
		SudokuBoard board = core.solve(out);
		board.log();
		
	}
}
