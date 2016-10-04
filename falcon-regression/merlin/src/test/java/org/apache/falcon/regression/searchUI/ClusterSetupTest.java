/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.falcon.regression.searchUI;

import org.apache.commons.lang.StringUtils;
import org.apache.falcon.entity.v0.cluster.ClusterLocationType;
import org.apache.falcon.entity.v0.cluster.Interface;
import org.apache.falcon.entity.v0.cluster.Interfacetype;
import org.apache.falcon.entity.v0.cluster.Location;
import org.apache.falcon.entity.v0.cluster.Property;
import org.apache.falcon.regression.Entities.ClusterMerlin;
import org.apache.falcon.regression.core.bundle.Bundle;
import org.apache.falcon.regression.core.helpers.ColoHelper;
import org.apache.falcon.regression.core.util.BundleUtil;
import org.apache.falcon.regression.testHelper.BaseUITestClass;
import org.apache.falcon.regression.ui.search.ClusterWizardPage;
import org.apache.falcon.regression.ui.search.LoginPage;
import org.apache.falcon.regression.ui.search.SearchPage;
import org.apache.hadoop.security.authentication.client.AuthenticationException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Tests for cluster setup page.
 */
@Test(groups = "search-api")
public class ClusterSetupTest extends BaseUITestClass{
    private ClusterWizardPage clusterSetup = null;
    private ColoHelper cluster = servers.get(0);
    private ClusterMerlin sourceCluster;

