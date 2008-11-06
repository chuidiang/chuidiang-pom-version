package com.chuidiang.pom_version;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.DOMWriterImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Se le pasa la lista de cambios a hacer en el pom.xml y los realiza. Los
 * cambios se pasan por medio de un Hashtable en el que tanto las claves como
 * loas valores son Artifact. Las claves son los Artifact existentes en el
 * pom.xml. Los valores son los nuevos Artifact deseados.
 * 
 * @author chuidiang
 * 
 */
public class CambiaPom {

	/** Marca si ha habido cambios en el pom.xml, para reescribirlo */
	private boolean hayCambios = false;

	/**
	 * Cambios a realizar. Las claves son los Artifacts existentes en el
	 * pom.xml, los valores son los nuevos Artifacts deseados.
	 */
	private Hashtable<Artifact, Artifact> cambios = null;

	/**
	 * Directorio en el que se encuentra el fichero pom.xml a analizar.
	 */
	private File basedir;

	/** Lista de Artifacts que se han encontrado en el pom.xml */
	private LinkedList<Artifact> listado = new LinkedList<Artifact>();

	/**
	 * Indica si saca en el listado todos los artifacts. Si es false, solo saca
	 * los de parent y project.
	 */
	private boolean todo;

	/**
	 * Constructor de la clase. Analiza el fichero pom.xml en el directorio
	 * basedir que se le pasa y realiza los cambios que se indican en el
	 * Hashtable cambios.<br>
	 * En el proceso rellena el atributo listado con todos los Artifacts
	 * encontrados. Podemos usar esta clase pasando null en cambios para obtener
	 * un listado de los Artifacts que aparecen en el pom.xml
	 * 
	 * @param cambios
	 *            Cambios que se desean.
	 * @param basedir
	 *            Directorio donde esta el pom.xml
	 * @param todo
	 *            Si es true, anade todos los artifacts que encuentre en el
	 *            pom.xml a la lista. Si es false, solo anade los parent y
	 *            project.
	 */
	public CambiaPom(Hashtable<Artifact, Artifact> cambios, File basedir,
			boolean todo) {
		this.todo = todo;
		this.basedir = basedir;
		this.cambios = cambios;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document documento = null;

		try {
			// Lectura del pom.xml
			DocumentBuilder builder = factory.newDocumentBuilder();
			documento = builder.parse(new File(basedir, "pom.xml"));

			// Se recorren los nodos leidos para realizar los cambios.
			Node n = documento.getFirstChild();
			analiza(n);

			// Si ha habido cambios, se escribe un fichero ppom.xml
			// con los cambios realizados.
			if (hayCambios) {
				DOMWriterImpl domWriter = new DOMWriterImpl();
				FileOutputStream f = new FileOutputStream(new File(basedir,
						"ppom.xml"));
				domWriter.writeNode(f, n);
				f.close();

				// Se renombran los ficheros, de forma que el original
				// pasa a ser pom1.xml y el ppom.xml que acabamos de
				// escribir pasa a ser pom.xml
				renombraFicheros();
			}
		} catch (Exception spe) {
			spe.printStackTrace();
		}

	}

	/**
	 * Renombra los ficheros pom.xml.<br> - pom.xml pasa a pom1.xml. Si ya
	 * existe a pom2.xml y asi sucesivamente.<br> - ppom.xml pasa a ser el
	 * nuevo pom.xml
	 */
	private void renombraFicheros() {
		File pom = new File(basedir, "pom.xml");

		// busqueda de un pom?.xml que no exista.
		int version = 1;
		File rename = new File(basedir, "pom" + version + ".xml");
		while (rename.exists()) {
			version++;
			rename = new File(basedir, "pom" + version + ".xml");
		}

		// renombrado.
		pom.renameTo(rename);
		File ppom = new File(basedir, "ppom.xml");
		ppom.renameTo(new File(basedir, "pom.xml"));
	}

	/**
	 * Mira si el nodo es susceptible de contener un Artifact para ver si es
	 * cambiable. Luego, recursivamente, se llama a si mismo para todos los
	 * nodos hijos.
	 * 
	 * @param n
	 *            Nodo a analizar.
	 */
	private void analiza(Node n) {
		// El tag project puede tener dentro groupId, artifactId
		// y version.
		if ("project".equals(n.getNodeName()))
			analizaArtifact(n);
		// Idem con los tag dependency
		if ("dependency".equals(n.getNodeName()))
			analizaArtifact(n);
		// y los tag parent.
		if ("parent".equals(n.getNodeName()))
			analizaArtifact(n);

		// Analisis de los hijos.
		NodeList lista = n.getChildNodes();
		int numeroNodos = lista.getLength();
		for (int i = 0; i < numeroNodos; i++)
			analiza(lista.item(i));
	}

	/**
	 * Busca los nodos hijos groupId, artifactId y version del nodo que se le
	 * pasa y mira a ver si deben ser cambiados de acuerdo al Hashtable que se
	 * paso en el constructor. Realiza si procede los cambios, actualiza el
	 * listado de Artifacts y marca el flag hayCambios si los hay.
	 * 
	 * @param n
	 *            Nodo a analizar.
	 */
	private void analizaArtifact(Node n) {
		NodeList lista = n.getChildNodes();
		Node nodoGroupId = null;
		Node nodoArtifactId = null;
		Node nodoVersion = null;
		int numeroNodos = lista.getLength();
		Artifact artifact = new Artifact();

		// recorre nodos hijos buscando groupId, artifactId y version
		for (int i = 0; i < numeroNodos; i++) {
			Node hijo = lista.item(i);
			if ("groupId".equals(hijo.getNodeName())) {
				artifact.setGroupId(hijo.getTextContent());
				nodoGroupId = hijo;
			}
			if ("artifactId".equals(hijo.getNodeName())) {
				artifact.setArtifactId(hijo.getTextContent());
				nodoArtifactId = hijo;
			}
			if ("version".equals(hijo.getNodeName())) {
				artifact.setVersion(hijo.getTextContent());
				nodoVersion = hijo;
			}
		}

		// Anade el Artifact al listado si no estaba ya.
		if (-1 == listado.indexOf(artifact)) {
			if ("dependency".equals(n.getNodeName())) {
				if (todo)
					listado.add(artifact);
			} else
				listado.add(artifact);
		}

		// Realiza los cambios si esta Artifact hay que cambiarlo.
		if (null != cambios) {
			Artifact nuevo = cambios.get(artifact);
			if (null != nuevo) {
				hayCambios = true;
				System.out.println("Cambiando " + artifact + " --> " + nuevo);
				nodoGroupId.setTextContent(nuevo.getGroupId());
				nodoArtifactId.setTextContent(nuevo.getArtifactId());
				nodoVersion.setTextContent(nuevo.getVersion());
			}
		}
	}

	/**
	 * Devuelve el listado de Artifacts encontrados en el pom.xml actual.
	 * 
	 * @return Lista de artifacts
	 */
	public LinkedList<Artifact> getListado() {
		return listado;
	}
}
