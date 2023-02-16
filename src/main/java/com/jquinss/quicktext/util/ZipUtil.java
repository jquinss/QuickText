package com.jquinss.quicktext.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.Arrays;
import java.util.List;

public class ZipUtil {
	private final static int BUFFER_SIZE = 1024;
	
	private ZipUtil() {}
	
	public static void zipFiles(List<String> srcFiles, String destZipFile) throws IOException {
		try (ZipOutputStream outZipFile = new ZipOutputStream(
											new FileOutputStream(destZipFile))) {
			
			for (String srcFile : srcFiles) {
				File fileToZip = new File(srcFile);
				try (FileInputStream inputFile = new FileInputStream(fileToZip)) {
					ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
					outZipFile.putNextEntry(zipEntry);
					
					byte[] buffer = new byte[BUFFER_SIZE];
					int length;
					while ((length = inputFile.read(buffer)) != -1) {
						outZipFile.write(buffer, 0, length);
					}
				}
			}
		}
	}
	
	public static void zipFolder(String sourceFolder, String destZipFile, String... ignoredFiles) throws IOException {
		List<String> ignored = Arrays.asList(ignoredFiles);

        try (ZipOutputStream outZipFile = new ZipOutputStream(
                        new FileOutputStream(destZipFile))) {
        	Path sourcePath = Paths.get(sourceFolder);
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
            	@Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException
                {
            		if (ignored.contains(dir.toString())) {
            			return FileVisitResult.SKIP_SUBTREE;
            		}
            		
            		// zip also empty directories to keep the same file/folder structure
                    if (dir.toFile().list().length == 0) {
                    	Path targetFile = sourcePath.relativize(dir);
                    	outZipFile.putNextEntry(new ZipEntry(targetFile.toString() + System.getProperty("file.separator")));
                    	outZipFile.closeEntry();
                    }
                    
                    return FileVisitResult.CONTINUE;
                }
            	
            	
                @Override
                public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attributes) {
                	
                	if (ignored.contains(file.toString())) {
                		return FileVisitResult.CONTINUE;
                	}

                    // only copy files, no symbolic links
                    if (attributes.isSymbolicLink()) {
                        return FileVisitResult.CONTINUE;
                    }

                    try (FileInputStream inputFile = new FileInputStream(file.toFile())) {

                        Path targetFile = sourcePath.relativize(file);
                        outZipFile.putNextEntry(new ZipEntry(targetFile.toString()));

                        byte[] buffer = new byte[BUFFER_SIZE];
                        int len;
                        while ((len = inputFile.read(buffer)) > 0) {
                        	outZipFile.write(buffer, 0, len);
                        }
                        outZipFile.closeEntry();
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

        }

    }
	
	public static void unzipFiles(String srcZipFile, String destFolder) throws IOException {
		try (ZipInputStream inZipFile = new ZipInputStream(
				new FileInputStream(srcZipFile))) {
			
			File destDir = new File(destFolder);
			byte[] buffer = new byte[BUFFER_SIZE];
			ZipEntry zipEntry = inZipFile.getNextEntry();
			while (zipEntry != null) {
				File file = buildFile(destDir, zipEntry);
				if (zipEntry.isDirectory()) {
					if (!file.isDirectory() && !file.mkdirs()) {
						throw new IOException("Failed to create directory " + file);
					}
				}
				else {
					File parent = file.getParentFile();
					if (!parent.isDirectory() && !parent.mkdirs()) {
						throw new IOException("Failed to create directory " + parent);
					}
				
					try (FileOutputStream outputFile = new FileOutputStream(file)) {
						int len;
						while ((len = inZipFile.read(buffer)) > 0) {
							outputFile.write(buffer, 0, len);
						}
					}
				}
				zipEntry = inZipFile.getNextEntry();
			}
		}
	}
	
	private static File buildFile(File destDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destDir, zipEntry.getName());
		
		String destDirPath = destDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();
		
		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside the target directory: " + zipEntry.getName());
		}
		
		return destFile;
	}
}
