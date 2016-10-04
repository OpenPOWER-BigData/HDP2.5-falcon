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

package org.apache.falcon.regression.Entities;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.falcon.entity.v0.EntityType;
import org.apache.falcon.entity.v0.Frequency;
import org.apache.falcon.entity.v0.feed.ACL;
import org.apache.falcon.entity.v0.feed.ActionType;
import org.apache.falcon.entity.v0.feed.CatalogTable;
import org.apache.falcon.entity.v0.feed.Cluster;
import org.apache.falcon.entity.v0.feed.ClusterType;
import org.apache.falcon.entity.v0.feed.Feed;
import org.apache.falcon.entity.v0.feed.Location;
import org.apache.falcon.entity.v0.feed.LocationType;
import org.apache.falcon.entity.v0.feed.Locations;
import org.apache.falcon.entity.v0.feed.Property;
import org.apache.falcon.entity.v0.feed.Retention;
import org.apache.falcon.entity.v0.feed.Sla;
import org.apache.falcon.entity.v0.feed.Validity;
import org.apache.falcon.regression.core.util.TimeUtil;
import org.apache.falcon.regression.core.util.Util;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import javax.xml.bind.JAXBException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Class for representing a feed xml. */
public class FeedMerlin extends Feed {
    private static final Logger LOGGER = Logger.getLogger(FeedMerlin.class);

    public FeedMerlin(String feedData) {
        this((Feed) TestEntityUtil.fromString(EntityType.FEED, feedData));
    }

    public FeedMerlin(final Feed feed) {
        try {
            PropertyUtils.copyProperties(this, feed);
            this.setACL(feed.getACL());
        } catch (ReflectiveOperationException e) {
            Assert.fail("Can't create FeedMerlin: " + ExceptionUtils.getStackTrace(e));
        }
    }

    public static List<FeedMerlin> fromString(List<String> feedStrings) {
        List<FeedMerlin> feeds = new ArrayList<>();
        for (String feedString : feedStrings) {
            feeds.add(fromString(feedString));
        }
        return feeds;
    }

    public static FeedMerlin fromString(String feedString) {
        return new FeedMerlin(feedString);
    }

    public List<String> getClusterNames() {
        List<String> names = new ArrayList<>();
        for (Cluster cluster : getClusters().getClusters()) {
            names.add(cluster.getName());
        }
        return names;
    }

    /**
     * Add/replace a property.
     * @param name name of the property
     * @param value value of the property
     * @return this
     */
    public FeedMerlin withProperty(String name, String value) {
        final List<Property> properties = getProperties().getProperties();
        //if property with same name exists, just replace the value
        for (Property property : properties) {
            if (property.getName().equals(name)) {
                LOGGER.info(String.format("Overwriting property name = %s oldVal = %s newVal = %s",
                    property.getName(), property.getValue(), value));
                property.setValue(value);
                return this;
            }
        }
        //if property is not added already, add it
        final Property property = new Property();
        property.setName(name);
        property.setValue(value);
        properties.add(property);
        return this;
    }

    /**
     * Return feed path of the specified type.
     * @return feed data path
     * @param locationType the type of the location
     */
    public String getFeedPath(LocationType locationType) {
        for (Location location : this.getLocations().getLocations()) {
            if (location.getType() == locationType) {
                return location.getPath();
            }
        }
        return null;
    }

    /**
     * Sets cut-off period.
     * @param frequency cut-off period
     */
    public FeedMerlin insertLateFeedValue(Frequency frequency) {
        this.getLateArrival().setCutOff(frequency);
        return this;
    }

    /**
     * Sets data location for a feed.
     * @param pathValue new path
     */
    public FeedMerlin setFeedPathValue(String pathValue) {
        for (Location location : this.getLocations().getLocations()) {
            if (location.getType() == LocationType.DATA) {
                location.setPath(pathValue);
            }
        }
        return this;
    }

    /**
     * Sets name for a cluster by given order number.
     * @param clusterName new cluster name
     * @param clusterIndex index of cluster which should be updated
     */
    public FeedMerlin setClusterNameInFeed(String clusterName, int clusterIndex) {
        this.getClusters().getClusters().get(clusterIndex).setName(clusterName);
        return this;
    }

    /** clear clusters of this feed. */
    public FeedMerlin clearFeedClusters() {
        getClusters().getClusters().clear();
        return this;
    }

