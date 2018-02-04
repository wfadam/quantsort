package quantsort;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import com.advantest.kei.KSystemVariable;
import com.advantest.kei.KTestSystem;

final public class QuantSort {
	final private static int maxInterval = systemType().startsWith("T5773") ? 100 : 50;
	final private static String prefix = "QUANT:";
	final private static String site = String.valueOf(KTestSystem.getTestSiteNumber());
	final private static String LOCK = "LOCK";

	private static Map<String, String> lot2Cnt = new HashMap<String, String>();


	private static String getLot(String platform) {
		if(platform.endsWith("ES")) {
			return "";
		}
		return KSystemVariable.read("ECOTS_SD_LOTNUMBER_AND_CURTIME");
	}

	final public static void init(String fname) {
		toKeyPair(fname, lot2Cnt);

		if(1 == KTestSystem.getTestSiteNumber()) {
			String platform = systemType();
			String lot = getLot(platform);
			String prompt = String.format("%s>", lot);
			if(platform.endsWith("ES") || lot.contains("_0001_")) {
				exportSysVar();
			}
			display(prompt);
			symbol(LOCK, "FREE");
		}
	}

	private static void exportSysVar() {
		for(Map.Entry<String, String> kv : lot2Cnt.entrySet()) {
			symbol(kv.getKey(), kv.getValue());
		}
	}

	final public static String symbol(String name) {
		return KSystemVariable.read(prefix + name).trim();
	}

	final public static void symbol(String name, String value) {
		KSystemVariable.write(prefix + name, value.trim());
	}

	final public static Map<String, String> recordMap() {
		return lot2Cnt;
	}

	final public static String arr2str(int... arr) {
		StringBuilder sb = new StringBuilder();
		for(int i : arr) {
			sb.append((char)i);
		}
		return sb.toString();
	}

	final public static void freeLock() {
		symbol(LOCK, "FREE");
		System.out.printf("Site%s freed the lock\n", site);
	}

	final public static void tryLock() {
		while(true) {
			KTestSystem.waitTime(new Random().nextInt(maxInterval) * 1e-3);

			String msg = symbol(LOCK);
			if(! "FREE".equals(msg)) {
				System.out.printf("Waiting Site%s to release the lock\n", msg);
				continue;
			}

			symbol(LOCK, site);
			if(site.equals(symbol(LOCK))) {
				System.out.printf("Site%s successfully got the lock\n", site);
				break;
			}
		}
	}

	final public static void toKeyPair(String fname, Map<String, String> l2c) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				String[] kv = line.split("\\s+");
				if(kv.length == 2 ) {
					l2c.put(kv[0].trim(), kv[1].trim());
				}
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			try{ br.close(); } catch(Exception e) { /*empty*/ }
		}
	}

	final public static void display(String prompt) {
		for(java.util.Map.Entry<String, String> kv : lot2Cnt.entrySet()) {
			System.out.printf("%s %s = %s\n", prompt, kv.getKey(), symbol(kv.getKey()));
		}
	}

	private static String systemType() {
		String cmdL = System.getenv("ATFSDIAG") != null 
			? "grep ProductType /var/opt/ATFS/" + System.getenv("ATFSDIAG") + "/common/UTD_SystemConf.txt"
			: "grep System_name /var/opt/ATKEI/" + System.getenv("ATKEIDIAG") + "/common/UTD_system_configuration.txt";

		Process p = null; 
		java.io.BufferedReader br = null;
		try{ 
			p = Runtime.getRuntime().exec(cmdL);
			if(p.waitFor() != 0) {
				return "";
			}
			br = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
			String line = "";
			while((line = br.readLine()) != null) {
				String[] kv = line.split("[ =]+");
				if(kv.length == 2) {
					return kv[1];
				}
			}
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try{ br.close(); } catch(Exception e) {/*empty*/}
		}
		return "";
	}

}

