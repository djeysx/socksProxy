package sp.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class DomainRegistryImpl implements DomainRegistry {
	private static final Logger log = Logger.getLogger(DomainRegistryImpl.class);

	private final String DOMAIN_FILENAME;
	private List<String> domainList = new ArrayList<String>();

	protected File userMonitoredFile;
	private volatile long lastModified = 0L;

	public DomainRegistryImpl() {
		this("rejectedDomain.txt");
	}

	protected DomainRegistryImpl(String domainFilename) {
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
		for (String d : domainList) {
			if (domain.equals(d) || domain.endsWith("." + d)) {
				if (log.isDebugEnabled())
					log.debug("Rejected: " + domain);
				return true;
			}
		}
		return false;
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
		// log.info("Reload " + DOMAIN_FILENAME);
		FileReader fr = new FileReader(DOMAIN_FILENAME);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		ArrayList<String> swapList = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.length() < 3 || line.startsWith("#")) {
				// nothing
			} else {
				swapList.add(line);
			}
		}
		br.close();
		swapList.trimToSize();
		this.domainList = swapList;
	}

}
