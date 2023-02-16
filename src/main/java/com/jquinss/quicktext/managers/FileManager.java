package com.jquinss.quicktext.managers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

public class FileManager {	
	public void createDir(File dirName) throws IOException {
		Files.createDirectory(dirName.toPath());
	}
	
	public boolean removeFile(File fileName) throws IOException {
		return Files.deleteIfExists(fileName.toPath());
	}
	
	public void removeDir(File dirName) throws IOException {
		Files.delete(Paths.get(dirName.toString()));
	}
	
	public void createDirPath(String dirPath) throws IOException {
		Files.createDirectories(Paths.get(dirPath));
	}
	
	public void copyFile(File sourceFileName, File destFileName) throws IOException {
		if (destFileName.exists()) {
			throw new FileAlreadyExistsException(destFileName.toString());
		}
		Files.copy(sourceFileName.toPath(), destFileName.toPath());
	}
	
	public void moveFile(File sourceFileName, File destFileName) throws IOException {
		Files.move(sourceFileName.toPath(), destFileName.toPath());
	}
	
	public String getNextAvailableFileName(String dirName, String fileName, String extension) {
		int i = 0;
		StringBuilder resultFileName = new StringBuilder();
		File destFilePath = null;
		
		do {
			resultFileName = new StringBuilder();
			resultFileName.append(fileName);
			resultFileName.append("(");
			resultFileName.append(i);
			resultFileName.append(")");

			destFilePath = buildFilePath(dirName, resultFileName.toString(), extension);
			i++;
		} while (destFilePath.exists());
		
		
		return resultFileName.toString();
	}
	
	public File buildFilePath(String dirName, String fileName, String...suffixes) {
		StringBuilder filePath = new StringBuilder();
		filePath.append(dirName);
		filePath.append(File.separator);
		filePath.append(fileName);
		for (String suffix : suffixes) {
			filePath.append(suffix);
		}
		
		return new File(filePath.toString());
	}
	
	public File buildFilePath(File dirName, String fileName, String...suffixes) {
		return buildFilePath(dirName.toString(), fileName, suffixes);
	}
	
	public String getExtensionFromFile(String fileName) {
		String suffix = new String();
		int suffixIndex = fileName.lastIndexOf(".");
		
		if (suffixIndex != -1) {
			suffix = fileName.substring(suffixIndex);
		}
		
		return suffix;
	}
	
	public String removeFileExtension(String fileName, String extension) {
		return fileName.substring(0, fileName.lastIndexOf(extension));
	}
	
	public String getRelativePath(File root, File file) {
		return root.toPath().relativize(file.toPath()).toString();
	}
	
	public void deleteFileTree(File startDirectory, boolean includeStartDirectory, String... ignoredFiles) throws IOException {
		List<String> ignored = Arrays.asList(ignoredFiles);
		
		Files.walkFileTree(startDirectory.toPath(), new SimpleFileVisitor<Path>() {
			@Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
        		if (ignored.contains(dir.toString())) {
        			return FileVisitResult.SKIP_SUBTREE;
        		}
                
                return FileVisitResult.CONTINUE;
            }
			
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
            	if (ignored.contains(file.toString())) {
            		return FileVisitResult.CONTINUE;
            	}
            	
				Files.delete(file);
				
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	public void writeStringToFile(String text, File file, Charset charset) throws IOException {
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset)))){
    		writer.print(text);
    		if (writer.checkError()) {
    			throw new IOException();
    		}
    	}
	}
	
	public List<String> readAllLinesFromFileAsList(File file, Charset charset) throws IOException {
		return Files.readAllLines(Paths.get(file.toURI()), charset);
	}
	
	public String readAllLinesFromFileAsString(File file, Charset charset) throws IOException {
    	List<String> fileLines = readAllLinesFromFileAsList(file, charset);
    	StringBuilder text = new StringBuilder();
    	for (String line : fileLines) {
    		text.append(line + System.lineSeparator());
    	}
    	
    	if (!fileLines.isEmpty()) {
    		text.delete(text.length() - System.lineSeparator().length(), text.length());
    	}
    	
    	return text.toString();
	}
}