    /** add a feed cluster to this feed. */
    public FeedMerlin addFeedCluster(Cluster cluster) {
        getClusters().getClusters().add(cluster);
        return this;
    }

    /** Fluent builder wrapper for cluster fragment of feed entity . */
    public static class FeedClusterBuilder {
        private Cluster cluster = new Cluster();

        public FeedClusterBuilder(String clusterName) {
            cluster.setName(clusterName);
        }

        public Cluster build() {
            Cluster retVal = cluster;
            cluster = null;
            return retVal;
        }

        public FeedClusterBuilder withRetention(String limit, ActionType action) {
            Retention r = new Retention();
            r.setLimit(new Frequency(limit));
            r.setAction(action);
            cluster.setRetention(r);
            return this;
        }

        public FeedClusterBuilder withValidity(String startTime, String endTime) {
            Validity v = new Validity();
            v.setStart(TimeUtil.oozieDateToDate(startTime).toDate());
            v.setEnd(TimeUtil.oozieDateToDate(endTime).toDate());
            cluster.setValidity(v);
            return this;
        }

        public FeedClusterBuilder withClusterType(ClusterType type) {
            cluster.setType(type);
            return this;
        }

        public FeedClusterBuilder withPartition(String partition) {
            cluster.setPartition(partition);
            return this;
        }

        public FeedClusterBuilder withTableUri(String tableUri) {
            CatalogTable catalogTable = new CatalogTable();
            catalogTable.setUri(tableUri);
            cluster.setTable(catalogTable);
            return this;
        }

        public FeedClusterBuilder withDataLocation(String dataLocation) {
            Location oneLocation = new Location();
            oneLocation.setPath(dataLocation);
            oneLocation.setType(LocationType.DATA);

            Locations feedLocations = new Locations();
            feedLocations.getLocations().add(oneLocation);
            cluster.setLocations(feedLocations);
            return this;
        }

        public FeedClusterBuilder withDelay(Frequency frequency) {
            cluster.setDelay(frequency);
            return this;
        }


    }

    /**
     * Method sets a number of clusters to feed definition.
     *
     * @param newClusters list of definitions of clusters which are to be set to feed
     * @param location location of data on every cluster
     * @param startTime start of feed validity on every cluster
     * @param endTime end of feed validity on every cluster
     */
    public void setFeedClusters(List<String> newClusters, String location, String startTime,
                                String endTime) {
        clearFeedClusters();
        setFrequency(new Frequency("" + 5, Frequency.TimeUnit.minutes));

        for (String newCluster : newClusters) {
            Cluster feedCluster = new FeedClusterBuilder(new ClusterMerlin(newCluster).getName())
                .withDataLocation(location + "/${YEAR}/${MONTH}/${DAY}/${HOUR}/${MINUTE}")
                .withValidity(TimeUtil.addMinsToTime(startTime, -180),
                    TimeUtil.addMinsToTime(endTime, 180))
                .withRetention("hours(20)", ActionType.DELETE)
                .build();
            addFeedCluster(feedCluster);
        }
    }

    public void setRetentionValue(String retentionValue) {
        for (org.apache.falcon.entity.v0.feed.Cluster cluster : getClusters().getClusters()) {
            cluster.getRetention().setLimit(new Frequency(retentionValue));
        }
    }

    public void setTableValue(String dBName, String tableName, String pathValue) {
        getTable().setUri("catalog:" + dBName + ":" + tableName + "#" + pathValue);
    }

