package org.matsim.prepare;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.run.NetworkCleaner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class CreatePopulation {

    public static void main(String[] args) {
        String networkFile = "scenarios/equil/network_Falkirk.xml";
        String populationFile = "scenarios/equil/plans_Falkirk.xml";

        //Load network into scenario
        Config config = ConfigUtils.createConfig();
        config.network().setInputFile(networkFile);
        Scenario scenario = ScenarioUtils.createScenario(config);

        Population population = scenario.getPopulation();
        PopulationFactory factory = population.getFactory();

        //Extract all link id
        List<Id<Link>> allLinksIds = new ArrayList<>(scenario.getNetwork().getLinks().keySet());
        Random random = new Random();

        String[] travelModes = {TransportMode.car, TransportMode.bike,TransportMode.pt};

        //Create a number of agents
        for (int i =0; i<100; i++) {
            Person person = factory.createPerson(Id.createPersonId(i));
            Plan plan = factory.createPlan();


            //Random home and work links
            Id<Link> homeLinkId = allLinksIds.get(random.nextInt(allLinksIds.size()));
            Id<Link> workLinkId;
            do {
                workLinkId = allLinksIds.get(random.nextInt(allLinksIds.size()));
            } while (workLinkId.equals(homeLinkId));

            //Randomly assign mode
            String mode = travelModes[random.nextInt(travelModes.length)];

            //Home activity
            Activity home = factory.createActivityFromLinkId("home", homeLinkId);
            home.setEndTime(8 * 3600 + random.nextInt(1800));
            plan.addActivity(home);

            Leg legToWork = factory.createLeg(mode);
            plan.addLeg(legToWork);

            //Work activity
            Activity work = factory.createActivityFromLinkId("work", workLinkId);
            work.setEndTime(17 * 3600);
            plan.addActivity(home);

            Leg legToHome = factory.createLeg(mode);
            plan.addLeg(legToHome);

            //Home activity
            Activity homeReturn = factory.createActivityFromLinkId("home", homeLinkId);
            plan.addActivity(homeReturn);

            person.addPlan(plan);
            population.addPerson(person);

        }

        new PopulationWriter(population).write(populationFile);
        System.out.println("Randomized population writen to:" + populationFile);
//        Network network = scenario.getNetwork();

    }
}
