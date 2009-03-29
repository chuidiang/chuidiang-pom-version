package com.chuidiang.pom_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.util.FileUtils;

/**
 * Test para la clase CambiaPom
 * 
 * @author Chuidiang
 * 
 */
public class TestCambiaPom extends TestCase {
    /** Path en el que estan los ficheros pom.xml de pruebas */
    private static final String SRC_TEST_CONFIG = "./src/test/config/";

    /** Borra ficheros de resultados de test anteriores */
    @Override
    protected void setUp() throws Exception {
        borraFicherosDeResultados();
        super.setUp();
    }

    /**
     * Borra los ficheros pom.xml y pom1.xml del path SRT_TEST_CONFIG.<br>
     * Dichos ficheros se supone que son de resultados de otros test
     */
    private void borraFicherosDeResultados() {
        File pom = new File(SRC_TEST_CONFIG, "pom.xml");
        if (pom.exists())
            if (!pom.delete())
                System.out.println("No puedo borrar pom.xml");
        pom = new File(SRC_TEST_CONFIG, "pom1.xml");
        if (pom.exists())
            if (!pom.delete())
                System.out.println("No puedo borrar pom1.xml");
    }

    /**
     * Borra ficheros de resultados de test anteriores
     */
    @Override
    protected void tearDown() throws Exception {
        borraFicherosDeResultados();
        super.tearDown();
    }

    /**
     * testea metodo limpiaEspacios. Este test es mas para confirmar que
     * replaceAll() de String funciona como espero que para probar el metodo.
     */
    public void testLimpiaEspacios() {
        CambiaPom cambia = new CambiaPom(null, null, false, null, null);
        String cadena = "  hola  ";
        assertEquals("hola", cambia.limpiaEspacios(cadena));

        cadena = "	\r\n dos\n";
        assertEquals("dos", cambia.limpiaEspacios(cadena));
    }

    /**
     * Testea el metodo reemplazaPropieadesPorValor(), comprobando que cambia en
     * una cadena de texto variables que existen estilo ${variable} por su
     * valor.
     */
    public void testCambiaVariables() {
        CambiaPom cambia = new CambiaPom(null, null, false, null, null);

        // Variables existentes y su valor en un Properties
        Properties propiedades = new Properties();
        propiedades.setProperty("unaVersion", "1.2.3");

        // Test de cadena con variable existente
        String valorObtenido = cambia.reemplazaPropiedadesPorValor(
                "${unaVersion}", propiedades);
        assertEquals("1.2.3", valorObtenido);

        // Test de cadena con variable existente y mas cadena
        valorObtenido = cambia.reemplazaPropiedadesPorValor(
                "uno ${unaVersion} dos", propiedades);
        assertEquals("uno 1.2.3 dos", valorObtenido);

        // Test de cadena con variable que no existe
        valorObtenido = cambia.reemplazaPropiedadesPorValor(
                "uno ${noexiste} dos", propiedades);
        assertEquals("uno ${noexiste} dos", valorObtenido);

    }

    /**
     * Con un pom yun cambio de version para hacer, comprueba que genera los dos
     * ficheros pom.xml y pom1.xml y que ha cambiado la version.
     */
    public void testEscrituraFichero() {

        // Copia del fichero de pruebas con el nombre pom.xml
        try {
            FileUtils.copyFile(new File(SRC_TEST_CONFIG,
                    "pom_testEscrituraFichero.xml"), new File(SRC_TEST_CONFIG,
                    "pom.xml"));
        } catch (IOException e) {
            fail("No se puede copiar fichero " + e);
        }

        // Cambio de version que se quiere realizar, eun Hashtable.
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

        // Instanciacion de la clase, se supone que hace los cambios.
        new CambiaPom(cambios, new File(SRC_TEST_CONFIG), false,
                new SystemStreamLog(), null);

        // Comprobacion de que existen ambos ficheros.
        File pom = new File(SRC_TEST_CONFIG, "pom.xml");
        File pom1 = new File(SRC_TEST_CONFIG, "pom1.xml");
        assertTrue(pom.exists());
        assertTrue(pom1.exists());

        // Se comparan ambos ficheros, comprobando que pom1.xml contiene la
        // version
        // antigua y pom.xml la nueva.
        try {
            BufferedReader brentrada = new BufferedReader(new FileReader(pom));
            BufferedReader bsalida = new BufferedReader(new FileReader(pom1));

            // La escritura del fichero puede cambiar la cabecera xml, por lo
            // que
            // saltamos la cabecera hasta el primert tag groupId.
            String lineaEntrada = avanzaHastaPrimerGroupId(brentrada);
            String lineaEntrada2 = avanzaHastaPrimerGroupId(bsalida);

            // Comparación de ambos ficheros, linea a linea.
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

            // Comprobacion de que ambos ficheros han llegado a fin de fichero
            assertNull(lineaEntrada);
            assertNull(lineaEntrada2);
            brentrada.close();
            bsalida.close();
        } catch (Exception e) {
            fail("no se puede leer fichero " + e);
        }
    }

