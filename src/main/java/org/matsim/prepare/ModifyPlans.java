package org.matsim.prepare;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ModifyPlans {

    public static void main(String[] args) {
        // 1. Load config WITH NETWORK
        Config config = ConfigUtils.createConfig();
        config.plans().setInputFile("scenarios/equil/plans100.xml");
        config.network().setInputFile("scenarios/equil/network.xml"); // Critical addition

        // 2. Load scenario (now with network)
        Scenario scenario = ScenarioUtils.loadScenario(config);

        // 3. Initialize attributes
//        ObjectAttributes attributes = new ObjectAttributes();
        Random random = new Random(1234);



        // 4. Mode options
        List<String> modes = Arrays.asList(TransportMode.car, "bike", "pedelec");

//        // 5. Valid LINK sequences (not node sequences)
        List<String> validLinkRoutes = Arrays.asList(
                "1 2",    // Link IDs for 1→2→3
                "6 15",   // Link IDs for 2→7→12
                "20 21"   // Link IDs for 12→13→14
        );

        // 6. Modify plans
        for (Person person : scenario.getPopulation().getPersons().values()) {
            Plan plan = person.getPlans().get(0);

            // 6a. Random mode assignment
            String mode = modes.get(random.nextInt(modes.size()));
            for (PlanElement pe : plan.getPlanElements()) {
                if (pe instanceof Leg) {
                    ((Leg) pe).setMode(mode);
                }
            }

            // 6b. Departure time adjustment
            Activity firstAct = (Activity) plan.getPlanElements().get(0);
            if(firstAct.getEndTime().isDefined()) {
                double currentEndTime = firstAct.getEndTime().seconds();
                double newEndTime = Math.max(0, currentEndTime + (random.nextDouble() * 3600 - 1800));
                firstAct.setEndTime(newEndTime);
            }
            // 6c. Update routes (using LINK IDs)
            if (mode.equals(TransportMode.car)) {
                Route route = ((Leg) plan.getPlanElements().get(1)).getRoute();
                if (route != null) {
                    route.setRouteDescription(validLinkRoutes.get(random.nextInt(validLinkRoutes.size())));
                }
            }

            // 6d. Add income attribute
            person.getAttributes().putAttribute( "income", random.nextDouble() > 0.5 ? "high" : "low");
        }

        // 7. Save outputs
        new PopulationWriter(scenario.getPopulation()).write("scenarios/equil/plans100_diverse.xml");
//        new ObjectAttributesXmlWriter(attributes).writeFile("scenarios/equil/person_attributes.xml");

        System.out.println("Successfully created:");
        System.out.println("- Modified plans: plans100_diverse.xml");
        System.out.println("- Person attributes: person_attributes.xml");
    }
}