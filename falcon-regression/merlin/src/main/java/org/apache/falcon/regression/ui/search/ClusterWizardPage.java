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

package org.apache.falcon.regression.ui.search;

import org.apache.commons.lang.StringUtils;
import org.apache.falcon.entity.v0.cluster.ACL;
import org.apache.falcon.entity.v0.cluster.ClusterLocationType;
import org.apache.falcon.entity.v0.cluster.Interface;
import org.apache.falcon.entity.v0.cluster.Interfacetype;
import org.apache.falcon.entity.v0.cluster.Location;
import org.apache.falcon.entity.v0.cluster.Property;
import org.apache.falcon.regression.Entities.ClusterMerlin;
import org.apache.falcon.regression.core.util.UIAssert;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.testng.Assert;

import java.util.List;

/** Page object of the Cluster creation page. */
public class ClusterWizardPage extends EntityWizardPage {
    private static final Logger LOGGER = Logger.getLogger(ClusterWizardPage.class);
    @FindBys({
        @FindBy(className = "mainUIView"),
        @FindBy(className = "clusterForm")
    })
    private WebElement clusterBox;
    @FindBy(id = "clusterAdvancedOption")
    private WebElement clusterAdvancedOption;
    @FindBy(id = "cluster.step1")
    private WebElement next;
    @FindBy(id = "cluster.step2")
    private WebElement save;
    @FindBy(id = "cluster.backToStep1")
    private WebElement previous;
    @FindBy(xpath = "//a[contains(text(), 'Cancel')]")
    private WebElement cancel;
    @FindBy(xpath = "//div[contains(@class, 'clusterSummaryRow')]")
    private WebElement summaryBox;

    public ClusterWizardPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public void checkPage() {
        UIAssert.assertDisplayed(clusterBox, "Cluster box");
    }

    /**
     * Fills cluster setup forms with values retrieved from Filling object.
     */
    public void fillForm(ClusterMerlin cluster) {
        clusterAdvancedOption.click();
        setName(cluster.getName());
        setColo(cluster.getColo());
        setDescription(cluster.getDescription());
        setTags(cluster.getTags());
        ACL acl = cluster.getACL();
        setOwner(acl.getOwner());
        setGroup(acl.getGroup());
        setPermissions(acl.getPermission());
        for(Interface iface : cluster.getInterfaces().getInterfaces()) {
            setInterface(iface);
        }
        for (Property property : cluster.getProperties().getProperties()) {
            addProperty(property.getName(), property.getValue());
        }
        setLocations(cluster.getLocations().getLocations());
        waitForAngularToFinish();
    }

    /**
     * Methods to fill specific wizard fields.
     */
    public void setName(String name) {
        WebElement nameInput = getNameInput();
        nameInput.clear();
        sendKeysSlowly(nameInput, name);
    }

    public String getName() {
        return getNameInput().getAttribute("value");
    }

    private WebElement getNameInput() {
        return driver.findElement(By.xpath("//div[label[text()='Cluster Name']]/input"));
    }

    public void setColo(String colo) {
        WebElement coloInput = clusterBox
          .findElement(By.xpath("//div[label[text()='Data Center or Colo Name']]/input"));
        coloInput.clear();
        sendKeysSlowly(coloInput, colo);
    }

    public void setDescription(String description) {
        WebElement descriptionInput = clusterBox
           .findElement(By.xpath("//div[label[text()='Description']]/input"));
        descriptionInput.clear();
        sendKeysSlowly(descriptionInput, description);
    }

    public void setOwner(String owner) {
        WebElement ownerInput = clusterBox.findElement(By.xpath("//div[label[text()='Owner']]/input"));
        ownerInput.clear();
        sendKeysSlowly(ownerInput, owner);
    }

    public void setGroup(String group) {
        WebElement groupInput = clusterBox.findElement(By.xpath("//div[label[text()='Group']]/input"));
        groupInput.clear();
        sendKeysSlowly(groupInput, group);
    }

