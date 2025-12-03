package org.worker.model;

/**
 * Immutable data carrier for a CSV row: id, year, score.
 */
public record Row(String id, String year, int score) {

}
