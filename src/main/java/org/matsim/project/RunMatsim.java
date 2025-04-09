//* *********************************************************************** *
//		* project: org.matsim.*												   *
//		*                                                                         *
//		* *********************************************************************** *
//		*                                                                         *
//		* copyright       : (C) 2008 by the members listed in the COPYING,        *
//		*                   LICENSE and WARRANTY file.                            *
//		* email           : info at matsim dot org                                *
//		*                                                                         *
//		* *********************************************************************** *
//		*                                                                         *
//		*   This program is free software; you can redistribute it and/or modify  *
//		*   it under the terms of the GNU General Public License as published by  *
//		*   the Free Software Foundation; either version 2 of the License, or     *
//		*   (at your option) any later version.                                   *
//		*   See also COPYING, LICENSE and WARRANTY file                           *
//		*                                                                         *
//		* *********************************************************************** */
package org.matsim.project;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.ScoringConfigGroup;
import org.matsim.core.config.groups.RoutingConfigGroup;
import org.matsim.core.config.groups.ReplanningConfigGroup;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * @author nagel
 *
 */
public class RunMatsim{

	public static void main(String[] args) {

		Config config;
		if (args == null || args.length == 0 || args[0] == null) {
			config = ConfigUtils.loadConfig("scenarios/equil/config.xml");
		} else {
			config = ConfigUtils.loadConfig(args);
		}

		config.controller().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controller().setLastIteration(2);
		config.controller().setOutputDirectory("output/test");

		config.qsim().setLinkDynamics(QSimConfigGroup.LinkDynamics.PassingQ);
//
//		// possibly modify config here
//
		{
			Collection<String>modes = new ArrayList<>();
			modes.add(TransportMode.car);
			modes.add("pedelec");
			modes.add("bike");
			config.routing().setNetworkModes(modes);

		}
//
		{
			//removing this will make it work but wont be able to view on Via
			Collection<String>modes = new ArrayList<>();
			modes.add(TransportMode.car);
			modes.add("pedelec");
			modes.add("bike");
			config.qsim().setMainModes(modes);

		}

		{
			ScoringConfigGroup.ModeParams params = new ScoringConfigGroup.ModeParams("pedelec");
			config.scoring().addModeParams(params);

		}

		{
			ScoringConfigGroup.ModeParams params = new ScoringConfigGroup.ModeParams("car");
			config.scoring().addModeParams(params);

		}

		{
			ScoringConfigGroup.ModeParams params = new ScoringConfigGroup.ModeParams("bike");
			config.scoring().addModeParams(params);

		}
//
		{
			ReplanningConfigGroup.StrategySettings settings = new ReplanningConfigGroup.StrategySettings();
			settings.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.SubtourModeChoice);
			settings.setWeight(0.1);
			config.replanning().addStrategySettings(settings);
		}

		{
			ReplanningConfigGroup.StrategySettings settings = new ReplanningConfigGroup.StrategySettings();
			settings.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.ReRoute);
			settings.setWeight(0.2);
			config.replanning().addStrategySettings(settings);
		}

		{
			ReplanningConfigGroup.StrategySettings settings = new ReplanningConfigGroup.StrategySettings();
			settings.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.ChangeSingleTripMode);
			settings.setWeight(0.1);
			config.replanning().addStrategySettings(settings);
		}

		{
			ReplanningConfigGroup.StrategySettings settings = new ReplanningConfigGroup.StrategySettings();
			settings.setStrategyName(DefaultPlanStrategiesModule.DefaultSelector.ChangeExpBeta);
			settings.setWeight(0.6);
			config.replanning().addStrategySettings(settings);
		}

		{
			String[]modes={"car","pedelec", "bike", "pt"};
			config.subtourModeChoice().setChainBasedModes(modes);
			config.subtourModeChoice().setModes(modes);

		}


		{
			config.routing().clearTeleportedModeParams();

		}
		{
			RoutingConfigGroup.TeleportedModeParams params = new RoutingConfigGroup.TeleportedModeParams("walk");
			//add walk properties
			params.setTeleportedModeSpeed(4 / 3.6); //speed in m/s
			params.setBeelineDistanceFactor(1.3); // Route efficiency factor
			config.routing().addTeleportedModeParams(params);
		}

		{
			RoutingConfigGroup.TeleportedModeParams params = new RoutingConfigGroup.TeleportedModeParams("pt");
			//add walk properties
			params.setTeleportedModeSpeed(30 / 3.6); //speed in m/s
			params.setBeelineDistanceFactor(1.3); // Route efficiency factor
			config.routing().addTeleportedModeParams(params);
		}

