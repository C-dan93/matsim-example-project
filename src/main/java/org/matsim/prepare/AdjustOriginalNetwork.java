package org.matsim.prepare;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;

import java.util.HashSet;
import java.util.Set;

public class AdjustOriginalNetwork {

    public static void main(String[] args) {
        String inputNetwork = "scenarios/equil/network.xml";
        String outputNetwork = "scenarios/equil/network_restricted.xml";

        Network network = NetworkUtils.readNetwork(inputNetwork);

        for (Link link : network.getLinks().values()) {
            String linkId = link.getId().toString();

            if (linkId.equals("6") || linkId.equals("7")) {
                // Restrict to bike and pedelec only, reduce speed and capacity to discourage car usage
                link.setFreespeed(link.getFreespeed() * 0.4);
                link.setCapacity(link.getCapacity() * 0.5);
                Set<String> allowed = new HashSet<>();
                allowed.add("bike");
                allowed.add("pedelec");
                link.setAllowedModes(allowed);
            } else {
                // Default: allow all major modes
                Set<String> allModes = new HashSet<>();
                allModes.add("car");
                allModes.add("bike");
                allModes.add("pedelec");
                link.setAllowedModes(allModes);
            }
        }

        NetworkUtils.writeNetwork(network, outputNetwork);
    }
}
