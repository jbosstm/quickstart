package org.jboss.narayana.jta.quickstarts;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreAdaptor;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.InfinispanSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.InfinispanStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import jakarta.transaction.UserTransaction;
import org.infinispan.manager.DefaultCacheManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InfinispanSlotStoreExample {
    private final static String CACHE_CONFIG_FILE = "/infinispan-config.xml";
    private static final String CACHE_NAME = "sharedCache";

    private static List<DefaultCacheManager> cacheManagers; // the cluster of infinispan cache managers

    public static void main(String[] args) throws Exception {
        setupStore();

        UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        utx.begin();
        Util.enlistResources();
        utx.commit();

        shutdownStore();

        System.exit(0); // exit immediately
    }

    public static void setupStore() throws IOException {
        // common config for each slot store
        SlotStoreEnvironmentBean slotStoreConfig = BeanPopulator.getDefaultInstance(SlotStoreEnvironmentBean.class);
        InfinispanStoreEnvironmentBean config = BeanPopulator.getDefaultInstance(InfinispanStoreEnvironmentBean.class);

        cacheManagers = new ArrayList<>();

        var manager = createCacheManager();
        var slots = new InfinispanSlots(); // slot store backed by an infinispan cache

        config.setNumberOfSlots(slotStoreConfig.getNumberOfSlots());
        config.setBytesPerSlot(slotStoreConfig.getBytesPerSlot());
        config.setStoreDir(slotStoreConfig.getStoreDir());
        config.setSyncWrites(true);
        config.setSyncDeletes(true);
        config.setBackingSlots(slots);

        config.setNodeAddress(manager.getNodeAddress());
        config.setIgnoreReturnValues(true);

        config.setCacheName(CACHE_NAME);

        config.setBackingSlots(slots);
        config.setCache(manager.getCache(CACHE_NAME));

        slots.init(config); // can throw IOException

        cacheManagers.add(manager);

        // tell the recovery manager that we are using the slot store
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).
                setObjectStoreType(SlotStoreAdaptor.class.getName());
    }

    public static void shutdownStore() {
        for (DefaultCacheManager manager: cacheManagers) {
            manager.shutdownAllCaches();
            manager.stop();
        }

        TransactionReaper.terminate(true);
    }

    private static DefaultCacheManager createCacheManager() throws IOException {
        return new DefaultCacheManager(
                InfinispanSlotStoreExample.class.getResourceAsStream(CACHE_CONFIG_FILE));
    }
}