    @BeforeClass(alwaysRun = true)
    public void prepareCluster() throws IOException {
        bundles[0] = BundleUtil.readELBundle();
        bundles[0] = new Bundle(bundles[0], cluster);
        bundles[0].generateUniqueBundle(this);
    }

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        openBrowser();
        SearchPage homePage = LoginPage.open(getDriver()).doDefaultLogin();
        clusterSetup = homePage.getPageHeader().doCreateCluster();
        clusterSetup.checkPage();
        sourceCluster = bundles[0].getClusterElement();
        //add custom cluster properties
        sourceCluster.setTags("myTag1=myValue1");
        sourceCluster.setDescription("description");
    }

    @Test
    public void testHeader() {
        clusterSetup.getPageHeader().checkHeader();
    }

    /**
     * Default cluster creation scenario. Populate fields with valid values. Click next. Return back and click
     * next again. Check that all values are present on Summary page. Save cluster.
     * Check the cluster definition trough /definition API.
     */
    @Test
    public void testDefaultScenario()
        throws URISyntaxException, AuthenticationException, InterruptedException, IOException {
        Assert.assertFalse(clusterSetup.isXmlPreviewExpanded(), "Xml preview should be collapsed by default.");
        clusterSetup.fillForm(sourceCluster);
        clusterSetup.clickNext();
        clusterSetup.clickPrevious();
        clusterSetup.clickNext();
        ClusterMerlin summaryBlock = clusterSetup.getSummary(sourceCluster.getEmptyCluster());
        //summary block should contain the same info as source
        sourceCluster.assertEquals(summaryBlock);
        clusterSetup.clickSave();
        String alertText = clusterSetup.getActiveAlertText();
        Assert.assertEquals(alertText, "falcon/default/Submit successful (cluster) " + sourceCluster.getName());
        //check the same via notifications bar
        clusterSetup.getPageHeader().validateNotificationCountAndCheckLast(1,
            "falcon/default/Submit successful (cluster) " + sourceCluster.getName());
        ClusterMerlin definition = new ClusterMerlin(cluster.getClusterHelper()
            .getEntityDefinition(bundles[0].getClusterElement().toString()).getMessage());
        //definition should be the same that the source
        sourceCluster.assertEquals(definition);
    }

    /**
     * Populate fields with valid values. Check that changes are reflected on XMLPreview block. Click next.
     * Check that XML is what we have populated on the previous step.
     */
    @Test
    public void testXmlPreview() {
        clusterSetup.fillForm(sourceCluster);
        ClusterMerlin generalStepPreview = clusterSetup.getEntityFromXMLPreview();
        cleanGeneralPreview(generalStepPreview);
        sourceCluster.assertEquals(generalStepPreview);
        clusterSetup.clickNext();
        ClusterMerlin summaryStepPreview = clusterSetup.getEntityFromXMLPreview();
        sourceCluster.assertEquals(summaryStepPreview);
        generalStepPreview.assertEquals(summaryStepPreview);
    }

    private void cleanGeneralPreview(ClusterMerlin clusterMerlin) {
        //On general step xml preview has extra empty values which should be removed to compare data.
        List<Location> locations = clusterMerlin.getLocations().getLocations();
        int last = locations.size() - 1;
        if (locations.get(last).getName() == null && locations.get(last).getPath().isEmpty()) {
            locations.remove(last);
        }
        List<Interface> interfaces = clusterMerlin.getInterfaces().getInterfaces();
        last = interfaces.size() - 1;
        if (interfaces.get(last).getEndpoint().isEmpty() && interfaces.get(last).getVersion().isEmpty()) {
            interfaces.remove(last);
        }
        List<Property> properties = clusterMerlin.getProperties().getProperties();
        last = properties.size() - 1;
        if (properties.get(last).getName().isEmpty() && properties.get(last).getValue().isEmpty()) {
            properties.remove(last);
        }
    }

    /**
     * Add location to cluster. Check that it is present. Check XML preview has it.
     * Delete the location. Check that it has been deleted from wizard window.
     */
    @Test
    public void testAddDeleteLocation() {
        //to make addLocation button enabled
        sourceCluster.addLocation(ClusterLocationType.WORKING, "/one-another-temp");
        clusterSetup.fillForm(sourceCluster);

        //check without extra location
        ClusterMerlin preview = clusterSetup.getEntityFromXMLPreview();
        cleanGeneralPreview(preview);
        sourceCluster.assertEquals(preview);

        //add one more location to the form and check results
        String path = "/one-extra-working";
        Location location = new Location();
        location.setName(ClusterLocationType.WORKING);
        location.setPath(path);
        clusterSetup.clickAddLocation();
        clusterSetup.fillAdditionalLocation(location);
        Assert.assertTrue(clusterSetup.checkElementByContent("input", path), "Location should be present.");
        preview = clusterSetup.getEntityFromXMLPreview();
        cleanGeneralPreview(preview);
        //add location to source to compare equality
        sourceCluster.addLocation(ClusterLocationType.WORKING, path);
        sourceCluster.assertEquals(preview);

        //delete location and check results
        clusterSetup.clickDeleteLocation();
        Assert.assertFalse(clusterSetup.checkElementByContent("input", path), "Location should be absent.");
        preview = clusterSetup.getEntityFromXMLPreview();
        cleanGeneralPreview(preview);
        //remove location from source to check equality
        int last = sourceCluster.getLocations().getLocations().size() - 1;
        sourceCluster.getLocations().getLocations().remove(last);
        sourceCluster.assertEquals(preview);
    }

    /**
     * Add tag to cluster. Check that it is present. Check XML preview has it.
     * Delete the tag. Check that it has been deleted from wizard window.
     */
    @Test
    public void testAddDeleteTag() {
        clusterSetup.fillForm(sourceCluster);

        //check without extra tag
        ClusterMerlin preview = clusterSetup.getEntityFromXMLPreview();
        cleanGeneralPreview(preview);
        sourceCluster.assertEquals(preview);

        //add one more tag to the form and check results
        clusterSetup.clickAddTag();
        clusterSetup.addTag("myTag2", "myValue2");
        Assert.assertTrue(clusterSetup.checkElementByContent("input", "myTag2"), "Tag should be present");
        Assert.assertTrue(clusterSetup.checkElementByContent("input", "myValue2"), "Tag should be present");
        preview = clusterSetup.getEntityFromXMLPreview();
        cleanGeneralPreview(preview);
        //add tag to source to compare equality
        sourceCluster.setTags("myTag1=myValue1,myTag2=myValue2");
        sourceCluster.assertEquals(preview);

        //delete location and check results
        clusterSetup.clickDeleteTag();
        Assert.assertFalse(clusterSetup.checkElementByContent("input", "myTag2"), "Tag should be absent.");
        Assert.assertFalse(clusterSetup.checkElementByContent("input", "myValue2"), "Tag should be absent.");
        preview = clusterSetup.getEntityFromXMLPreview();
        cleanGeneralPreview(preview);
        //remove location from source to check equality
        sourceCluster.setTags("myTag1=myValue1");
        sourceCluster.assertEquals(preview);
    }

    /**
     * Check that staging interface is unavailable by default but becomes available when we set matching checkbox.
     */
    @Test
    public void testRegistryInterface() {
        Assert.assertFalse(clusterSetup.isRegistryEnabled(), "Registry should be disabled.");
        clusterSetup.checkRegistry(true);
        Assert.assertTrue(clusterSetup.isRegistryEnabled(), "Registry should be enabled.");
        clusterSetup.checkRegistry(false);
        Assert.assertFalse(clusterSetup.isRegistryEnabled(), "Registry should be disabled again.");
    }

    /**
     * Check that interface version with different length and parts is allowed.
     */
    @Test
    public void testDifferentInterfaceVersions() {
        sourceCluster.addInterface(Interfacetype.REGISTRY, "http://colo-1.example.com:15000", "1.1.1");
        clusterSetup.checkRegistry(true);
        clusterSetup.fillForm(sourceCluster);
        StringBuilder partialVersion = new StringBuilder("");
        for (String c : new String[]{"3", ".", "2", ".", "0"}) {
            partialVersion.append(c);
            for (Interface inface : sourceCluster.getInterfaces().getInterfaces()) {
                inface.setVersion(partialVersion.toString());
                clusterSetup.setInterfaceVersion(inface);
            }
            clusterSetup.clickNext();
            clusterSetup.clickPrevious();
        }
    }

    /**
     * Populate working location with value pointing to directory with wider permissions then 755.
     * Check that user is not allowed to create a cluster and is notified with an alert.
     */
    @Test
    public void testLocationsBadPermissions() throws IOException {
        //reverse staging and working location dirs
        String staging = sourceCluster.getLocation(ClusterLocationType.STAGING).getPath();
        String working = sourceCluster.getLocation(ClusterLocationType.WORKING).getPath();
        //set working to dir which has 777 permissions
        sourceCluster.getLocation(ClusterLocationType.WORKING).setPath(staging);
        //set staging to dir which has 755 permissions
        sourceCluster.getLocation(ClusterLocationType.STAGING).setPath(working);
        clusterSetup.fillForm(sourceCluster);
        clusterSetup.clickNext();
        clusterSetup.clickSave();
        String alertMessage = clusterSetup.getActiveAlertText();
        Assert.assertEquals(alertMessage,
            String.format("Path %s has permissions: rwxr-xr-x, should be rwxrwxrwx", working));
    }

    /**
     * Provide locations which are formally correct but don't exist.
     * Check that user can't create cluster and has been notified with an alert.
     */
    @Test
    public void testLocationsNonExistent() throws IOException {
        String nonExistent = "/non-existent-directory";
        sourceCluster.getLocation(ClusterLocationType.STAGING).setPath(nonExistent);
        clusterSetup.fillForm(sourceCluster);
        clusterSetup.clickNext();
        clusterSetup.clickSave();
        String alertMessage = clusterSetup.getActiveAlertText();
        Assert.assertTrue(alertMessage.contains(String.format("Location %s for cluster %s must exist.",
            nonExistent, sourceCluster.getName())), "Alert message should match to expected.");
        //check the same through notification bar
        clusterSetup.getPageHeader().validateNotificationCountAndCheckLast(1,
            String.format("Location %s for cluster %s must exist.", nonExistent, sourceCluster.getName()));
    }

    /**
     * Specify the same directory locations for staging and working location.
     * Check that user is not allowed to create a cluster with same directory for both with proper error message.
     */
    @Test
    public void testSameLocations() throws IOException {

        //get the staging directory location
        String staging = sourceCluster.getLocation(ClusterLocationType.STAGING).getPath();

        //set the working directory to staging directory
        sourceCluster.getLocation(ClusterLocationType.WORKING).setPath(staging);

        clusterSetup.fillForm(sourceCluster);
        clusterSetup.clickJustNext();
        clusterSetup.assertLocationsEqualError();
    }

    /**
     * Default cluster creation scenario with Optional fields set with Empty values. Click next. Return back and click.
     * next again. Check that all values are present on Summary page. Save cluster.
     * Check the cluster definition trough /definition API.
     */
    @Test
    public void testOptionalfields()
        throws URISyntaxException, AuthenticationException, InterruptedException, IOException {

        // Set the Description value to empty
        sourceCluster.setDescription("");
        // Set the temp location value to empty
        sourceCluster.getLocation(ClusterLocationType.TEMP).setPath("");
        // Now fill the form with the above values for optional fields
        clusterSetup.fillForm(sourceCluster);

        clusterSetup.clickNext();
        clusterSetup.clickPrevious();
        clusterSetup.clickNext();

        ClusterMerlin summaryBlock = clusterSetup.getSummary(sourceCluster.getEmptyCluster());
        //summary block should contain the same info as source
        sourceCluster.assertEquals(summaryBlock);
        clusterSetup.clickSave();

        String alertText = clusterSetup.getActiveAlertText();
        Assert.assertEquals(alertText, "falcon/default/Submit successful (cluster) " + sourceCluster.getName());

        //check the same via notifications bar
        clusterSetup.getPageHeader().validateNotificationCountAndCheckLast(1,
                "falcon/default/Submit successful (cluster) " + sourceCluster.getName());

        ClusterMerlin definition = new ClusterMerlin(cluster.getClusterHelper().
                getEntityDefinition(bundles[0].getClusterElement().toString()).getMessage());

        //definition should be the same that the source
        sourceCluster.assertEquals(definition);
    }



    /**
     * Validate alert lifetime.
     */
    @Test
    public void testValidateAlertLifeTime() throws IOException {
        String nonExistent = "/non-existent-directory";
        sourceCluster.getLocation(ClusterLocationType.STAGING).setPath(nonExistent);
        clusterSetup.fillForm(sourceCluster);
        clusterSetup.clickNext();
        clusterSetup.clickSave();
        clusterSetup.validateAlertLifetime();
    }

    /**
     * Populate cluster with properties. Click Edit XML. Change cluster name and
     * description, add registry interface. Check that they were enabled and populated
     * in wizard.
     */
    @Test
    public void testEditXml() {
        clusterSetup.fillForm(sourceCluster);
        //check that registry is empty
        String registryEndpoint = clusterSetup.getInterfaceEndpointValue(Interfacetype.REGISTRY);
        Assert.assertTrue(StringUtils.isEmpty(registryEndpoint), "Registry endpoint should be empty");
        String registryVersion = clusterSetup.getInterfaceVersionValue(Interfacetype.REGISTRY);
        Assert.assertTrue(StringUtils.isEmpty(registryVersion), "Registry version should be empty");
        Assert.assertFalse(clusterSetup.isRegistryEnabled(), "Registry should be disabled.");

        //change cluster xml
        sourceCluster.setName(sourceCluster.getName() + "-new");
        sourceCluster.setDescription("newDescription");
        Interface iFace = new Interface();
        iFace.setEndpoint(cluster.getClusterHelper().getHostname());
        iFace.setVersion("1.0.0");
        iFace.setType(Interfacetype.REGISTRY);
        sourceCluster.getInterfaces().getInterfaces().add(iFace);

        //populate it to xmlPreview
        clusterSetup.setXmlPreview(sourceCluster.toString());

        //check values on wizard
        registryEndpoint = clusterSetup.getInterfaceEndpointValue(Interfacetype.REGISTRY);
        Assert.assertEquals(registryEndpoint, sourceCluster.getInterfaces().getInterfaces().get(5).getEndpoint(),
            "Registry endpoint on wizard should match to endpoint on preview xml.");
        registryVersion = clusterSetup.getInterfaceVersionValue(Interfacetype.REGISTRY);
        Assert.assertEquals(registryVersion, sourceCluster.getInterfaces().getInterfaces().get(5).getVersion(),
            "Registry version on wizard should match to endpoint on preview xml.");
        Assert.assertTrue(clusterSetup.isRegistryEnabled(), "Registry should be enabled.");
    }

    /**
     * Populate cluster with properties. Click Edit XML. Break the XML (delete closing tag).
     * Check that malformed cluster is not accepted by the form.
     * Undo the change. Change cluster name to malformed one.
     * Check that value is accepted.
     */
    @Test
    public void testEditXmlInvalidValues(){
        clusterSetup.fillForm(sourceCluster);
        ClusterMerlin initialPreview = clusterSetup.getEntityFromXMLPreview();

        //break xml
        String brokenXml = new ClusterMerlin(sourceCluster.toString()).toString();
        brokenXml = brokenXml.substring(0, brokenXml.length() - 3);

        //enter it into xml preview form
        clusterSetup.setXmlPreview(brokenXml);

        //compare preview before and after changes
        ClusterMerlin finalPreview = clusterSetup.getEntityFromXMLPreview();
        Assert.assertEquals(initialPreview, finalPreview, "Broken xml shouldn't be accepted.");

        //change properties to malformed
        sourceCluster.setName("abc123!@#");

        //enter it into xml preview form
        clusterSetup.setXmlPreview(sourceCluster.toString());

        //check the value on a wizard
        Assert.assertEquals(clusterSetup.getName(), sourceCluster.getName(), "Malformed name should be accepted.");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws IOException {
        removeTestClassEntities();
        closeBrowser();
    }
}
