package run.bemin.api.review.messaging;

import java.util.UUID;

public class ReviewEvent {
  private UUID storeId;
  private double averageRating;

  public ReviewEvent(UUID storeId, double averageRating) {
    this.storeId = storeId;
    this.averageRating = averageRating;
  }

  public UUID getStoreId() {
    return storeId;
  }

  public double getAverageRating() {
    return averageRating;
  }
}

