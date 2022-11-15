package io.anandf.fileserver.storage;

public class StorageFactory {
	
	private static LocalDiskStorage defaultStorage = new LocalDiskStorage();
	
	public static Storage getDefaultStorage() {
		return defaultStorage;
	}

}
