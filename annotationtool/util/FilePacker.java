package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FilePacker {
	
	public static String retrieveFromZip(String zipPath, String fileContents) {
		String fileName = "";
		String tempDir = "";
		if(System.getProperty("os.name").equals("Linux")) {
			tempDir = "/tmp/";
		}else{
			tempDir =  System.getProperty("java.io.tmpdir");
		}

		try {
			ZipFile zip = new ZipFile(zipPath);
			Enumeration entries = zip.entries();
			int entryNum = 0;
			while(entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if(entry.getName().contains(fileContents)) {
					InputStream in = zip.getInputStream(entry);
					fileName = tempDir + entry.getName();
					if(new File(fileName).exists()) {
						new File(fileName).delete();
					}
					Files.copy(in, Paths.get(fileName));
				}
				entryNum++;
			}
			System.out.println("Entries: " + entryNum);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("File is at: " + fileName);
		return fileName;
	}
	
	public static void createZip(String zipName, ArrayList<String> fileNames) {
		try {
			FileOutputStream fOut = new FileOutputStream(zipName);
			ZipOutputStream zOut = new ZipOutputStream(fOut);
			
			for(String fileName : fileNames) {
				addToZip(fileName, zOut);
			}
			
			zOut.close();
			fOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

	}

	public static void addToZip(String fileName, ZipOutputStream zos) throws FileNotFoundException, IOException {

		System.out.println("Writing '" + fileName + "' to zip file");

		File file = new File(fileName);
		FileInputStream fis = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}

		zos.closeEntry();
		fis.close();
	}

	
}
