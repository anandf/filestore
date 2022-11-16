package io.anandf.fileserver.storage;

public class StorageProviderFactory {
	
	/**
	 * Use local disk based storage by default. Based on the requirement add new storage classes
	 */
	private static LocalDiskStorage defaultStorage = new LocalDiskStorage();
	
	/**
	 * Returns the default storage provider
	 * @return default storage provider
	 */
	public static StorageProvider getDefaultStorage() {
		return defaultStorage;
	}

}
