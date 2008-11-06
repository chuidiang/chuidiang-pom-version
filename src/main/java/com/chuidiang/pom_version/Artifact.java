package com.chuidiang.pom_version;

/**
 * Describe un artifact manteniendo un groupId, artifactId y version
 * 
 * @author chuidiang
 * 
 */
public class Artifact {
	/** groupId */
	private String groupId;

	/** artifactId */
	private String artifactId;

	/** version */
	private String version;

	/**
	 * getter
	 * 
	 * @return el groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * setter
	 * 
	 * @param groupId
	 *            el groupId
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * getter
	 * 
	 * @return el artifactId
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * setter
	 * 
	 * @param artifactId
	 *            el artifactId
	 */
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	/**
	 * getter
	 * 
	 * @return la version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * setter
	 * 
	 * @param version
	 *            la version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Devuelve un String groupId:artifactId:version
	 */
	public String toString() {
		return groupId + ":" + artifactId + ":" + version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Artifact other = (Artifact) obj;
		if (artifactId == null) {
			if (other.artifactId != null)
				return false;
		} else if (!artifactId.equals(other.artifactId))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
}
