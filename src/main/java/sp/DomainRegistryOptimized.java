package sp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class DomainRegistryOptimized implements DomainRegistry {
	private static final Logger log = Logger.getLogger(DomainRegistryOptimized.class);

	private final String DOMAIN_FILENAME;
	private Map<String, Map> subdomainMap = new HashMap<String, Map>();

	protected File userMonitoredFile;
	private volatile long lastModified = 0L;

	public DomainRegistryOptimized() {
		this("rejectedDomain.txt");
	}

	protected DomainRegistryOptimized(String domainFilename) {
		this.DOMAIN_FILENAME = domainFilename;
		this.userMonitoredFile = new File(DOMAIN_FILENAME);
	}

	public boolean match(String domain) {
		try {
			ensureDomainListLoaded();
		} catch (Exception e) {
			log.error(e, e);
			return false;
		}

		boolean result = checkMatchTree(domain);

		if (!result) {
			if (domain.startsWith("ad") //
					|| domain.startsWith("platform")//
					|| domain.startsWith("pub")//
			) {
				log.warn("may be advertisement : " + domain);
			} else if (domain.contains("ad") //
					|| domain.contains("pub")//
			)
				log.warn("may be advertisement : " + domain);
		}

		return result;
	}

	private boolean checkMatchTree(String domain) {
		// subdomain
		Map<String, Map> currentPos = subdomainMap;
		for (String d : reverseSubDomains(domain)) {
			if (currentPos.size() > 0) {
				currentPos = currentPos.get(d);
				if (currentPos == null) {
					// cas de pas trouvé
					// System.out.println("End currentPos=" + currentPos + " d="
					// + d + " pas trouvé : false");
					return false;
				}
			} else {
				// cas de plus grand
				// System.out.println("End currentPos=" + currentPos + " d=" + d
				// + " plus grand : true");
				return true;
			}
		}

		if (currentPos != null && currentPos.size() == 0) {
			// cas de equals
			// System.out.println("End currentPos=" + currentPos +
			// " endOfLoop equals: true");
			return true;
		} else {
			// cas de plus petit
			// System.out.println("End currentPos=" + currentPos +
			// " endOfLoop plus petit : false");
			return false;
		}
	}

	private synchronized void ensureDomainListLoaded() throws IOException {
		try {
			if (!this.userMonitoredFile.exists())
				throw new FileNotFoundException(this.userMonitoredFile.getAbsolutePath());
			long curLastModified = userMonitoredFile.lastModified();
			if (curLastModified != this.lastModified) {
				// RELOAD des scripts
				log.info("Reload file " + this.userMonitoredFile.getAbsolutePath());
				this.lastModified = curLastModified;
				loadDomainList();
			}
		} catch (IOException e) {
			this.lastModified = 0;
			throw e;
		}
	}

	private void loadDomainList() throws IOException {
		FileReader fr = new FileReader(DOMAIN_FILENAME);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		Map<String, Map> swap_subdomainMap = new HashMap<String, Map>();
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.length() < 3 || line.startsWith("#")) {
				// nothing
			} else {
				putSubdomain(swap_subdomainMap, line);
			}
		}
		br.close();
		subdomainMap = swap_subdomainMap;
	}

	private void putSubdomain(Map<String, Map> swap_subdomainMap, String line) {
		String[] subdomainsReverse = reverseSubDomains(line);
		Map<String, Map> currentPos = swap_subdomainMap;
		for (String sdr : subdomainsReverse) {
			Map<String, Map> future_currentPos = currentPos.get(sdr);
			if (future_currentPos == null) {
				future_currentPos = new HashMap<String, Map>();
				currentPos.put(sdr, future_currentPos);
			}
			currentPos = future_currentPos;
		}
	}

	protected String[] reverseSubDomains(String domain) {
		String[] subdomains = domain.split("\\.");
		String[] subdomainsReverse = new String[subdomains.length];
		for (int i = 0; i < subdomains.length; i++) {
			subdomainsReverse[subdomains.length - 1 - i] = subdomains[i];
		}
		return subdomainsReverse;
	}
}
