package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.List;

public class ZipUtil {
	private final static int BUFFER_SIZE = 1024;
	
	private ZipUtil() {}
	
	public static void zipFiles(List<String> srcFiles, String destZipFile) throws IOException {
		ZipOutputStream outZipFile = new ZipOutputStream(new FileOutputStream(destZipFile));
		for (String srcFile : srcFiles) {
			File fileToZip = new File(srcFile);
			FileInputStream inputFile = new FileInputStream(fileToZip);
			ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
			outZipFile.putNextEntry(zipEntry);
			
			byte[] buffer = new byte[BUFFER_SIZE];
			int length;
			while ((length = inputFile.read(buffer)) != -1) {
				outZipFile.write(buffer, 0, length);
			}
			inputFile.close();
		}
		outZipFile.close();
	}
	
	public static void unzipFiles(String srcZipFile, String destFolder) throws IOException {
		ZipInputStream inZipFile = new ZipInputStream(new FileInputStream(srcZipFile));
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
				
				FileOutputStream outputFile = new FileOutputStream(file);
				int len;
				while ((len = inZipFile.read(buffer)) > 0) {
					outputFile.write(buffer, 0, len);
				}
				outputFile.close();
			}
			zipEntry = inZipFile.getNextEntry();
		}
		inZipFile.closeEntry();
		inZipFile.close();
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