    /**
     * Common method to fill interfaces.
     */
    public void setInterface(Interface iface) {
        WebElement ifaceEndpoint = clusterBox.findElement(By.id("interface." + iface.getType().value()));
        ifaceEndpoint.clear();
        sendKeysSlowly(ifaceEndpoint, iface.getEndpoint());
    }

    /**
     * Populates form with tags.
     */
    public void setTags(String tagsStr){
        if (!StringUtils.isEmpty(tagsStr)) {
            String [] tags = tagsStr.split(",");
            for (int i = 0; i < tags.length; i++) {
                if (i > 0){
                    clickAddTag();
                }
                String key = tags[i].trim().split("=")[0];
                String value = tags[i].trim().split("=")[1];
                addTag(key, value);
            }
        }
    }

    /**
     * Populates last (empty) tag and value fields and creates new fields.
     * @param key tag key
     * @param value tag value
     */
    public void addTag(String key, String value) {
        List<WebElement> tagInputs = clusterBox.findElements(By.xpath("//input[@ng-model='tag.key']"));
        List<WebElement> valueInputs = clusterBox.findElements(By.xpath("//input[@ng-model='tag.value']"));
        WebElement tagInput = tagInputs.get(tagInputs.size() - 1);
        sendKeysSlowly(tagInput, key);
        WebElement valueInput = valueInputs.get(valueInputs.size() - 1);
        sendKeysSlowly(valueInput, value);
    }

    public void clickAddTag() {
        clusterBox.findElement(By.xpath("//button[contains(., 'add tag')]")).click();
    }

    public void clickDeleteTag() {
        List<WebElement> buttons = clusterBox
            .findElements(By.xpath("//div[@class='row dynamic-table-spacer ng-scope']//button[contains(.,'delete')]"));
        buttons.get(buttons.size() - 1).click();
    }

    /**
     * Fills property fields and creates new empty property fields.
     */
    public void addProperty(String name, String value) {
        List<WebElement> propInputs = clusterBox.findElements(By.xpath("//input[@ng-model='property._name']"));
        List<WebElement> valueInputs = clusterBox.findElements(By.xpath("//input[@ng-model='property._value']"));
        WebElement propInput = propInputs.get(propInputs.size()-1);
        sendKeysSlowly(propInput, name);
        WebElement valueInput = valueInputs.get(valueInputs.size() - 1);
        sendKeysSlowly(valueInput, value);
        clickAddProperty();
    }

    public void clickAddProperty() {
        clusterBox.findElement(By.xpath("//button[contains(., 'add property')]")).click();
    }

    /**
     * Method to set locations. Location can be only one of ClusterLocationType. So adding new location only after
     * respective compulsory location was filled.
     * @param locations locations
     */
    public void setLocations(List<Location> locations) {
        boolean staging = false, temp = false, working = false;
        for(Location location : locations) {
            WebElement pathInput = null;
            if (location.getName() == ClusterLocationType.STAGING && !staging) {
                pathInput = clusterBox.findElement(By.id("location.staging"));
                staging = true;
            } else  if (location.getName() == ClusterLocationType.TEMP && !temp) {
                pathInput = clusterBox.findElement(By.id("location.temp"));
                temp = true;
            } else if (location.getName() == ClusterLocationType.WORKING && !working) {
                pathInput = clusterBox.findElement(By.id("location.working"));
                working = true;
            } else {
                fillAdditionalLocation(location);
            }
            if (pathInput != null) {
                pathInput.clear();
                sendKeysSlowly(pathInput, location.getPath());
            }
        }
    }

    /**
     * Method populates last location fields with values and creates new fields.
     */
    public void fillAdditionalLocation(Location location) {
        List<WebElement> allNameInputs = clusterBox
            .findElements(By.xpath("//input[contains(@ng-model, 'location._name')]"));
        sendKeysSlowly(allNameInputs.get(allNameInputs.size() - 1), location.getName().value());
        List<WebElement> allPathInputs = clusterBox
            .findElements(By.xpath("//input[contains(@ng-model, 'location._path')]"));
        sendKeysSlowly(allPathInputs.get(allPathInputs.size() - 1), location.getPath());
    }

