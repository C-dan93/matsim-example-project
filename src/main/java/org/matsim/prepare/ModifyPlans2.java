package org.matsim.prepare;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.population.io.PopulationWriter;
import java.util.Random;

public class ModifyPlans2 {

    public static void main(String[] args) {
        // 1. Load existing plans and network
        Config config = ConfigUtils.createConfig();
        config.plans().setInputFile("scenarios/equil/plans100.xml");
        config.network().setInputFile("scenarios/equil/network.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config);

        // 2. Initialize random generator with fixed seed for reproducibility
        Random random = new Random(1234);

        // 3. Modify each person's plan
        for (Person person : scenario.getPopulation().getPersons().values()) {
            // 3a. Assign income (50% high, 50% low)
            String income = random.nextDouble() > 0.5 ? "high" : "low";
            person.getAttributes().putAttribute("income", income);

            // 3b. Assign mode based on income
            String mode;
            if ("low".equals(income)) {
                // Low-income: 80% chance of bike/pedelec, 20% car
                mode = random.nextDouble() <= 0.8 ?
                        (random.nextBoolean() ? "bike" : "pedelec") :
                        TransportMode.car;
            } else {
                // High-income: 70% car, 30% bike/pedelec
                mode = random.nextDouble() <= 0.7 ?
                        TransportMode.car :
                        (random.nextBoolean() ? "bike" : "pedelec");
            }

            // 3c. Apply mode to all legs in the plan
            for (PlanElement pe : person.getSelectedPlan().getPlanElements()) {
                if (pe instanceof Leg) {
                    ((Leg) pe).setMode(mode);
                }
            }
        }

        // 4. Save modified plans with embedded attributes
        new PopulationWriter(scenario.getPopulation(), scenario.getNetwork())
                .write("scenarios/equil/plans100_diverse.xml");

        // 5. Print verification sample
        System.out.println("Sample mode assignments:");
        scenario.getPopulation().getPersons().values().stream()
                .limit(5)
                .forEach(p -> {
                    Leg firstLeg = (Leg) p.getSelectedPlan().getPlanElements().stream()
                            .filter(pe -> pe instanceof Leg)
                            .findFirst()
                            .get();
                    System.out.printf("%s (income: %-4s) → %s%n",
                            p.getId(),
                            p.getAttributes().getAttribute("income"),
                            firstLeg.getMode());
                });

        System.out.println("\nSuccessfully created plans100_diverse.xml with:");
        System.out.println("- Income attributes embedded in each person");
        System.out.println("- Low-income agents: 80% bike/pedelec, 20% car");
        System.out.println("- High-income agents: 70% car, 30% bike/pedelec");
    }
}