    /**
     * Comprobacion de que un fichero pom.xml con variables, se reemplazan las
     * variables por su valor a la hora de leer los artifacts.
     */
    public void testPomConVariables() {
        // Copia del fichero para esta prueba en pom.xml
        try {
            FileUtils.copyFile(
                    new File(SRC_TEST_CONFIG, "pom_conVariables.xml"),
                    new File(SRC_TEST_CONFIG, "pom.xml"));
        } catch (IOException e) {
            fail("No se puede copiar fichero " + e);
        }

        // Variables existentes.
        Properties propiedades = new Properties();
        propiedades.setProperty("variableExiste", "1.2.3");

        // Instanciacion de la clase.
        CambiaPom cambia = new CambiaPom(null, new File(SRC_TEST_CONFIG), true,
                new SystemStreamLog(), propiedades);

        // Se obtiene el listado de artifacts generados por la clase.
        LinkedList<Artifact> listado = cambia.getListado();

        // El listado no es null.
        assertNotNull(listado);

        // Debe haber tres artifacts, que son los que hay en el pom.xml
        assertEquals(3, listado.size());

        // Se comrpueban las versiones de los tres artifacts.
        int contador = 0;
        for (Artifact artefacto : listado) {
            // la 1.1.0 debe permanecer inalterada.
            if ("pom_version".equals(artefacto.getArtifactId())) {
                assertEquals("1.1.0", artefacto.getVersion());
                contador++;
            }
            // ${variableExiste} debe haber sido reemplazada por "1.2.3"
            if ("maven-plugin-api".equals(artefacto.getArtifactId())) {
                assertEquals("1.2.3", artefacto.getVersion());
                contador++;
            }
            // ${variableNoExiste} no debe haber sido reemplazada, puesto que no
            // existe
            if ("junit".equals(artefacto.getArtifactId())) {
                assertEquals("${variableNoExiste}", artefacto.getVersion());
                contador++;
            }
        }
        // Se han encontrado los 3 artifacts esperados
        assertEquals(3, contador);
    }

    /**
     * Cambio de version de un pom.xml en el que hay variables.
     */
    public void testCambiaPomConVariables() {
        boolean encontradaVariable = false;
        // Copia del pom de prueba en pom.xml
        try {
            FileUtils.copyFile(
                    new File(SRC_TEST_CONFIG, "pom_conVariables.xml"),
                    new File(SRC_TEST_CONFIG, "pom.xml"));
        } catch (IOException e) {
            fail("No se puede copiar fichero " + e);
        }

        // Variables que tienen valor.
        Properties propiedades = new Properties();
        propiedades.setProperty("variableExiste", "1.2.3");

        // Cambios deseados.
        Hashtable<Artifact, Artifact> cambios = new Hashtable<Artifact, Artifact>();
        Artifact artifactOriginal = new Artifact();
        artifactOriginal.setGroupId("org.apache.maven");
        artifactOriginal.setArtifactId("maven-plugin-api");
        artifactOriginal.setVersion("1.2.3");
        Artifact artifactDestino = new Artifact();
        artifactDestino.setGroupId("org.apache.maven");
        artifactDestino.setArtifactId("maven-plugin-api");
        artifactDestino.setVersion("3.2.1");
        cambios.put(artifactOriginal, artifactDestino);

        // Instanciacion de la clase, que hace los cambios.
        new CambiaPom(cambios, new File(SRC_TEST_CONFIG), true,
                new SystemStreamLog(), propiedades);

        // Comprobacion de que existen pom.xml y pom1.xml
        File pom = new File(SRC_TEST_CONFIG, "pom.xml");
        File pom1 = new File(SRC_TEST_CONFIG, "pom1.xml");
        assertTrue(pom.exists());
        assertTrue(pom1.exists());

        try {
            // Lectura hasta el primer groupId, saltandose la cabecera, que
            // puede variar de un fichero a otro.
            BufferedReader brentrada = new BufferedReader(new FileReader(pom));
            String lineaEntrada = avanzaHastaPrimerGroupId(brentrada);
            BufferedReader bsalida = new BufferedReader(new FileReader(pom1));
            String lineaEntrada2 = avanzaHastaPrimerGroupId(bsalida);

            // Comprobacion linea por linea. Las lineas deben ser iguales,
            // salvo la que tenia la variable (ahora en pom1.xml) que ha
            // debido ser reemplazada por 3.2.1
            while (null != lineaEntrada) {
                if (lineaEntrada2.indexOf("${variableExiste}") > -1) {
                    assertTrue(lineaEntrada.indexOf("3.2.1") > -1);
                    encontradaVariable = true;
                } else {
                    assertEquals(lineaEntrada, lineaEntrada2);
                }
                lineaEntrada = brentrada.readLine();
                lineaEntrada2 = bsalida.readLine();
            }
            // Comprobacion de que ambos ficheros han llegado al final a la vez.
            assertNull(lineaEntrada);
            assertNull(lineaEntrada2);

            // Comprobacion de que en pom1.xml no se ha perdido la variable.
            assertTrue(encontradaVariable);
            bsalida.close();
            brentrada.close();
        } catch (Exception e) {
            fail("no se puede leer fichero " + e);
        }

    }

    /**
     * Avanza en el BufferedReader, leyendo linea a linea, hasta que encuentra
     * la primera linea que contiene <groupId>. Devuelve dicha linea.
     * 
     * @param brentrada
     *            BufferedReader de lectura
     * @return
     * @throws IOException
     */
    private String avanzaHastaPrimerGroupId(BufferedReader brentrada)
            throws IOException {
        String lineaEntrada = brentrada.readLine();
        while ((lineaEntrada.indexOf("groupId") == -1))
            lineaEntrada = brentrada.readLine();
        return lineaEntrada;
    }
}
