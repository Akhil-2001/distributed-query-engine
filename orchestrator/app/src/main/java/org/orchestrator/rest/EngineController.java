package org.orchestrator.rest;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Map;

import org.orchestrator.core.TaskExecutor;
import org.orchestrator.core.TaskPlan;

/**
 * EngineController implemented with Javalin.
 *
 * Endpoints:
 *  - GET /startTask
 *  - GET /completeTask (no workerId)
 *  - GET /completeTask/:workerId
 */
public class EngineController {
	private Javalin app;

	/** Start the HTTP server on the given port. */
	public void start(int port) {
		// Use a minimal Javalin instance. Keep logging lightweight via a before-handler.
		app = Javalin.create();

		// Simple request logger (method + path)
		app.before(ctx -> System.out.println(ctx.method() + " " + ctx.path()));

		app.get("/startTask", this::handleStartTask);
		app.get("/completeTask/{workerId}", this::handleCompleteTaskWithId);

		app.get("/", ctx -> ctx.result("EngineController running"));

		app.start(port);
		System.out.println("EngineController: Javalin server started on port " + port);
	}

	/** Stop the server (graceful). */
	public void stop() {
		if (app != null) {
			app.stop();
		}
	}

	// Handler implementations — currently skeletons; user will add logic.
	private void handleStartTask(Context ctx) {
		TaskPlan plan = new TaskPlan();
		TaskExecutor.INSTANCE.startTask(plan);
		ctx.result("status OK for start task");
	}

	private void handleCompleteTaskWithId(Context ctx) {
		String workerId = ctx.pathParam("workerId");
		TaskExecutor.INSTANCE.onTaskComplete(workerId);
		ctx.result("status OK for complete task and worker " + workerId);
	}
}
