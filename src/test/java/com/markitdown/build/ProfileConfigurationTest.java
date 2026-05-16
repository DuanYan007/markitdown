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
    void containsCurrentReleaseProfileOnly() throws Exception {
        Document pom = loadPom();
        Map<String, Element> profiles = readProfiles(pom);

        assertEquals(1, profiles.size());
        assertTrue(profiles.containsKey("release"));
    }

    @Test
    void usesShadePackagingAndDoesNotDependOnTess4j() throws Exception {
        Document pom = loadPom();
        Map<String, Element> profiles = readProfiles(pom);

        assertTrue(profileContainsArtifact(profiles.get("release"), "maven-source-plugin"));
        assertTrue(profileContainsArtifact(profiles.get("release"), "maven-javadoc-plugin"));
        assertTrue(profileContainsArtifact(profiles.get("release"), "maven-gpg-plugin"));
        assertTrue(documentContainsArtifact(pom, "maven-shade-plugin"));
        assertTrue(documentContainsTagValue(pom, "mainClass", "com.markitdown.MarkItDownApplication"));
        assertFalse(documentContainsArtifact(pom, "tess4j"));
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

    private boolean documentContainsArtifact(Document pom, String artifactId) {
        NodeList artifactNodes = pom.getElementsByTagName("artifactId");
        for (int i = 0; i < artifactNodes.getLength(); i++) {
            if (artifactId.equals(artifactNodes.item(i).getTextContent().trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean documentContainsTagValue(Document pom, String tagName, String expectedValue) {
        NodeList nodes = pom.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            if (expectedValue.equals(nodes.item(i).getTextContent().trim())) {
                return true;
            }
        }
        return false;
    }
}
