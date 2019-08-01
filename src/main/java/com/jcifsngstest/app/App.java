package com.jcifsngstest.app;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.CloseableIterator;
import jcifs.ResolverType;
import jcifs.SmbConstants;
import jcifs.SmbResource;
import jcifs.SmbTreeHandle;
import jcifs.config.DelegatingConfiguration;
import jcifs.context.CIFSContextWrapper;
import jcifs.netbios.NameServiceClientImpl;
import jcifs.smb.DosFileFilter;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFilenameFilter;
import jcifs.smb.SmbUnsupportedOperationException;
import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.Configuration;
import jcifs.SmbResource;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.context.CIFSContextWrapper;
import jcifs.smb.NtlmPasswordAuthenticator;

class JCIFSNGTEST {
	static final String SERVER		= "SERVER";
	static final String DOMAIN		= "DOMAIN";
	static final String USER_NAME		= "USER_NAME";
	static final String PASSWORD		= "PASSWORD";
	static final String SMB_VERS		= "SMB_VERS";
	static final String SIGNING		= "SIGNING";
	static final String ENCRYPTION		= "ENCRYPTION";

	static Map<String, String> opts;
	private Properties prop;

	JCIFSNGTEST(String[] args)
	{
		prop = new Properties();

		prop.put("jcifs.traceResources", "true");

		opts = new HashMap<>();
		for (String arg : args) {
			if (arg.contains("=")) {
				String k = arg.substring(0, arg.indexOf('='));
				String v = arg.substring(arg.indexOf('=') + 1);

				System.out.printf("Processing %s = %s\n",
						k, v);
				opts.put(k, v);
			}
		}
	}

	public int init_smb_prop()
	{
		if (opts.containsKey(SIGNING) && opts.get(SIGNING).equals("yes")) {
			prop.put("jcifs.smb.client.signingEnforced", true);
		} else {
			prop.put("jcifs.smb.client.signingEnforced", false);
			prop.put("jcifs.smb.client.signingPreferred", false);
			prop.put("jcifs.smb.client.ipcSigningEnforced", false);
		}

		if (opts.containsKey(ENCRYPTION) && opts.get(ENCRYPTION).equals("yes")) {
			prop.put("jcifs.smb.client.encryptionEnabled", true);
		} else {
			prop.put("jcifs.smb.client.encryptionEnabled", false);
		}


		if (opts.get(SMB_VERS).equals("2")) {
			prop.put("jcifs.smb.client.enableSMB2", true);
			prop.put("jcifs.smb.client.enableSMB1", false);
			return 0;
		}
		if (opts.get(SMB_VERS).equals("1")) {
			prop.put("jcifs.smb.client.enableSMB2", false);
			prop.put("jcifs.smb.client.disableSMB1", true);
			return 0;
		}

		System.out.println("Unsupported SMB_VERS");
		return -1;
	}

	public int ShareEnum() {
		try {
			Configuration config = new PropertyConfiguration(prop);
			CIFSContext ctx = new BaseContext(config);

			CIFSContext uctx = ctx.withCredentials(new NtlmPasswordAuthenticator(opts.get(DOMAIN),
											     opts.get(USER_NAME),
											     opts.get(PASSWORD)));
			SmbFile smbFile = new SmbFile("smb://" + opts.get(SERVER), uctx);

			String[] list = smbFile.list();
			System.out.println(Arrays.toString(list));
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
}

public class App
{
	public static void main( String[] args )
	{
		JCIFSNGTEST jt = new JCIFSNGTEST(args);

		if (jt.init_smb_prop() != 0)
			return;

		jt.ShareEnum();
	}
}
