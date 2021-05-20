package managers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class FileManager {	
	public void createDir(String dirName, String dirLocation) throws IOException {
		Files.createDirectory(Paths.get(dirLocation + File.separator + dirName));
	}
	
	public boolean removeFile(String fileName) throws IOException {
		return Files.deleteIfExists(Paths.get(fileName));
	}
	
	public void removeDir(File dirName) throws IOException {
		Files.delete(Paths.get(dirName.toString()));
	}
	
	public void createDirPath(String dirPath) throws IOException {
		Files.createDirectories(Paths.get(dirPath));
	}
	
	public void copyFileToDir(File sourceFileName, File destDirectoryName) throws IOException {
		Path sourcePath = Paths.get(sourceFileName.toString());
		Path destPath = Paths.get(destDirectoryName.toString()).resolve(sourcePath.getFileName());
		
		Files.copy(sourcePath, destPath);
	}
	
	public void deleteFileTree(File startDirectory, boolean includeStartDirectory) throws IOException {
		Files.walkFileTree(startDirectory.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
				if (e == null) {
					if ((!dir.equals(startDirectory.toPath()) || includeStartDirectory)) {
						Files.delete(dir);
					}
					
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
	
	public void writeStringToFile(String text, File file) throws IOException {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))){
    		writer.print(text);
    		if (writer.checkError()) {
    			throw new IOException();
    		}
    	}
	}
	
	public List<String> readAllLinesFromFile(File file) throws IOException {
		return Files.readAllLines(Paths.get(file.toURI()));
	}
	
	public String readAllLinesAsStringFromFile(File file) throws IOException {
    	List<String> fileLines = readAllLinesFromFile(file);
    	StringBuilder text = new StringBuilder();
    	for (String line : fileLines) {
    		text.append(line + System.lineSeparator());
    	}
    	text.delete(text.length() - System.lineSeparator().length(), text.length());
    	
    	return text.toString();
	}
}