//
//		{
//			//Configure teleported mode parameters for bike
//			RoutingConfigGroup.TeleportedModeParams params = new RoutingConfigGroup.TeleportedModeParams("pedelec");
//			//add bike properties
//			params.setTeleportedModeSpeed(25 / 3.6); //speed in m/s
//			params.setBeelineDistanceFactor(1.3); // Route efficiency factor
//			config.routing().addTeleportedModeParams(params);
//		}

//		{
//			//Configure teleported mode parameters for bike
//			RoutingConfigGroup.TeleportedModeParams params = new RoutingConfigGroup.TeleportedModeParams("bike");
//			//add bike properties
//			params.setTeleportedModeSpeed(25 / 3.6); //speed in m/s
//			params.setBeelineDistanceFactor(1.3); // Route efficiency factor
//			config.routing().addTeleportedModeParams(params);
//		}
		Scenario scenario = ScenarioUtils.loadScenario(config) ;
		{
			//Adjust the speed of the pedelec
			Id<VehicleType>vehicleId = Id.create( "pedelec", VehicleType.class);
			VehicleType pedelecType = VehicleUtils.createVehicleType(vehicleId);
			pedelecType.setMaximumVelocity(25/3.6);
			scenario.getVehicles().addVehicleType(pedelecType);
		}

		{
			//Adjust the speed of the car
			Id<VehicleType>vehicleId = Id.create( "car", VehicleType.class);
			VehicleType carType = VehicleUtils.createVehicleType(vehicleId);
			carType.setMaximumVelocity(100/3.6);
			scenario.getVehicles().addVehicleType(carType);
		}

		{
			//Adjust the speed of the pedelec
			Id<VehicleType>vehicleId = Id.create( "bike", VehicleType.class);
			VehicleType carType = VehicleUtils.createVehicleType(vehicleId);
			carType.setMaximumVelocity(50/3.6);
			scenario.getVehicles().addVehicleType(carType);
		}
		//Needed to do this to add
//		for(Link link: scenario.getNetwork().getLinks().values()){
//
//			String linkId = link.getId().toString();
//			if (linkId.equals("6") || linkId.equals("7")){
//				continue;
//			}
//
//			Set<String>modes = new HashSet<>();
//			modes.add("car");
//			modes.add("pedelec");
//			modes.add("bike");
//			modes.add("pt");
//			link.setAllowedModes(modes);
//		}

		Controler controler = new Controler( scenario) ;
		// Add this to your simulation setup
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				addEventHandlerBinding().toInstance(new LinkEnterEventHandler() {
					@Override
					public void handleEvent(LinkEnterEvent event) {
						String linkId = event.getLinkId().toString();
						if (linkId.equals("2") || linkId.equals("7")) {
							System.out.println("Vehicle " + event.getVehicleId() +
									" entered restricted link " + linkId);
						}
					}

					@Override
					public void reset(int iteration) {}
				});
			}
		});

		// possibly modify controler here

//		controler.addOverridingModule( new OTFVisLiveModule() ) ;

//		controler.addOverridingModule( new SimWrapperModule() );

		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				addEventHandlerBinding().toInstance(new PersonDepartureEventHandler() {
					@Override
					public void handleEvent(PersonDepartureEvent event) {
						System.out.println("Agent" + event.getPersonId()+ "departed mode" + event.getLegMode());
					}
				});

			}
		});

		// ---

		controler.run();
	}

}