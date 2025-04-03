package org.matsim.prepare;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.ScoringConfigGroup;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.api.core.v01.Scenario;

import java.util.HashSet;
import java.util.Set;

public class AdjustOriginalNetworkToll_By_Scoring {

    public static void main(String[] args) {
        String inputNetwork = "scenarios/equil/network.xml";
        String outputNetwork = "scenarios/equil/network_penalized_car.xml";

        Config config = ConfigUtils.createConfig();
        config.network().setInputFile(inputNetwork);

        // Penalize car mode through scoring instead of XML
        ScoringConfigGroup scoring = config.scoring();

        ScoringConfigGroup.ModeParams carParams = new ScoringConfigGroup.ModeParams("car");
        carParams.setConstant(-5.0);
        carParams.setMarginalUtilityOfTraveling(-6.0);
        scoring.addModeParams(carParams);

        ScoringConfigGroup.ModeParams bikeParams = new ScoringConfigGroup.ModeParams("bike");
        bikeParams.setConstant(0.0);
        bikeParams.setMarginalUtilityOfTraveling(0.0);
        scoring.addModeParams(bikeParams);

        ScoringConfigGroup.ModeParams pedelecParams = new ScoringConfigGroup.ModeParams("pedelec");
        pedelecParams.setConstant(0.0);
        pedelecParams.setMarginalUtilityOfTraveling(0.0);
        scoring.addModeParams(pedelecParams);

        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();

        for (Link link : network.getLinks().values()) {
            Set<String> allowed = new HashSet<>();
            allowed.add("car");
            allowed.add("bike");
            allowed.add("pedelec");
            link.setAllowedModes(allowed);
        }

        NetworkUtils.writeNetwork(network, outputNetwork);
    }
}
