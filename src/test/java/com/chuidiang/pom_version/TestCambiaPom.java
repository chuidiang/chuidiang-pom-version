package com.chuidiang.pom_version;

import junit.framework.TestCase;

public class TestCambiaPom extends TestCase {

	/**
	 * testea metodo limpiaEspacios.
	 * Este test es mas para confirmar que replaceAll() de String
	 * funciona como espero que para probar el metodo.
	 */
	public void testLimpiaEspacios() {
		CambiaPom cambia = new CambiaPom(null,null,false, null);
		String cadena = "  hola  ";
		assertEquals("hola", cambia.limpiaEspacios(cadena));
		
		cadena = "	\r\n dos\n";
		assertEquals("dos", cambia.limpiaEspacios(cadena));
	}

}
