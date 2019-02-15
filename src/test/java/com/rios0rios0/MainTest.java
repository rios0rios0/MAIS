package com.rios0rios0;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MainTest extends TestCase {

	@BeforeClass
	public static void init() {
	}

	@Before
	public void beforeEachTest() {
		System.out.println("This is executed before each Test...");
	}

	@After
	public void afterEachTest() {
		System.out.println("This is executed after each Test...");
	}

	@Test
	public void testEqual() {
		assertTrue(true);
	}
}