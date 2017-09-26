package com.tomkou.chat.util;

import java.io.StringReader;

import static org.junit.Assert.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;

public class XMLUtilTest {
	private SAXReader saxReader;
	
	@Before
	public void setUp() {
		saxReader = new SAXReader();
	}
	
	@Test
	public void testConstructLoginXML() {
		try {
			String xml = XMLUtil.constructLoginXML("Tom");
			
			Document document = saxReader.read(new StringReader(xml));
			
			Element root = document.getRootElement();
			
			String rootName = root.getName();
			
			Element typeElement = root.element("type");
			Element userElement = root.element("user");
			
			assertEquals("message", rootName);
			assertEquals("1", typeElement.getText());
			assertEquals("Tom", userElement.getText());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			fail();
		}
	}
}
