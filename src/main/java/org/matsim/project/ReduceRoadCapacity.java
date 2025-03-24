package org.matsim.project;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.io.IOUtils;


public class ReduceRoadCapacity {
    public static void main(String [] args){
        String inputnetwork = "C:/Users/emerald/IdeaProjects/matsim-example-project/scenarios/equil/network.xml";
        String outputnetwork = "C:/Users/emerald/IdeaProjects/matsim-example-project/scenarios/equil/modified_network.xml";

        Network network = NetworkUtils.readNetwork(inputnetwork);

        for (Link link: network.getLinks().values()) {
            if(link.getId().toString().equals("6")){
                link.setFreespeed(link.getFreespeed() * 0.5);
                link.setCapacity(link.getCapacity()*0.7);
            }

        }

        NetworkUtils.writeNetwork(network, outputnetwork);
    }

}
