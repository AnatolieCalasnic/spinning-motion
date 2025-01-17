package org.myexample.spinningmotion.business.interfc;

import org.myexample.spinningmotion.domain.record.InventoryUpdate;

public interface InventoryTrackingUseCase {
    void broadcastInventoryUpdate(InventoryUpdate update);
}
