package com.chuidiang.pom_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.util.FileUtils;

public class TestCambiaPom extends TestCase {

    private static final String SRC_TEST_CONFIG = "./src/test/config/";

    public TestCambiaPom(String nombre) {
        super(nombre);
        File pom = new File(SRC_TEST_CONFIG, "pom.xml");
        if (pom.exists())
            pom.delete();
        pom = new File(SRC_TEST_CONFIG, "pom1.xml");
        if (pom.exists())
            pom.delete();

    }

    /**
     * testea metodo limpiaEspacios. Este test es mas para confirmar que
     * replaceAll() de String funciona como espero que para probar el metodo.
     */
    public void testLimpiaEspacios() {
        CambiaPom cambia = new CambiaPom(null, null, false, null);
        String cadena = "  hola  ";
        assertEquals("hola", cambia.limpiaEspacios(cadena));

        cadena = "	\r\n dos\n";
        assertEquals("dos", cambia.limpiaEspacios(cadena));
    }

    public void testEscrituraFichero() {
        try {
            FileUtils.copyFile(new File(SRC_TEST_CONFIG,
                    "pom_testEscrituraFichero.xml"), new File(SRC_TEST_CONFIG,
                    "pom.xml"));
        } catch (IOException e) {
            fail("No se puede copiar fichero " + e);
        }

        Artifact artifactOrigen = new Artifact();
        artifactOrigen.setGroupId("com.chuidiang");
        artifactOrigen.setArtifactId("pom_version");
        artifactOrigen.setVersion("1.1.0");
        Artifact artifactDestino = new Artifact();
        artifactDestino.setGroupId("com.chuidiang");
        artifactDestino.setArtifactId("pom_version");
        artifactDestino.setVersion("1.1.1");
        Hashtable<Artifact, Artifact> cambios = new Hashtable<Artifact, Artifact>();
        cambios.put(artifactOrigen, artifactDestino);

        CambiaPom cambia = new CambiaPom(cambios, new File(SRC_TEST_CONFIG),
                false, new SystemStreamLog());

        File pom = new File(SRC_TEST_CONFIG, "pom.xml");
        File pom1 = new File(SRC_TEST_CONFIG, "pom1.xml");
        assertTrue(pom.exists());
        assertTrue(pom1.exists());

        try {
            BufferedReader brentrada = new BufferedReader(new FileReader(pom));
            BufferedReader bsalida = new BufferedReader(new FileReader(pom1));
            String lineaEntrada = brentrada.readLine();
            while (lineaEntrada.indexOf("groupId") == -1)
                lineaEntrada = brentrada.readLine();
            String lineaEntrada2 = bsalida.readLine();
            while (lineaEntrada2.indexOf("groupId") == -1)
                lineaEntrada2 = bsalida.readLine();
            while (null != lineaEntrada) {
                if (lineaEntrada.indexOf("1.1.1") == -1) {
                    assertEquals(lineaEntrada, lineaEntrada2);
                } else {
                    assertEquals("<version>1.1.1</version>", lineaEntrada
                            .trim());
                    assertEquals("<version>1.1.0</version>", lineaEntrada2
                            .trim());
                }
                lineaEntrada = brentrada.readLine();
                lineaEntrada2 = bsalida.readLine();
            }
            assertNull(lineaEntrada);
            assertNull(lineaEntrada2);
        } catch (Exception e) {
            fail("no se puede leer fichero " + e);
        }
    }
}
