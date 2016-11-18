package com.serotonin.m2m2.util.license;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.github.zafarkhaja.semver.Version;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.shared.LicenseParseException;
import com.serotonin.util.XmlUtilsTS;

/**
 * Licenses are provided to instances to inform the core/modules of the licenses granted to the instance.
 * 
 * @author Matthew
 */
public class InstanceLicense {
    private final String guid;
    private final String distributor;
    private final Version version;
    private final String licenseType;
    private final List<LicenseFeature> features;
    private final List<ModuleLicense> modules;

    public InstanceLicense(Document doc) throws LicenseParseException {
        Element licenseElement = doc.getDocumentElement();
        if (licenseElement == null || !StringUtils.equals("license", licenseElement.getTagName()))
            throw new LicenseParseException("Root element must be 'license'");

        Element coreElement = XmlUtilsTS.getChildElement(licenseElement, "core");
        if (coreElement == null)
            throw new LicenseParseException("Missing core element");

        guid = XmlUtilsTS.getChildElementText(coreElement, "guid");

        distributor = XmlUtilsTS.getChildElementText(coreElement, "distributor");
        String versionString = XmlUtilsTS.getChildElementText(coreElement, "version");
        version = versionString == null ? null : Common.parseVersion(versionString);
        licenseType = XmlUtilsTS.getChildElementText(coreElement, "licenseType");

        Element featuresElement = XmlUtilsTS.getChildElement(coreElement, "features");
        List<LicenseFeature> featureList = new ArrayList<LicenseFeature>();
        if (featuresElement != null) {
            for (Element feature : XmlUtilsTS.getChildElements(featuresElement))
                featureList.add(new LicenseFeature(feature));
        }
        features = Collections.unmodifiableList(featureList);

        Element modulesElement = XmlUtilsTS.getChildElement(licenseElement, "modules");
        List<ModuleLicense> moduleList = new ArrayList<ModuleLicense>();
        if (modulesElement != null) {
            for (Element module : XmlUtilsTS.getChildElements(modulesElement))
                moduleList.add(new ModuleLicense(module));
        }
        modules = Collections.unmodifiableList(moduleList);
    }

    public ModuleLicense getModuleLicense(String name) {
        for (ModuleLicense module : modules) {
            if (module.getName().equals(name))
                return module;
        }
        return null;
    }

    public String getGuid() {
        return guid;
    }

    public String getDistributor() {
        return distributor;
    }

    public Version getVersion() {
        return version;
    }

    public boolean versionMatches(Version coreVersion) {
        if (version == null)
            return true;
        return coreVersion.lessThan(version.incrementMajorVersion());
    }

    public String getLicenseType() {
        return licenseType;
    }

    public List<LicenseFeature> getFeatures() {
        return features;
    }

    public LicenseFeature getFeature(String name) {
        for (LicenseFeature feature : features) {
            if (feature.getName().equals(name))
                return feature;
        }
        return null;
    }

    public List<ModuleLicense> getModules() {
        return modules;
    }
}
