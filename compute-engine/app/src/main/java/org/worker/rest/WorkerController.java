package org.worker.rest;

import io.javalin.Javalin;

/**
 * WorkerController implemented using Javalin lightweight HTTP framework.
 *
 * Provides two endpoints (skeletons):
 * - GET /invokeRead/:param_blob_id
 * - GET /invokeShuffle/:worker_id
 *
 * Implement business logic inside `handleInvokeRead` and `handleInvokeShuffle`.
 */
public class WorkerController {
	
	private Javalin app;
	private static String ORCHESTRATOR_URL = "http://localhost:9000";
	private java.util.concurrent.ExecutorService executor;
	private WorkerHandlers handlers;

	/** Start the HTTP server on the given port. */
	public void start(int port) {
		// Use default Javalin configuration. Set response content type per-handler.
		app = Javalin.create().start(port);

		// create a dedicated executor for handling long-running work off the HTTP thread
		this.executor = java.util.concurrent.Executors.newFixedThreadPool(10);
		this.handlers = new WorkerHandlers(this.executor, ORCHESTRATOR_URL);

		app.get("/invokeRead/{blob_id}", handlers::invokeRead);
		app.get("/invokeShuffle/{worker_id}", handlers::invokeShuffle);
		app.get("/getTransientData", handlers::getTransientData);
		app.get("/writeResult", handlers::writeResult);

	}

	/** Stop the server if running. */
	public void stop() {
		if (app != null) {
			app.stop();
			app = null;
			if (executor != null) {
				executor.shutdownNow();
				executor = null;
			}
		}
	}

}