    public void clickAddLocation() {
        clusterBox.findElement(By.xpath("//button[@ng-click='addLocation()']")).click();
    }

    public void clickDeleteLocation() {
        List<WebElement> buttons = clusterBox
            .findElements(By.xpath("//div[@class='row ng-scope']//button[contains(.,'delete')]"));
        Assert.assertFalse(buttons.isEmpty(), "Delete button should be present.");
        buttons.get(0).click();
    }

    public boolean checkElementByContent(String elementTag, String content) {
        List<WebElement> elements = clusterBox.findElements(By.xpath("//" + elementTag));
        for(WebElement element : elements) {
            if (element.getAttribute("value").equals(content)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to assert the staging and Working location are same.
     */
    public void assertLocationsEqualError(){

        // Assertion for Staging Location.
        LOGGER.info(" Assertion for Staging Directory ");
        Assert.assertTrue(checkErrorMessageByElement("input[contains(@id,'location.staging')]//following-sibling::"
                + "span[contains(@ng-show, 'locationsEqualError')]",
                "Staging and Working location should be different"));

        // Assertion for Working Location.
        LOGGER.info("Assertion for Working Directory");
        Assert.assertTrue(checkErrorMessageByElement("input[contains(@id,'location.working')]//following-sibling::"
                + "span[contains(@ng-show, 'locationsEqualError')]",
                "Staging and Working location should be different"));
    }

    /**
     * Method to get the Error text message displayed based on Xpath and compares.
     * with the input string paramater : errMessage
     * @param elementTag elementTag
     * @param errMessage errMessage
     */
    public boolean checkErrorMessageByElement(String elementTag, String errMessage) {

        List<WebElement> elements = clusterBox.findElements(By.xpath("//" + elementTag));
        if (!elements.isEmpty()){
            for (WebElement element : elements) {
                Assert.assertEquals(element.getText(), errMessage);
                LOGGER.info("Error Message Displayed : " + element.getText());
            }
            return true;
        }else{
            LOGGER.info(" No Elements found with the xpath " + elementTag);
            return false;
        }
    }

    /**
     * Retrieves the value of the summary box and parses it to cluster properties.
     * @param draft empty cluster to contain all properties.
     * @return cluster filled with properties from the summary.
     */
    public ClusterMerlin getSummary(ClusterMerlin draft) {
        ClusterMerlin cluster = new ClusterMerlin(draft.toString());
        cluster.setName(summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterName']")).getText());
        cluster.setColo(summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterColo']")).getText());
        cluster.setDescription(summaryBox
                .findElement(By.xpath("//span[@data-qe-id='clusterDescription']")).getText());
        cluster.setTags(summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterTags']")).getText());
        cluster.getACL().setOwner(summaryBox
                .findElement(By.xpath("//span[@data-qe-id='clusterACLOwner']")).getText());
        cluster.getACL().setGroup(summaryBox
                .findElement(By.xpath("//span[@data-qe-id='clusterACLGroup']")).getText());
        cluster.getACL().setPermission(summaryBox
                .findElement(By.xpath("//span[@data-qe-id='clusterACLPermission']")).getText());

        cluster.addInterface(Interfacetype.READONLY,
                summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterInterfacereadonly']")).getText(),
                summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterInterfacereadonlyVersion']")).getText());
        cluster.addInterface(Interfacetype.WRITE,
                summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterInterfacewrite']")).getText(),
                summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterInterfacewriteVersion']")).getText());
        cluster.addInterface(Interfacetype.EXECUTE,
                summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterInterfaceexecute']")).getText(),
                summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterInterfaceexecuteVersion']")).getText());
        cluster.addInterface(Interfacetype.WORKFLOW,
                summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterInterfaceworkflow']")).getText(),
                summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterInterfaceworkflowVersion']")).getText());
        cluster.addInterface(Interfacetype.MESSAGING,
                summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterInterfacemessaging']")).getText(),
                summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterInterfacemessagingVersion']")).getText());
        //cluster.addInterface(Interfacetype.REGISTRY,
        //summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterInterfaceregistry']")).getText(),
        //summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterInterfaceregistryVersion']")).getText());

        cluster.addLocation(ClusterLocationType.STAGING,
                summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterLocationStaging']")).getText());
        cluster.addLocation(ClusterLocationType.TEMP,
                summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterLocationTemp']")).getText());
        cluster.addLocation(ClusterLocationType.WORKING,
                summaryBox.findElement(By.xpath("//span[@data-qe-id='clusterLocationWorking']")).getText());

        List<WebElement> clusterProperties = summaryBox.findElements(By.xpath("//div[@data-qe-id='clusterProps']"));
        for (int i = 0; i < clusterProperties.size(); i++) {
            cluster.withProperty(summaryBox.findElements(By.xpath("//div[@data-qe-id='clusterProps']")).get(i)
                .findElement(By.tagName("label")).getText(),
                summaryBox.findElements(By.xpath("//div[@data-qe-id='clusterProps']")).get(i)
                .findElement(By.tagName("span")).getText());
        }


        return cluster;
    }

    /**
     * Clicks on cancel button.
     */
    public void cancel() {
        cancel.click();
    }

    /**
     *  Click on next button which is the same as finish step 1.
     */
    public void clickNext() {
        next.click();
        waitForAngularToFinish();
        Assert.assertTrue(summaryBox.isDisplayed(), "Summary box should be displayed.");
    }

    /**
     *  Click on next button in the cluster creation page.
     */
    public void clickJustNext() {
        next.click();
        waitForAngularToFinish();
    }

    /**
     * Click on save button.
     */
    public void clickSave() {
        save.click();
        waitForAlert();
    }

    /**
     * Clicks on previous button.
     */
    public void clickPrevious() {
        previous.click();
        waitForAngularToFinish();
        UIAssert.assertDisplayed(clusterBox, "Cluster box");
    }

    /**
     * Method imitates click on check box.
     * @param expectedState whether check box is expected to be enabled or not after click.
     */
    public void checkRegistry(boolean expectedState) {
        WebElement checkbox = clusterBox.findElement(By.xpath("//input[@type='checkbox']"));
        clickCheckBoxSecurely(checkbox, expectedState);
        waitForAngularToFinish();
    }

    public WebElement getInterfaceEndpoint(Interfacetype interfacetype) {
        return clusterBox.findElement(By.id("interface." + interfacetype));
    }

    public String getInterfaceEndpointValue(Interfacetype interfacetype) {
        return getInterfaceEndpoint(interfacetype).getAttribute("value");
    }

    /**
     * Method preventing the NullPointerException.
     */
    public String getValueFromSlices(String[] slices, String line) {
        String trimValue;
        if (slices[0].length()==(line.length())) {
            trimValue = "";
        }else {
            trimValue = slices[1].trim();
        }
        return trimValue;
    }

    /**
     * Checks whether registry interface is enabled for input or not.
     */
    public boolean isRegistryEnabled() {
        return getInterfaceEndpoint(Interfacetype.REGISTRY).isEnabled();
    }

    private WebElement getNameUnavailable(){
        return clusterBox.findElement(By.xpath(
            "//label[contains(@class, 'nameValidationMessage ') and contains(@class, 'custom-danger')]"));
    }

    public void checkNameUnavailableDisplayed(boolean isDisplayed) {
        if (isDisplayed){
            UIAssert.assertDisplayed(getNameUnavailable(), "Name Unavailable not displayed");
        }else {
            try{
                getNameUnavailable();
                Assert.fail("Name Unavailable found");
            } catch (Exception ex){
                LOGGER.info("Name Unavailable not found");
            }
        }
    }

    @Override
    public WebElement getEditXMLButton() {
        return driver.findElement(By.id("cluster.editXML"));
    }

    @Override
    public WebElement getRevertXMLButton() {
        return driver.findElement(By.id("revertXMLBtn"));
    }

    @Override
    public ClusterMerlin getEntityFromXMLPreview() {
        return new ClusterMerlin(getXMLPreview());
    }
}
