package com.markitdown.build;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProfileConfigurationTest {

    @Test
    void containsExpectedPackagingProfiles() throws Exception {
        Document pom = loadPom();
        Map<String, Element> profiles = readProfiles(pom);

        assertTrue(profiles.containsKey("full"));
        assertTrue(profiles.containsKey("win32"));
        assertTrue(profiles.containsKey("win64"));
        assertTrue(profiles.containsKey("linux64"));
        assertTrue(profiles.containsKey("mac"));
    }

    @Test
    void embedsTess4jOnlyForWindowsAndFullProfiles() throws Exception {
        Document pom = loadPom();
        Map<String, Element> profiles = readProfiles(pom);

        assertTrue(profileContainsArtifact(profiles.get("full"), "tess4j"));
        assertTrue(profileContainsArtifact(profiles.get("win32"), "tess4j"));
        assertTrue(profileContainsArtifact(profiles.get("win64"), "tess4j"));
        assertFalse(profileContainsArtifact(profiles.get("linux64"), "tess4j"));
        assertFalse(profileContainsArtifact(profiles.get("mac"), "tess4j"));
    }

    private Document loadPom() throws Exception {
        Path pomPath = Path.of("pom.xml").toAbsolutePath();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        return factory.newDocumentBuilder().parse(pomPath.toFile());
    }

    private Map<String, Element> readProfiles(Document pom) {
        Map<String, Element> profiles = new HashMap<>();
        NodeList profileNodes = pom.getElementsByTagName("profile");
        for (int i = 0; i < profileNodes.getLength(); i++) {
            Element profile = (Element) profileNodes.item(i);
            String id = profile.getElementsByTagName("id").item(0).getTextContent().trim();
            profiles.put(id, profile);
        }
        return profiles;
    }

    private boolean profileContainsArtifact(Element profile, String artifactId) {
        if (profile == null) {
            return false;
        }
        NodeList artifactNodes = profile.getElementsByTagName("artifactId");
        for (int i = 0; i < artifactNodes.getLength(); i++) {
            if (artifactId.equals(artifactNodes.item(i).getTextContent().trim())) {
                return true;
            }
        }
        return false;
    }
}
