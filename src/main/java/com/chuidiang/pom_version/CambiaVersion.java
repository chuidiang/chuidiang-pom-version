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

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Modifica los groupId:artifactId:version de un fichero pom.xml<br>
 * En el directorio del pom.xml se crea un fichero cambia.conf con
 * una o mas lineas de texto.
 * Cada linea debe contener un groupId:artifactId:version existente en
 * el pom.xml, un espacio y un groupId:artifactId:version por el que se
 * desea cambiar.
 * Al ejecutar el plugin desde maven, se leera ese fichero cambia.conf
 * y se ejecutaran los cambios correspondientes en los pom.xml del
 * proyecto y subproyectos.
 * Se hacen copias de seguridad de los pom.xml originales llamandolas
 * pom1.xml, pom2.xml, etc.
 *
 * @goal cambia
 * 
 * @phase process-sources
 */
public class CambiaVersion
    extends AbstractMojo
{
    /**
     * Directorio del pom.xml
     * @parameter expression="${basedir}"
     * @required
     */
    private File basedir;

    /**
     * Realiza los cambios que se indiquen en el fichero cambios.conf
     */
    public void execute()
        throws MojoExecutionException
    {
    	// lectura del fichero cambio.conf
    	FicheroConfiguracion f = new FicheroConfiguracion();
    	
    	// realizacion de los cambios
    	new CambiaPom(f.getCambios(),basedir, true);
    }
}
