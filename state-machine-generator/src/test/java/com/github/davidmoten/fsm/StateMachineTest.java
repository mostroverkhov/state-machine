package com.github.davidmoten.fsm;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.davidmoten.fsm.generated.ShipBehaviour;
import com.github.davidmoten.fsm.generated.ShipBehaviourBase;
import com.github.davidmoten.fsm.generated.ShipStateMachine;
import com.github.davidmoten.fsm.model.State;
import com.github.davidmoten.fsm.model.StateMachine;
import com.github.davidmoten.fsm.runtime.Create;
import com.github.davidmoten.fsm.runtime.Event;

public class StateMachineTest {

	@Test
	public void test() throws IOException {
		File directory = new File("target/generated-sources/java");
		String pkg = "com.github.davidmoten.fsm.generated";

		StateMachine<Ship> m = StateMachine.create(Ship.class);

		// create states (with the event used to transition to it)
		State<Void> neverOutside = m.state("Never Outside", Create.class);
		State<Out> outside = m.state("Outside", Out.class);
		State<In> insideNotRisky = m.state("Inside Not Risky", In.class);
		State<Risky> insideRisky = m.state("Inside Risky", Risky.class);

		// create transitions and generate classes
		neverOutside.initial().to(outside).to(insideNotRisky).to(insideRisky).generateClasses(directory, pkg);

		System.out.println(new String(Files.readAllBytes(
				new File("target/generated-sources/java/com/github/davidmoten/fsm/generated/ShipStateMachine.java")
						.toPath())));
		System.out.println(new String(Files.readAllBytes(
				new File("target/generated-sources/java/com/github/davidmoten/fsm/generated/ShipBehaviour.java")
						.toPath())));
		System.out.println(new String(Files.readAllBytes(
				new File("target/generated-sources/java/com/github/davidmoten/fsm/generated/ShipBehaviourBase.java")
						.toPath())));


	}

	@Test
	public void testRuntime() {
		final Ship ship = new Ship("12345", "6789", 35.0f, 141.3f);
		List<Integer> list = new ArrayList<>();
		ShipBehaviour shipBehaviour = new ShipBehaviourBase() {

			@Override
			public Ship onEntry_Outside(Ship ship, Out out) {
				list.add(1);
				return new Ship(ship.imo(), ship.mmsi(), out.lat, out.lon);
			}

			@Override
			public Ship onEntry_NeverOutside(Create created) {
				list.add(2);
				return ship;
			}

			@Override
			public Ship onEntry_InsideNotRisky(Ship ship, In in) {
				list.add(3);
				return new Ship(ship.imo(), ship.mmsi(), in.lat, in.lon);
			}

		};
		ShipStateMachine m = ShipStateMachine.create(ship, shipBehaviour);
		m = m
				//
				.event(Create.instance())
				//
				.event(new In(1.0f, 2.0f))
				//
				.event(new Out(1.0f, 3.0f));
		assertEquals(Arrays.asList(2, 1), list);
	}

	public static class In implements Event<In> {
		public final float lat;
		public final float lon;

		public In(float lat, float lon) {
			this.lat = lat;
			this.lon = lon;
		}

	}

	public static class Out implements Event<Out> {
		public final float lat;
		public final float lon;

		public Out(float lat, float lon) {
			this.lat = lat;
			this.lon = lon;
		}

	}

	public static class Risky implements Event<Risky> {
		public final String message;

		public Risky(String message) {
			this.message = message;
		}

	}

}