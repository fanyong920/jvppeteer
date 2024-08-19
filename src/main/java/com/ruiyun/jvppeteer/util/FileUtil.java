package com.ruiyun.jvppeteer.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 操作文件的一些公告方法
 */
public class FileUtil {

	/**
	 * 根据给定的前缀创建临时文件夹
	 * @param prefix 临时文件夹前缀
	 * @return 临时文件夹路径
	 */
	public static String createProfileDir(String prefix){
		try {
			return Files.createTempDirectory(prefix).toRealPath().toString();
		} catch (Exception e) {
			throw new RuntimeException("create temp profile dir fail:",e);
		}
		
	}

	/**
	 * 断言路径是否是可执行的exe文件
	 * @param executablePath 要断言的文件
	 * @return 可执行，返回true
	 */
	public static boolean assertExecutable(String executablePath){
		Path path = Paths.get(executablePath);
		return Files.isRegularFile(path) && Files.isReadable(path) && Files.isExecutable(path);
	}

	/**
	 * 移除文件
	 * @param path 要移除的路径
	 */
	public static void removeFolder(String path) {
		File file = new File(path);
		delete(file);
	}

	private static void delete(File file) {
		if(file.isDirectory()){
			File[] files = file.listFiles();
			if(files != null){
				for (File f : files) {
					delete(f);
				}
			}
			file.deleteOnExit();
		}else{
			 file.deleteOnExit();
		}
	}

	/**
	 * 创建一个文件，如果该文件上的有些文件夹路径不存在，会自动创建文件夹。
	 * @param file 创建的文件
	 * @throws IOException 异常
	 */
	public static final void createNewFile(File file) throws IOException {
		if(!file.exists()){
			mkdir(file.getParentFile());
			file.createNewFile();
		}
	}

	/**
	 * 递归创建文件夹
	 * @param parent 要创建的文件夹
	 */
	public static final void mkdir(File parent){
		if(parent != null && !parent.exists()){
			mkdir(parent.getParentFile());
			parent.mkdir();
		}
	}
}
