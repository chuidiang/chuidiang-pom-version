package com.chuidiang.pom_version;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Genera un listado en target/cambia.conf con todos los artifacts que hay
 * en el pom.xml y los pom.xml de los subproyectos<br>
 * 
 * Admite las siguientes propiedades:
 * 
 * pom_version.todo = true/false. Si es true, lista todos los artifacts
 * encontrados en los pom.xml. Si es false, solo lista los parent y
 * project.
 * 
 * pom_version.version = String. Si esta propiedad existe, se usara
 * como nueva version para el cambio de todos los artifacts que salgan
 * en el listado.
 *
 * pom_version.groupId = String. Si esta propiedad existe, se usara
 * como nuevo groupId para el cambio de todos los artifacts que salgan
 * en el listado.
 * 
 * @goal lista
 * 
 * @phase process-sources
 */
public class Listado extends AbstractMojo {

	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${basedir}"
	 * @required
	 */
	private File basedir;

	/** Directorio target del top level parent */
	private File outputDirectory;

	/**
	 * Proyecto
	 * 
	 * @parameter expression="${project}"
	 * @required
	 */
	private MavenProject proyecto;

	/**
	 * Si es true, se saca el listado de todos los artifacts contenidos en los
	 * pom.xml Si es false, solo se sacan de los project y parent.
	 * 
	 * @parameter expression="${pom_version.todo}"
	 */
	private boolean todo = false;

	/**
	 * Si contiene algun valor, usa esa version como nueva version para cambiar
	 * y poner en el fichero cambia.conf
	 * 
	 * @parameter expression="${pom_version.version}"
	 */
	private String version = null;

	/**
	 * Si contiene algun valor, usa ese groupId como nuevo groupId para cambiar
	 * y poner en el fichero cambia.conf
	 * 
	 * @parameter expression="${pom_version.groupId}"
	 */
	private String groupId = null;

	/**
	 * Analiza el pom.xml y realiza el listado.
	 */
	public void execute() throws MojoExecutionException {
		while (null != proyecto.getParent())
			proyecto = proyecto.getParent();
		outputDirectory = new File(proyecto.getBuild().getDirectory());
		if (!outputDirectory.exists())
			outputDirectory.mkdirs();

		// Si a CambiaPom se le pasa un Hashtable de cambios null,
		// no hace ningun cambio, pero genera igualmente el listado
		// de artifacts en el pom.xml
		CambiaPom cp = new CambiaPom(null, basedir, todo);
		LinkedList<Artifact> listado = cp.getListado();

		File ficheroCambiaConf = new File(outputDirectory, "cambia.conf");
		LinkedList<String> listaArtefactos = new LinkedList<String>();
		if (ficheroCambiaConf.canRead()) {
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(ficheroCambiaConf));
				String linea = br.readLine();
				while (null != linea) {
					String artefacto = linea.substring(0, linea.indexOf(" "));
					listaArtefactos.add(artefacto);
					linea = br.readLine();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			PrintWriter pw = new PrintWriter(new FileWriter(new File(
					outputDirectory, "cambia.conf"), true));
			for (Artifact a : listado) {
				String artefacto = a.toString();
				if (-1 == listaArtefactos.indexOf(artefacto)) {
					String[] partesArtefacto = artefacto.split(":");
					if (null != groupId)
						partesArtefacto[0] = groupId;
					if (null != version)
						partesArtefacto[2] = version;
					pw.println(artefacto + " " + partesArtefacto[0] + ":"
							+ partesArtefacto[1] + ":" + partesArtefacto[2]);
					listaArtefactos.add(artefacto);
				}
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
