package test.com;

import java.io.File;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		
//		String path = "E:\\excel";

		String path = args[0];
		File dir = new File(path);
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}

		for (File f : files) {
			if (f.isFile()) {
				String suffix = f.getName().split("\\.")[1].trim();
				if ("xlsx".equals(suffix) || "xls".equals(suffix)) {
					try {
						new ExcelHelper().importData(f);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

	}
}
