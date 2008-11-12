package com.chuidiang.pom_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;

import org.apache.maven.plugin.logging.Log;

/**
 * Lee el fichero cambio.conf en el directorio actual  y genera un
 * Hashtable con su contenido.<br>
 * El fichero cambia.conf puede contener una o mas lineas de texto.<br>
 * Cada linea debe contener un groupId:artifactId:version existente en
 * el pom.xml, un espacio y un groupId:artifactId:version por el que se
 * desea cambiar.<br>
 * El Hashtable devuelto tiene como clave los Artifact que aparecen
 * en la primera parte de la linea (que son los presuntamente existentes
 * en el fichero pom.xml que se quieren cambiar). Como valores tiene
 * los segundos Artifact que aparecen en cada linea de cambia.conf.
 * 
 * @author chuidiang
 *
 */
public class FicheroConfiguracion {
	/**
	 * Hashtable con los cambios deseados.
	 */
	Hashtable<Artifact, Artifact> cambios = new Hashtable<Artifact, Artifact>();
	
	/** Log de maven */
	private Log log;

	/**
	 * Lectura del fichero y generacion del Hashtable.
	 */
	public FicheroConfiguracion(Log log) {
		this.log = log;
		// Lectura del fichero.
		File f = new File("./cambia.conf");
		if (f.canRead()) {
			try {
				BufferedReader bis = new BufferedReader(new FileReader(f));
				String linea = bis.readLine();
				while (null != linea) {
					try {
						// Separar los artifacts partiendo la linea por el
						// espacio
						String versiones[] = linea.split(" ");
						
						// Separar groupId, artifactId y version partiendo
						// la cadena por los dos puntos :
						String[] versionOriginal = versiones[0].split(":");
						Artifact artifactOriginal = new Artifact();
						artifactOriginal.setGroupId(versionOriginal[0].trim());
						artifactOriginal.setArtifactId(versionOriginal[1]
								.trim());
						artifactOriginal.setVersion(versionOriginal[2].trim());
						
						// Idem para el segundo trozo de la linea
						String[] versionNueva = versiones[1].split(":");
						Artifact artifactNuevo = new Artifact();
						artifactNuevo.setGroupId(versionNueva[0].trim());
						artifactNuevo.setArtifactId(versionNueva[1].trim());
						artifactNuevo.setVersion(versionNueva[2].trim());
						
						// Al Hashtable.
						cambios.put(artifactOriginal, artifactNuevo);
					} catch (Exception e) {
						log.error(e);
					}
					linea = bis.readLine();
				}
			} catch (Exception e) {
				log.error(e);
			}

		}
	}

	/**
	 * Devuelve el Hashtable con los cambios deseados.
	 * @return El Hashtable.
	 */
	public Hashtable<Artifact, Artifact> getCambios() {
		return cambios;
	}
}
