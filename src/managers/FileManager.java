package managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileManager {
	
	private String rootDirectory;
	
	public FileManager(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
	
	public void setRootDirectory(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
	
	public void createDirectory(String directoryName) throws IOException {
		Files.createDirectory(Paths.get(rootDirectory + File.separator + directoryName));
	}
	
	public boolean removeFile(String fileName) throws IOException {
		return Files.deleteIfExists(Paths.get(fileName));
	}
	
	public void removeDirectory(File directoryName) throws IOException {
		Files.delete(Paths.get(directoryName.toString()));
	}
	
	public void createRootDirectory() throws IOException {
		Files.createDirectories(Paths.get(rootDirectory));
	}
	
	public void copyFileToDirectory(File sourceFileName, File destDirectoryName) throws IOException {
		Path sourcePath = Paths.get(sourceFileName.toString());
		Path destPath = Paths.get(destDirectoryName.toString()).resolve(sourcePath.getFileName());
		
		Files.copy(sourcePath, destPath);
	}
	
	public void deleteFileTree(File startDirectory) throws IOException {
		Files.walkFileTree(startDirectory.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
				if (e == null) {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
				else {
					throw e;
				}
			}
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
		});
	}
}