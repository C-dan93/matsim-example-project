package org.matsim.prepare;


import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.osm.networkReader.SupersonicOsmNetworkReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;

import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.geotools.gml3.GML.direction;

public class OSMToNetworkConverter {

    public static void main (String[] args) {

        String osmFile = "original-input-data/map.osm";
        String outputNetworkFile = "output/network_Falkirk.xml";


        CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation(
                TransformationFactory.WGS84, "EPSG:27700");

        Network network = NetworkUtils.createNetwork();

        ExecutorService executor = Executors.newWorkStealingPool();

        new SupersonicOsmNetworkReader.Builder()
                .setCoordinateTransformation(transformation)
                .setIncludeLinkAtCoordWithHierarchy((coord,hierarchy) -> true)
                .setAfterLinkCreated((link,tags, direction) -> {})
                .build()
                .read(Paths.get(osmFile));
        new NetworkWriter(network).write(outputNetworkFile);

        executor.shutdown();

        System.out.println("network created and saved to:" + outputNetworkFile);
    }
}
