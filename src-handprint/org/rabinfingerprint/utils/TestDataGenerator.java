package org.rabinfingerprint.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

public class TestDataGenerator {
	public static List<String> TEST_FILES = Lists.newArrayList("testResources/1.bin", "testResources/2.bin", "testResources/3.bin");
	public static int TEST_RESOURCE_BYTES = 4 * (1 << 20); // 4 MB

	public static void validateTestResources() throws IOException {
		Random rand = new Random(42);
		for (String str : TEST_FILES) {
			File f = new File(str);
			if (!f.isFile()) {
				f.createNewFile();
				DataOutputStream out = new DataOutputStream(new FileOutputStream(f));
				for (int i = 0; i < TEST_RESOURCE_BYTES; i += 1 << 10) {
					byte[] bytes = new byte[1 << 10];
					rand.nextBytes(bytes);
					out.write(bytes);
				}
				out.close();
			}
		}
	}
}