    @Override
    public String toString() {
        try {
            StringWriter sw = new StringWriter();
            EntityType.FEED.getMarshaller().marshal(this, sw);
            return sw.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLocation(LocationType locationType, String feedInputPath) {
        for (Location location : getLocations().getLocations()) {
            if (location.getType() == locationType) {
                location.setPath(feedInputPath);
            }
        }
    }

    /**
     * Sets unique names for the feed.
     * @return mapping of old name to new name
     * @param prefix prefix of new name
     */
    public Map<? extends String, ? extends String> setUniqueName(String prefix) {
        final String oldName = getName();
        final String newName = TestEntityUtil.generateUniqueName(prefix, oldName);
        setName(newName);
        final HashMap<String, String> nameMap = new HashMap<>(1);
        nameMap.put(oldName, newName);
        return nameMap;
    }

    public void renameClusters(Map<String, String> clusterNameMap) {
        for (Cluster cluster : getClusters().getClusters()) {
            final String oldName = cluster.getName();
            final String newName = clusterNameMap.get(oldName);
            if (!StringUtils.isEmpty(newName)) {
                cluster.setName(newName);
            }
        }
    }

    /**
     * Set ACL.
     */
    public void setACL(String owner, String group, String permission) {
        ACL acl = new ACL();
        acl.setOwner(owner);
        acl.setGroup(group);
        acl.setPermission(permission);
        this.setACL(acl);
    }

    /**
     * Sel SLA.
     * @param slaLow : low value of SLA
     * @param slaHigh : high value of SLA
     */

    public void setSla(Frequency slaLow, Frequency slaHigh) {
        Sla sla = new Sla();
        sla.setSlaLow(slaLow);
        sla.setSlaHigh(slaHigh);
        this.setSla(sla);
    }

    /**
     * Sets new feed data path (for first location).
     *
     * @param path new feed data path
     */
    public void setFilePath(String path) {
        getLocations().getLocations().get(0).setPath(path);
    }


    /**
     * Retrieves prefix (main sub-folders) of first feed data path.
     */
    public String getFeedPrefix() {
        String path = getLocations().getLocations().get(0).getPath();
        return path.substring(0, path.indexOf('$'));
    }

    public void setValidity(String feedStart, String feedEnd) {
        this.getClusters().getClusters().get(0).getValidity()
            .setStart(TimeUtil.oozieDateToDate(feedStart).toDate());
        this.getClusters().getClusters().get(0).getValidity()
            .setEnd(TimeUtil.oozieDateToDate(feedEnd).toDate());

    }

    public void setDataLocationPath(String path) {
        final List<Location> locations = this.getLocations().getLocations();
        for (Location location : locations) {
            if (location.getType() == LocationType.DATA) {
                location.setPath(path);
            }
        }
    }

    public void setPeriodicity(int frequency, Frequency.TimeUnit periodicity) {
        Frequency frq = new Frequency(String.valueOf(frequency), periodicity);
        this.setFrequency(frq);
    }

    public void setTableUri(String tableUri) {
        final CatalogTable catalogTable = new CatalogTable();
        catalogTable.setUri(tableUri);
        this.setTable(catalogTable);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.FEED;
    }

    public void assertGeneralProperties(FeedMerlin newFeed){

        LOGGER.info(String.format("Comparing General Properties: source: %n%s%n and feed: %n%n%s",
            Util.prettyPrintXml(toString()), Util.prettyPrintXml(newFeed.toString())));

        SoftAssert softAssert = new SoftAssert();

        // Assert all the the General Properties
        softAssert.assertEquals(newFeed.getName(), getName(),
            "Feed Name is different");
        softAssert.assertEquals(newFeed.getDescription(), getDescription(),
            "Feed Description is different");
        softAssert.assertEquals(newFeed.getTags(), getTags(),
            "Feed Tags is different");
        softAssert.assertEquals(newFeed.getGroups(), getGroups(),
            "Feed Groups is different");
        softAssert.assertEquals(newFeed.getACL().getOwner(), getACL().getOwner(),
            "Feed ACL Owner is different");
        softAssert.assertEquals(newFeed.getACL().getGroup(), getACL().getGroup(),
            "Feed ACL Group is different");
        softAssert.assertEquals(newFeed.getACL().getPermission(), getACL().getPermission(),
            "Feed ACL Permission is different");
        softAssert.assertEquals(newFeed.getSchema().getLocation(), getSchema().getLocation(),
            "Feed Schema Location is different");
        softAssert.assertEquals(newFeed.getSchema().getProvider(), getSchema().getProvider(),
            "Feed Schema Provider is different");
        softAssert.assertAll();

    }

    public void assertPropertiesInfo(FeedMerlin newFeed){

        LOGGER.info(String.format("Comparing Properties Info: source: %n%s%n and feed: %n%n%s",
            Util.prettyPrintXml(toString()), Util.prettyPrintXml(newFeed.toString())));

        SoftAssert softAssert = new SoftAssert();

        // Assert all the Properties Info
        softAssert.assertEquals(newFeed.getFrequency().getFrequency(),
            getFrequency().getFrequency(),
            "Feed Frequency is different");
        softAssert.assertEquals(newFeed.getFrequency().getTimeUnit().toString(),
            getFrequency().getTimeUnit().toString(),
            "Feed Frequency Unit is different");
        softAssert.assertEquals(newFeed.getLateArrival().getCutOff().getFrequencyAsInt(),
            getLateArrival().getCutOff().getFrequencyAsInt(),
            "Feed CutOff is different");
        softAssert.assertEquals(newFeed.getLateArrival().getCutOff().getTimeUnit(),
            getLateArrival().getCutOff().getTimeUnit(),
            "Feed CutOff Unit is different");
        softAssert.assertEquals(newFeed.getAvailabilityFlag(),
            getAvailabilityFlag(),
            "Feed Availability Flag is different");

        softAssert.assertAll();
    }

    public void assertLocationInfo(FeedMerlin newFeed){

        LOGGER.info(String.format("Comparing Location Info: source: %n%s%n and feed: %n%n%s",
            Util.prettyPrintXml(toString()), Util.prettyPrintXml(newFeed.toString())));

        SoftAssert softAssert = new SoftAssert();

        // Assert all the Location Properties
        softAssert.assertEquals(newFeed.getLocations().getLocations().get(0).getPath(),
            getLocations().getLocations().get(0).getPath(),
            "Feed Location Data Path is different");
        softAssert.assertEquals(newFeed.getLocations().getLocations().get(1).getPath(),
            getLocations().getLocations().get(1).getPath(),
            "Feed Location Stats Path is different");
        softAssert.assertEquals(newFeed.getLocations().getLocations().get(2).getPath(),
            getLocations().getLocations().get(2).getPath(),
            "Feed Location Meta Path is different");

        softAssert.assertAll();

    }

    public void assertClusterInfo(FeedMerlin newFeed){

        LOGGER.info(String.format("Comparing Feed Cluster Info: source: %n%s%n and feed: %n%n%s",
            Util.prettyPrintXml(toString()), Util.prettyPrintXml(newFeed.toString())));

        SoftAssert softAssert = new SoftAssert();

        // Assert all the Cluster Properties
        softAssert.assertEquals(newFeed.getClusters().getClusters().get(0)
                .getName(),
            getClusters().getClusters().get(0).getName(),
            "Feed Cluster Name is different");
        softAssert.assertEquals(newFeed.getClusters().getClusters().get(0)
                .getLocations().getLocations().get(0).getPath(),
            getLocations().getLocations().get(0).getPath(),
            "Feed Cluster Data Path is different");
        softAssert.assertEquals(newFeed.getClusters().getClusters().get(0)
                .getLocations().getLocations().get(1).getPath(),
            getLocations().getLocations().get(1).getPath(),
            "Feed Cluster Stats Path is different");
        softAssert.assertEquals(newFeed.getClusters().getClusters().get(0)
                .getLocations().getLocations().get(2).getPath(),
            getLocations().getLocations().get(2).getPath(),
            "Feed Cluster Meta Path is different");
        softAssert.assertEquals(newFeed.getClusters().getClusters().get(0)
                .getValidity().getStart(),
            getClusters().getClusters().get(0).getValidity().getStart(),
            "Feed Cluster Start Date is different");
        softAssert.assertEquals(newFeed.getClusters().getClusters().get(0)
                .getValidity().getEnd(),
            getClusters().getClusters().get(0).getValidity().getEnd(),
            "Feed Cluster End Date is different");
        // Asserting on hardcoded value of 99, due to UI bug which only support till two digits.
        softAssert.assertEquals(newFeed.getClusters().getClusters().get(0)
                .getRetention().getLimit().getFrequency(), "99",
            "Feed Retention is different");
        softAssert.assertEquals(newFeed.getClusters().getClusters().get(0)
                .getRetention().getLimit().getTimeUnit().name(),
            getClusters().getClusters().get(0).getRetention().getLimit().getTimeUnit().name(),
            "Feed Retention Unit is different");

        softAssert.assertAll();

    }

    public void assertEquals(FeedMerlin newFeed) {

        assertGeneralProperties(newFeed);
        assertPropertiesInfo(newFeed);
        assertLocationInfo(newFeed);
        assertClusterInfo(newFeed);
    }



}
